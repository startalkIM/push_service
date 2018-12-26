package com.qunar.qchat.adrpushserver;

import com.alibaba.fastjson.JSON;
import com.oppo.push.server.Notification;
import com.oppo.push.server.Result;
import com.oppo.push.server.Sender;
import com.oppo.push.server.Target;
import com.qunar.qchat.constants.AdrPushConstants;
import com.qunar.qchat.constants.Config;
import com.qunar.qchat.constants.SchemeConstants;
import com.qunar.qchat.dao.model.NotificationInfo;
import com.qunar.qchat.utils.ProtoMessageOuterClass;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.TextUtils;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * create by hubo.hu (lex) at 2018/8/23
 */
public class OPushServerManager implements QPushServerManager {

    private final String appPackageName;
    private final String appSecretKey;
    private static final String OPPO_APP_KEY = Config.OPUSH_QT_SECRET_APP_KEY;
    private static final Logger LOGGER = LoggerFactory.getLogger(OPushServerManager.class);

    private Sender sender;

    public OPushServerManager(String appPackageName, String appSecretKey) {
        this.appPackageName = appPackageName;
        this.appSecretKey = appSecretKey;
        try {
            sender = new Sender(OPPO_APP_KEY, appSecretKey);
        } catch (Exception e) {
            sender = null;
            LOGGER.info("opush sendNotifyToAlias init Exception send e={}", e.getMessage());
        }
    }

