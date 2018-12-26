package com.qunar.qchat.adrpushserver;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qunar.qchat.constants.AdrPushConstants;
import com.qunar.qchat.constants.Config;
import com.qunar.qchat.dao.model.NotificationInfo;
import com.qunar.qchat.model.response.HwPushAccessTokenResult;
import com.qunar.qchat.model.response.HwPushSendResult;
import com.qunar.qchat.utils.HttpClientUtils;
import com.qunar.qchat.utils.JacksonUtils;
import org.apache.http.util.TextUtils;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class HWPushServerManager implements QPushServerManager {
    private final String appPackageName;
    private  String appId = Config.HWPUSH_QT_SECRET_APPID;//用户在华为开发者联盟申请的appId和appSecret（会员中心->应用管理，点击应用名称的链接）
    private final String appSecretKey;
    private static final Logger LOGGER = LoggerFactory.getLogger(HWPushServerManager.class);

    private static String apiUrl = "https://api.push.hicloud.com/pushsend.do"; //应用级消息下发API
    private static String tokenUrl = "https://login.cloud.huawei.com/oauth2/v2/token"; //获取认证Token的URL
    private static String accessToken;//下发通知消息的认证Token
    private static long tokenExpiredTime;  //accessToken的过期时间 秒s
    private static long tokenRefreshTime;  //accessToken的刷新时间 毫秒ms

    public HWPushServerManager(String appPackageName, String appSecretKey) {
        this.appPackageName = appPackageName;
        this.appSecretKey = appSecretKey;
    }

    //获取下发通知消息的认证Token
    private void refreshToken() {
        try {
            String msgBody = MessageFormat.format(
                    "grant_type=client_credentials&client_secret={0}&client_id={1}",
                    URLEncoder.encode(appSecretKey, "UTF-8"), appId);
            String responce = HttpClientUtils.postBody(tokenUrl, msgBody);
            if(TextUtils.isEmpty(responce)) {
               return;
            }
            HwPushAccessTokenResult result = JacksonUtils.string2Obj(responce, HwPushAccessTokenResult.class);
//                LOGGER.info("refreshToken result : " + result.toString());
            if(result != null && !TextUtils.isEmpty(result.access_token)) {
                accessToken = result.access_token;
                tokenExpiredTime = result.expires_in;
                tokenRefreshTime = System.currentTimeMillis();
            }
        }catch (Exception e){
            LOGGER.error("refreshToken_error : " + e.getMessage());
        }

    }

    //发送Push消息
    private void sendPushMessage(NotificationInfo info) throws IOException {
        if ((System.currentTimeMillis() - tokenRefreshTime) / 1000 >= tokenExpiredTime - 10 * 60) {//有效期设置为（expires_in - 10分钟）
//            LOGGER.info("accessToken过期 请求token  refreshToken");
            refreshToken();
        }
        /*PushManager.requestToken为客户端申请token的方法，可以调用多次以防止申请token失败*/
        /*PushToken不支持手动编写，需使用客户端的onToken方法获取*/
        JSONArray deviceTokens = new JSONArray();//目标设备Token
        deviceTokens.add(info.platkeys.get(0));

        JSONObject body = new JSONObject();//仅通知栏消息需要设置标题和内容，透传消息key和value为用户自定义
        body.put("title", info.title);//消息标题
        body.put("content", info.description);//消息内容体


        JSONObject action = new JSONObject();
        //指定intent
        action.put("type", 1);//类型3为打开APP，其他行为请参考接口文档设置
        //消息点击动作参数
//        String intenturi = "";
//        if(info.type == ProtoMessageOuterClass.SignalType.SignalTypeGroupChat_VALUE) {
//            intenturi = "intent://qunarchat/openGroupChat?jid="+info.fromjid+"&type="+info.type+"&chatid="+info.chatid+"&realjid="+info.realjid+"#Intent;scheme=qtalkaphone;end";
//        } else {
//            intenturi = "intent://qunarchat/openSingleChat?jid="+info.fromjid+"&type="+info.type+"&chatid="+info.chatid+"&realjid="+info.realjid+"#Intent;scheme=qtalkaphone;end";
//        }
        JSONObject param = new JSONObject();
        //"#Intent;launchFlags=0x4000000;component=com.qunar.im/.ui.activity.TabMainActivity;S.jid="+info.fromjid+";i.type="+info.type +";end"
//        param.put("intent", intenturi);
        param.put("intent", "#Intent;component=com.qunar.im/.ui.activity.TabMainActivity;S.jid="+info.fromjid
                +";i.type="+info.type
                +";S.chatid="+info.chatid
                +";S.realjid="+info.realjid
                +";end");


        action.put("param", param);
//        param.put("appPkgName", info.pkgname);//定义需要打开的appPkgName
//        action.put("type", 3);//类型3为打开APP，其他行为请参考接口文档设置
//        action.put("param", param);//消息点击动作参数

        JSONObject msg = new JSONObject();
        msg.put("type", 3);//3: 通知栏消息，异步透传消息请根据接口文档设置 1、透传
        msg.put("action", action);//消息点击动作
        msg.put("body", body);//通知栏消息body内容

//        JSONObject ext = new JSONObject();//扩展信息，含BI消息统计，特定展示风格，消息折叠。
//        ext.put("biTag", "Trump");//设置消息标签，如果带了这个标签，会在回执中推送给CP用于检测某种类型消息的到达率和状态
//        ext.put("icon", "http://pic.qiantucdn.com/58pic/12/38/18/13758PIC4GV.jpg");//自定义推送消息在通知栏的图标,value为一个公网可以访问的URL
        JSONObject hps = new JSONObject();//华为PUSH消息总结构体
        hps.put("msg", msg);
//        hps.put("ext", ext);
        JSONObject payload = new JSONObject();
        payload.put("hps", hps);

        String postBody = MessageFormat.format(
        "access_token={0}&nsp_svc={1}&nsp_ts={2}&device_token_list={3}&payload={4}&expire_time={5}",
        URLEncoder.encode(accessToken,"UTF-8"),
        URLEncoder.encode("openpush.message.api.send","UTF-8"),
        URLEncoder.encode(String.valueOf(System.currentTimeMillis() / 1000),"UTF-8"),
        URLEncoder.encode(deviceTokens.toString(),"UTF-8"),
        URLEncoder.encode(payload.toString(),"UTF-8"),
        URLEncoder.encode(getTimeAfterFive(),"UTF-8"));

        String postUrl = apiUrl + "?nsp_ctx=" + URLEncoder.encode("{\"ver\":\"1\", \"appId\":\"" + appId + "\"}", "UTF-8");

        String responce = HttpClientUtils.postJson(postUrl, postBody);
        if(!TextUtils.isEmpty(responce)) {
            HwPushSendResult result = JacksonUtils.string2Obj(responce, HwPushSendResult.class);
            LOGGER.info("HWPUSH qtalk send postBody={}\n result={} touser={}", postBody, result.toString(), info.toUserName);
        }

    }


    @Override
    public void sendMessageToAlias(List<String> alias, String title, String messagePayload) throws IOException, ParseException {
    }

    @Override
    public void sendMessageToTags(List<String> tags, String messagePayload) throws IOException, ParseException {
    }

    @Override
    public void sendMessageToAll(String messagePayload) throws IOException, ParseException {
    }


    @Override
    public void sendNotifyToAlias(NotificationInfo notificationInfo) throws IOException, ParseException {
        sendPushMessage(notificationInfo);
    }

    @Override
    public void sendNotifyToTags(List<String> tags, String title, String description, String messagePayload) throws IOException, ParseException {
    }

    @Override
    public void sendNotifyToAll(String title, String description, String messagePayload) throws IOException, ParseException {
    }

    @Override
    public void sendLinkNotifyToAlias(List<String> alias, String title, String description, String url) throws IOException, ParseException {
    }

    @Override
    public void sendLinkNotifyToTags(List<String> tags, String title, String description, String url) throws IOException, ParseException {
    }

    @Override
    public void sendLinkNotifyToAll(String title, String description, String url) throws IOException, ParseException {
    }

    @Override
    public String getName() {
        return AdrPushConstants.NAME_HUAWEI;
    }

    @Override
    public String getPkgname() {
        return appPackageName;
    }

    /**
     * 5分钟后时间
     *
     * @return 返回时间类型 yyyy-MM-dd HH:mm:ss
     */
    public String getTimeAfterFive() {
        Date currentTime = new Date(System.currentTimeMillis() + 5 * 60 *1000);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        String dateString = formatter.format(currentTime);
        return dateString;
    }
}
