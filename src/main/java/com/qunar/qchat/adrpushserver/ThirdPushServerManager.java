package com.qunar.qchat.adrpushserver;

import com.qunar.qchat.dao.model.NotificationInfo;
import com.qunar.qchat.utils.SecurityUtils;
import com.xiaomi.xmpush.server.Constants;
import com.xiaomi.xmpush.server.Message;
import com.xiaomi.xmpush.server.Result;
import com.xiaomi.xmpush.server.Sender;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;


public class ThirdPushServerManager implements QPushServerManager {
    private final String appPackageName;
    private final String appSecretKey;
    private final String appPlatName;
    private static final Logger LOGGER = LoggerFactory.getLogger(ThirdPushServerManager.class);

    public ThirdPushServerManager(String appPackageName, String appSecretKey, String name) {
        this.appPackageName = appPackageName;
        this.appSecretKey = appSecretKey;
        this.appPlatName = name;
    }
    private Message getLinkMessage(String title, String description, String url){
        return new Message.Builder()
                .title(title)
                .description(description)
                .restrictedPackageName(appPackageName)
                .passThrough(0)  //消息使用通知栏方式
                .notifyType(1)
                .extra(Constants.EXTRA_PARAM_NOTIFY_EFFECT, Constants.NOTIFY_WEB)
                .extra(Constants.EXTRA_PARAM_WEB_URI, url)
                .build();
    }

    private Message getNotifyMessage(String fromjid, int type, String title, String description, String messagePayload){
        return new Message.Builder()
                .title(title)
                .description(description).payload(messagePayload)
                .restrictedPackageName(appPackageName)
                .passThrough(0)
                .notifyType(1)
                .timeToLive(5 * 60 * 1000)
                .notifyId(Math.abs(SecurityUtils.transformStringToInt(fromjid)))
                .extra(Constants.EXTRA_PARAM_NOTIFY_EFFECT, Constants.NOTIFY_ACTIVITY)//点击通知栏启动应用
                .extra(Constants.EXTRA_PARAM_NOTIFY_FOREGROUND, "0")//应用前期不显示通知
                .extra(Constants.EXTRA_PARAM_INTENT_URI,
                        "intent:#Intent;component=com.qunar.im/.ui.activity.TabMainActivity;S.jid="+fromjid+";i.type="+type+";end")//启动应用传传参数
                .build();
    }

    private Message getNotifyMessage(NotificationInfo notificationInfo){
//        String intenturi = "";
//        if(notificationInfo.type == ProtoMessageOuterClass.SignalType.SignalTypeGroupChat_VALUE) {
//            intenturi = "intent://qunarchat/openGroupChat?jid="+notificationInfo.fromjid+"&type="+notificationInfo.type+"&chatid="+notificationInfo.chatid+"&realjid="+notificationInfo.realjid+"#Intent;scheme=qtalkaphone;end";
//        } else {
//            intenturi = "intent://qunarchat/openSingleChat?jid="+notificationInfo.fromjid+"&type="+notificationInfo.type+"&chatid="+notificationInfo.chatid+"&realjid="+notificationInfo.realjid+"#Intent;scheme=qtalkaphone;end";
//        }

        return new Message.Builder()
                .title(notificationInfo.title)
                .description(notificationInfo.description).payload(notificationInfo.json)
                .restrictedPackageName(appPackageName)
                .passThrough(0)
                .notifyType(1)
                .timeToLive(5 * 60 * 1000)
                .notifyId(Math.abs(SecurityUtils.transformStringToInt(notificationInfo.messageId)))
//                .notifyId(notificationInfo.fromjid.hashCode())
                .extra(Constants.EXTRA_PARAM_NOTIFY_FOREGROUND, "0")//应用前期不显示通知
                .extra(Constants.EXTRA_PARAM_NOTIFY_EFFECT, Constants.NOTIFY_ACTIVITY)//点击通知栏启动应用
                .extra(Constants.EXTRA_PARAM_INTENT_URI,
                        "intent:#Intent;component=com.qunar.im/.ui.activity.TabMainActivity;S.jid="+notificationInfo.fromjid
                                +";i.type="+notificationInfo.type
                                +";S.chatid="+notificationInfo.chatid
                                +";S.realjid="+notificationInfo.realjid
                                +";end")//启动应用传传参数
//                .extra(Constants.EXTRA_PARAM_INTENT_URI, intenturi)
//                .extra(Constants.EXTRA_PARAM_NOTIFY_EFFECT, Constants.NOTIFY_WEB)
//                .extra(Constants.EXTRA_PARAM_WEB_URI, uriBuilder.toString())
                .build();
    }

