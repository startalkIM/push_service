package com.qunar.qchat.adrpushserver;

import com.qunar.qchat.constants.AdrPushConstants;
import com.qunar.qchat.dao.model.NotificationInfo;
import com.qunar.qchat.utils.HttpClientUtils;
import com.qunar.qchat.utils.JacksonUtils;
import com.qunar.qchat.utils.SecurityUtils;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * create by hubo.hu (lex) at 2019/10/16
 */
public class FCMPushServerManager implements QPushServerManager {


    private final String appPackageName;
    private final String mServerKey;
    private static final Logger LOGGER = LoggerFactory.getLogger(FCMPushServerManager.class);

    private static final String FCM_URL = "https://fcm.googleapis.com/fcm/send";

    public FCMPushServerManager(String packageName, String serverKey) {
        this.appPackageName = packageName;
        this.mServerKey = serverKey;
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
        for (String token : notificationInfo.platkeys) {
            try {
                Map<String, Object> msg = new HashMap<>();
                msg.put("to", token);
                msg.put("priority", "high");
                msg.put("time_to_live", 60);
                msg.put("content_available", true);

                Map<String, String> notification = new HashMap<>();
                notification.put("title", notificationInfo.title);
                notification.put("body", notificationInfo.description);
                notification.put("click_action", "intent:#Intent;component=" + notificationInfo.pkgname + "/com.qunar.im.ui.activity.TabMainActivity;S.jid="
                        + notificationInfo.fromjid
                        + ";i.type="+notificationInfo.type
                        + ";S.chatid="+notificationInfo.chatid
                        + ";S.realjid="+notificationInfo.realjid
                        + ";end");
                notification.put("tag", String.valueOf(Math.abs(SecurityUtils.transformStringToInt(notificationInfo.messageId))));

                msg.put("notification", notification);

                Map<String, String> header = new HashMap<>();
                header.put("Content-Type", "application/json");
                header.put("Authorization", "key=" + mServerKey);

                String result = HttpClientUtils.postJsonWithHeader(FCM_URL, JacksonUtils.obj2String(msg), header);
                LOGGER.info("fcmpush send key={} pkg={} header={} body={} result={} touser={}", token, JacksonUtils.obj2String(header), JacksonUtils.obj2String(msg), notificationInfo.pkgname, result, notificationInfo.toUserName);

            } catch (Exception e) {
                LOGGER.error("fcmpush send key={} pkg={} touser={} e={}", token, notificationInfo.pkgname, notificationInfo.toUserName, e);

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
        return AdrPushConstants.NAME_FCM;
    }

    @Override
    public String getPkgname() {
        return appPackageName;
    }
}
