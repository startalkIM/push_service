package com.qunar.qchat.adrpushserver;

import com.meizu.push.sdk.utils.MD5Util;
import com.oppo.push.server.Sender;
import com.qunar.qchat.constants.AdrPushConstants;
import com.qunar.qchat.dao.model.NotificationInfo;
import com.qunar.qchat.model.VivoAuthRequest;
import com.qunar.qchat.model.VivoSinglePushRequest;
import com.qunar.qchat.model.response.VivoAuthTokenResult;
import com.qunar.qchat.utils.HttpClientUtils;
import com.qunar.qchat.utils.JacksonUtils;
import org.apache.http.util.TextUtils;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * create by hubo.hu (lex) at 2018/8/23
 */
public class VivoPushServerManager implements QPushServerManager {

    private String appPackageName;
    private int appid;
    private String appKey;
    private String secretKey;
//     privatestatic final int VIVO_APP_ID = Config.VPUSH_QT_APP_ID;
//    private static final String VIVO_APP_KEY = Config.VPUSH_QT_APP_KEY;
    private static final Logger LOGGER = LoggerFactory.getLogger(VivoPushServerManager.class);

    private static final String VIVO_URL = "https://api-push.vivo.com.cn";
    private static final String AUTH_URL = VIVO_URL + "/message/auth";
    private static final String SINGLE_PUSH_URL = VIVO_URL + "/message/send";

    private String authToken;
    private long lastTimestamp;

    private Sender sender;

    public VivoPushServerManager(String appPackageName, int appID, String appKey, String appSecretKey) {
        this.appid = appID;
        this.appKey = appKey;
        this.appPackageName = appPackageName;
        this.secretKey = appSecretKey;
    }

    private void getAuth() {
        try {
            long timestamp = System.currentTimeMillis();
            VivoAuthRequest vivoAuthRequest = new VivoAuthRequest();
            vivoAuthRequest.appId = appid;
            vivoAuthRequest.appKey = appKey;
            vivoAuthRequest.sign = MD5Util.MD5Encode(appid + appKey.trim() + timestamp + secretKey.trim());
            vivoAuthRequest.timestamp = timestamp;

            String responce = HttpClientUtils.postJson(AUTH_URL, JacksonUtils.obj2String(vivoAuthRequest));
            LOGGER.info("vivo getAuth result : " + responce);
            if(TextUtils.isEmpty(responce)) {
                LOGGER.error("vivo getAuth failed : " + responce);
                return;
            }
            VivoAuthTokenResult result = JacksonUtils.string2Obj(responce, VivoAuthTokenResult.class);
//                LOGGER.info("refreshToken result : " + result.toString());
            if(result != null && result.result == 0 && !TextUtils.isEmpty(result.authToken)) {
                authToken = result.authToken;
                lastTimestamp = timestamp;
            } else {
                LOGGER.error("vivo getAuth failed : " + responce);
                authToken = "";
            }
        }catch (Exception e){
            LOGGER.error("vivo getAuth failed : " + e.getMessage());
            authToken= "";
        }
    }

    /**
     * 创建通知栏消息体
     *
     * @return
     */
    private VivoSinglePushRequest getNotification(String regid, NotificationInfo info) {
        VivoSinglePushRequest request = new VivoSinglePushRequest();
        request.title = info.title;//通知标题(用于通知栏消息) 最大 20 个汉字 (一个汉字等于两个英文字符，即最大不超 过 40 个英文字符)
        request.content = info.description;//通知内容(用于通知栏消息) 最大 50 个汉字 (一个汉字等于两个英文字符，即最大不超 过 100 个英文字符)
        request.regId = regid;
        request.requestId = UUID.randomUUID().toString();
        request.timeToLive = 60 * 60 * 24;
        request.notifyType = 4;//通知类型 1:无，2:响铃，3:振动，4:响铃和振动
        request.skipType = 4;//点击跳转类型 1:打开 APP 首页 2:打开链 接 3:自定义 4:打开 app 内指定页面
        request.skipContent =  "intent:#Intent;component="
                + info.pkgname + "/"
                + info.pkgname + ".ui.activity.TabMainActivity;S.jid="
                + info.fromjid
                + ";i.type="+info.type
                + ";S.chatid="+info.chatid
                + ";S.realjid="+info.realjid
                + ";end";

        return request;
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
        if ((System.currentTimeMillis() - lastTimestamp) >= 1000 * 60 * 60) {//1小时请求请求一次authtoken
            getAuth();
        }

        for (String item : notificationInfo.platkeys) {
            if(!TextUtils.isEmpty(item)) {
                try {
                    //发送单推通知栏消息
                    VivoSinglePushRequest request = getNotification(item, notificationInfo); //创建通知栏消息体
                    Map<String, String> header = new HashMap<>();
                    header.put("authToken", authToken);
                    String result = HttpClientUtils.postJsonWithHeader(SINGLE_PUSH_URL, JacksonUtils.obj2String(request), header);
                    LOGGER.info("vpush send key={} pkg={} result={} touser={}", item, notificationInfo.pkgname, result, notificationInfo.toUserName);
                } catch (Exception e) {
                    LOGGER.error("vpush send key={} pkg={} touser={} e={}", item, notificationInfo.pkgname, notificationInfo.toUserName, e);
                }
            }
        }

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
        return AdrPushConstants.NAME_VIVO;
    }

    @Override
    public String getPkgname() {
        return appPackageName;
    }

    @Override
    public String toString() {
        return "platname=" + getName()
                + "pkgname=" + appPackageName
                + "appSecretKey=" + secretKey;
    }
}