    private Message getMessage(String title, String messagePayload){
        return new Message.Builder()
                .passThrough(1)
                .payload(messagePayload)
                .restrictedPackageName(appPackageName)
                .notifyType(0)
                .notifyId(Math.abs(SecurityUtils.transformStringToInt(title)))
                .build();
    }



    @Override
    public void sendMessageToAlias(List<String> alias, String title, String messagePayload) throws IOException, ParseException {
        Constants.useOfficial();
        Sender sender = getSender();
        Message message = getMessage(title, messagePayload);
        for (String item : alias) {
            Result result = sender.sendToAlias(message, item, 10);
            LOGGER.info("thirdpush qtalk send key={} result={} touser={}", item, result.toString());
        }
    }

    @Override
    public void sendMessageToTags(List<String> tags, String messagePayload) throws IOException, ParseException {
        Constants.useOfficial();
        Sender sender = getSender();
        Message message = getMessage("", messagePayload);
        for (String item : tags) {
            Result result = sender.broadcast(message, item, 10);
            LOGGER.info("thirdpush qtalk send key={} result={} ", item, result.toString());
        }
    }

    @Override
    public void sendMessageToAll(String messagePayload) throws IOException, ParseException {
        Constants.useOfficial();
        Sender sender = getSender();
        Message message = getMessage("", messagePayload);
        Result result = sender.broadcastAll(message, 10);
        LOGGER.info("thirdpush qtalk send result={}", result.toString());
    }


    @Override
    public void sendNotifyToAlias(NotificationInfo notificationInfo) throws IOException, ParseException {
        Constants.useOfficial();
        Sender sender = getSender();
        Message message = getNotifyMessage(notificationInfo);
        for (String item : notificationInfo.platkeys) {
            Result result = sender.sendToAlias(message, item, 10);
            LOGGER.info("thirdpush qtalk send key={} result={} touser={}", item, result.toString(), notificationInfo.toUserName);
        }
    }

    @Override
    public void sendNotifyToTags(List<String> tags, String title, String description, String messagePayload) throws IOException, ParseException {
        Constants.useOfficial();
        Sender sender = new Sender(appSecretKey);
        Message message = getNotifyMessage("", 0, title, description, messagePayload);

        for (String item : tags) {
            Result result = sender.broadcast(message, item, 10);
            LOGGER.info("thirdpush qtalk send key={} result={} ", item, result.toString());
        }
    }

    @Override
    public void sendNotifyToAll(String title, String description, String messagePayload) throws IOException, ParseException {
        Constants.useOfficial();
        Sender sender = getSender();
        Message message = getNotifyMessage("", 0, title, description, messagePayload);

        Result result = sender.broadcastAll(message, 10);
        LOGGER.info("thirdpush qtalk send result={} ", result.toString());
    }

    @Override
    public void sendLinkNotifyToAlias(List<String> alias, String title, String description, String url) throws IOException, ParseException {
        Constants.useOfficial();
        Sender sender = new Sender(appSecretKey);
        Message message = getLinkMessage(title, description, url);
        for (String item : alias) {
            Result result = sender.sendToAlias(message, item, 10);
            LOGGER.info("thirdpush qtalk send key={} result={} ", item, result.toString());
        }
    }

    @Override
    public void sendLinkNotifyToTags(List<String> tags, String title, String description, String url) throws IOException, ParseException {
        Constants.useOfficial();
        Sender sender = getSender();
        Message message = getLinkMessage(title, description, url);

        for (String item : tags) {
            Result result = sender.broadcast(message, item, 10);
            LOGGER.info("thirdpush qtalk send key={} result={} ", item, result.toString());
        }
    }

    @Override
    public void sendLinkNotifyToAll(String title, String description, String url) throws IOException, ParseException {
        Constants.useOfficial();
        Sender sender = getSender();
        Message message = getLinkMessage(title, description, url);

        Result result = sender.broadcastAll(message, 10);
        LOGGER.info("thirdpush qtalk send result={} ", result.toString());
    }

    private Sender getSender(){
        return new Sender(appSecretKey);
    }

    @Override
    public String getName() {
        return appPlatName;
    }

    @Override
    public String getPkgname() {
        return appPackageName;
    }
}