    /**
     * 创建通知栏消息体
     *
     * @return
     */
    private Notification getNotification(NotificationInfo notificationInfo) {
        Notification notification = new Notification();
        /*** 以下参数必填项 */
        //title length limit is 32
        String title = notificationInfo.title;
        if(!TextUtils.isEmpty(title) && title.length() > 32) {
            title = title.substring(0, 32);
        }
        notification.setTitle(title);
        notification.setSubTitle("");
        notification.setContent(notificationInfo.description);

        /*** 以下参数非必填项， 如果需要使用可以参考OPPO push服务端api文档进行设置 */
        // App开发者自定义消息Id，OPPO推送平台根据此ID做去重处理，对于广播推送相同appMessageId只会保存一次，对于单推相同appMessageId只会推送一次
        notification.setAppMessageId(notificationInfo.messageId);
        // 应用接收消息到达回执的回调URL，字数限制200以内，中英文均以一个计算
//        notification.setCallBackUrl("");
        // App开发者自定义回执参数，字数限制50以内，中英文均以一个计算
//        notification.setCallBackParameter("");
        // 点击动作类型0，启动应用；1，打开应用内页（activity的intent action）；2，打开网页；4，打开应用内页（activity）；【非必填，默认值为0】;5,Intent scheme URL
        notification.setClickActionType(4);
        // 应用内页地址【click_action_type为1或4时必填，长度500】
//        notification.setClickActionActivity("com.qunar.im.ui.activity.TabMainActivity");
//        notification.setClickActionActivity("intent:#Intent;component=com.qunar.im/.ui.activity.TabMainActivity;S.jid="+notificationInfo.fromjid+";i.type="+notificationInfo.type +";end");
        //"qtalkaphone://qunarchat/openGroupChat?jid=" + notificationInfo.fromjid + "&type=" + notificationInfo.type
        URIBuilder uriBuilder = new URIBuilder();
        uriBuilder.setScheme(SchemeConstants.getScheme(true));
        if(notificationInfo.type == ProtoMessageOuterClass.SignalType.SignalTypeGroupChat_VALUE) {
            uriBuilder.setHost(SchemeConstants.HOST_GROUPCHAT);
        } else {
            uriBuilder.setHost(SchemeConstants.HOST_SINGLECHAT);
        }
        uriBuilder.addParameter("jid", notificationInfo.fromjid);
        uriBuilder.addParameter("type", notificationInfo.type + "");
        uriBuilder.addParameter("chatid", notificationInfo.chatid);
        uriBuilder.addParameter("realjid", notificationInfo.realjid);
//        notification.setClickActionUrl(uriBuilder.toString());


//        String intenturi = "";
//        if(notificationInfo.type == ProtoMessageOuterClass.SignalType.SignalTypeGroupChat_VALUE) {
//            intenturi = "intent://qunarchat/openGroupChat?jid="+notificationInfo.fromjid+"&type="+notificationInfo.type+"&chatid="+notificationInfo.chatid+"&realjid="+notificationInfo.realjid+"#Intent;scheme=qtalkaphone;end";
//        } else {
//            intenturi = "intent://qunarchat/openSingleChat?jid="+notificationInfo.fromjid+"&type="+notificationInfo.type+"&chatid="+notificationInfo.chatid+"&realjid="+notificationInfo.realjid+"#Intent;scheme=qtalkaphone;end";
//        }
        notification.setClickActionActivity("com.qunar.im.ui.activity.TabMainActivity");
        // 网页地址【click_action_type为2必填，长度500】
//        notification.setClickActionUrl("http://www.test.com");
        // 动作参数，打开应用内页或网页时传递给应用或网页【JSON格式，非必填】，字符数不能超过4K，示例：{"key1":"value1","key2":"value2"}
        Map map = new HashMap<>();
//        map.put("type", notificationInfo.type);
//        map.put("jid", notificationInfo.fromjid);
//        map.put("chatid", notificationInfo.chatid);
//        map.put("realjid", notificationInfo.realjid);
        notification.setActionParameters(JSON.toJSONString(map));
//        notification.setActionParameters("{\"type\":\"notificationInfo.type\",\"jid\":\"notificationInfo.fromjid\"}");
        // 展示类型 (0, “即时”),(1, “定时”)
        notification.setShowTimeType(0);
        // 定时展示开始时间（根据time_zone转换成当地时间），时间的毫秒数
//        notification.setShowStartTime(System.currentTimeMillis() + 1000 * 60 * 3);
        // 定时展示结束时间（根据time_zone转换成当地时间），时间的毫秒数
//        notification.setShowEndTime(System.currentTimeMillis() + 1000 * 60 * 5);
        // 是否进离线消息,【非必填，默认为True】
        notification.setOffLine(true);
        // 离线消息的存活时间(time_to_live) (单位：秒), 【off_line值为true时，必填，最长3天】
        notification.setOffLineTtl(3600);
        // 时区，默认值：（GMT+08:00）北京，香港，新加坡
        notification.setTimeZone("GMT+08:00");
        // 0：不限联网方式, 1：仅wifi推送
        notification.setNetworkType(0);
        return notification;
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
        //使用appKey, masterSecret创建sender对象（每次发送消息都使用这个sender对象）
//        LOGGER.info("opush sendNotifyToAlias send0 key={} touser={}", appSecretKey, notificationInfo.toUserName);
        if(sender == null) {
            try {
                sender = new Sender(OPPO_APP_KEY, appSecretKey);
            } catch (Exception e) {
                sender = null;
                LOGGER.info("opush sendNotifyToAlias Exception send e={} touser={}", e.getMessage(), notificationInfo.toUserName);
            }
        }

        if(sender == null) {
            LOGGER.info("opush sendNotifyToAlias failed send key={} touser={}", appSecretKey, notificationInfo.toUserName);
            return;
        }
//        LOGGER.info("opush sendNotifyToAlias send1 key={} touser={}", appSecretKey, notificationInfo.toUserName);
        //发送单推通知栏消息
        Notification notification = getNotification(notificationInfo); //创建通知栏消息体
//        LOGGER.info("opush sendNotifyToAlias send key={} touser={}", appSecretKey, notificationInfo.toUserName);
        for (String item : notificationInfo.platkeys) {
            Target target = Target.build(item); //创建发送对象
            Result result = null;  //发送单推消息
            try {
                result = sender.unicastNotification(notification, target);
            } catch (Exception e) {
                LOGGER.error("opush qtalk send key={} touser={} e={}", item, notificationInfo.toUserName, e);
            }
//            result.getStatusCode(); // 获取http请求状态码
//            result.getReturnCode(); // 获取平台返回码
//            result.getMessageId();  // 获取平台返回的messageId
            LOGGER.info("opush qtalk send key={} result={} touser={}", item, result == null ? "null" : result.toString(), notificationInfo.toUserName);
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
        return AdrPushConstants.NAME_OPPO;
    }

    @Override
    public String getPkgname() {
        return appPackageName;
    }

    @Override
    public String toString() {
        return "platname=" + getName()
                + "pkgname=" + appPackageName
                + "appSecretKey=" + appSecretKey;
    }
}
