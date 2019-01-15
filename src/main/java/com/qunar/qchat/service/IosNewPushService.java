package com.qunar.qchat.service;

import com.qunar.qchat.constants.Config;
import com.qunar.qchat.dao.model.NotificationInfo;
import com.qunar.qchat.utils.JacksonUtils;
import com.qunar.qchat.utils.ProtoMessageOuterClass;
import com.qunar.qtalk.ss.common.utils.JsonUtil;
import com.qunar.qtalk.ss.common.utils.watcher.QMonitor;
import com.turo.pushy.apns.ApnsClient;
import com.turo.pushy.apns.ApnsClientBuilder;
import com.turo.pushy.apns.DeliveryPriority;
import com.turo.pushy.apns.PushNotificationResponse;
import com.turo.pushy.apns.util.ApnsPayloadBuilder;
import com.turo.pushy.apns.util.SimpleApnsPushNotification;
import com.turo.pushy.apns.util.TokenUtil;
import com.turo.pushy.apns.util.concurrent.PushNotificationFuture;
import com.turo.pushy.apns.util.concurrent.PushNotificationResponseListener;
import org.apache.http.util.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lffan.liu on 2018/1/23.
 */
@Service
public class IosNewPushService {
    private static final Logger LOGGER = LoggerFactory.getLogger(IosNewPushService.class);

    @Autowired
    private QIosPushServer qIosPushServer;

    private ApnsClient client = null;
    private Map<String, ApnsClient> serviceMap;

    public void iosPush(NotificationInfo notificationInfo) {
        try {
            if (serviceMap == null) {
                serviceMap = new HashMap<>();
            }

            if (serviceMap.containsKey(notificationInfo.pkgname)) {
                client = serviceMap.get(notificationInfo.pkgname);
            } else {
                client = null;
            }
            String cert = "";
            String pwd = "";
            if(qIosPushServer.isBeta(notificationInfo.pkgname)) {
                if(qIosPushServer.getBetaCert(notificationInfo.pkgname) != null) {
                    cert = qIosPushServer.getBetaCert(notificationInfo.pkgname).certPath;
                    pwd = qIosPushServer.getBetaCert(notificationInfo.pkgname).certPwd;
                }
            } else if(qIosPushServer.isProd(notificationInfo.pkgname)) {
                if(qIosPushServer.getProCert(notificationInfo.pkgname) != null) {
                    cert = qIosPushServer.getProCert(notificationInfo.pkgname).certPath;
                    pwd = qIosPushServer.getProCert(notificationInfo.pkgname).certPwd;
                }
            }
            if(TextUtils.isEmpty(cert) || TextUtils.isEmpty(pwd)) {
                QMonitor.recordOne("send_ios_message_failure_nobid");
                LOGGER.info("ios push 没找到证书  cert={} pwd={} info ={}", cert, pwd, JsonUtil.obj2String(notificationInfo));
                return;
            }
            if (client == null) {
                if(qIosPushServer.isBeta(notificationInfo.pkgname)) {
                    client = new ApnsClientBuilder()
                            .setApnsServer(ApnsClientBuilder.DEVELOPMENT_APNS_HOST)
                            .setClientCredentials(new File(cert), pwd)
                            .build();
                } else if(qIosPushServer.isProd(notificationInfo.pkgname)) {
                    client = new ApnsClientBuilder()
                            .setApnsServer(ApnsClientBuilder.PRODUCTION_APNS_HOST)
                            .setClientCredentials(new File(cert), pwd)
                            .build();
                }

            }
            if(client == null) {
                QMonitor.recordOne("send_ios_message_failure_nobid");
                LOGGER.info("ios push 不存在该bundleid  info ={}", JsonUtil.obj2String(notificationInfo));
                return;
            }
            final String fcert = cert;
            serviceMap.put(notificationInfo.pkgname, client);

            final ApnsPayloadBuilder payloadBuilder = new ApnsPayloadBuilder();
            payloadBuilder.setAlertTitle(notificationInfo.title)
                    .setAlertBody(notificationInfo.description)
                    .setSound("default")
                    .addCustomProperty("msgtype", notificationInfo.msg_type)
                    .addCustomProperty("chattype", notificationInfo.type)
                    .addCustomProperty("jid", notificationInfo.fromjid)
                    .addCustomProperty("userid", notificationInfo.fromName + "@" + notificationInfo.fromHost);

            final String token = TokenUtil.sanitizeTokenString(notificationInfo.platkeys.get(0));

            if (notificationInfo.type == ProtoMessageOuterClass.SignalType.SignalTypeChat_VALUE
                    || notificationInfo.type == ProtoMessageOuterClass.SignalType.SignalTypeGroupChat_VALUE) {

                payloadBuilder.setCategoryName("msg");
                payloadBuilder.addCustomProperty("category", "msg");
            }
            if (notificationInfo.type == ProtoMessageOuterClass.SignalType.SignalTypeConsult_VALUE) {
                if ("com.qunar.qchat".equalsIgnoreCase(notificationInfo.pkgname)
                        || "com.qunar.qtalkDevelop".equalsIgnoreCase(notificationInfo.pkgname)) {
                    payloadBuilder.setSound("新咨询的播报.wav");
                }
                payloadBuilder.addCustomProperty("chatid", notificationInfo.chatid);
                payloadBuilder.addCustomProperty("realjid", notificationInfo.realjid);
            }

            String payload = payloadBuilder.buildWithDefaultMaximumLength();
            SimpleApnsPushNotification pushNotification = new SimpleApnsPushNotification(token, notificationInfo.pkgname,
                    payload, new Date(System.currentTimeMillis() + SimpleApnsPushNotification.DEFAULT_EXPIRATION_PERIOD_MILLIS),
                    DeliveryPriority.IMMEDIATE, notificationInfo.messageId);

            final PushNotificationFuture<SimpleApnsPushNotification, PushNotificationResponse<SimpleApnsPushNotification>>
                    sendNotificationFuture = client.sendNotification(pushNotification);

            sendNotificationFuture.addListener(new PushNotificationResponseListener<SimpleApnsPushNotification>() {

                @Override
                public void operationComplete(final PushNotificationFuture<SimpleApnsPushNotification, PushNotificationResponse<SimpleApnsPushNotification>> future) throws Exception {
                    // When using a listener, callers should check for a failure to send a
                    // notification by checking whether the future itself was successful
                    // since an exception will not be thrown.
                    if (future.isSuccess()) {
                        final PushNotificationResponse<SimpleApnsPushNotification> pushNotificationResponse =
                                sendNotificationFuture.getNow();

                        String result = "";
                        if (pushNotificationResponse.isAccepted()) {
                            result = "Push notification accepted by APNs gateway.";
                        } else {
                            result = "Notification rejected by the APNs gateway: " + pushNotificationResponse.getRejectionReason();

                            if (pushNotificationResponse.getTokenInvalidationTimestamp() != null) {
                                result += "\t…and the token is invalid as of " + pushNotificationResponse.getTokenInvalidationTimestamp();
                            }
                        }
                        QMonitor.recordOne("send_ios_message");
                        LOGGER.info("ios push send result={} cerpath={} notificationinfo={} touser={}", result, fcert, JacksonUtils.obj2String(notificationInfo), notificationInfo.toUserName);

                        // Handle the push notification response as before from here.
                    } else {
                        // Something went wrong when trying to send the notification to the
                        // APNs gateway. We can find the exception that caused the failure
                        // by getting future.cause().
                        QMonitor.recordOne("send_ios_message_failure");
                        LOGGER.info("ios push send failure reuslt={}", future.cause().getMessage());
                    }
                }

            });
        } catch (Exception e) {
            LOGGER.error("IosNewPushService Exception={} msg={}", e, JacksonUtils.obj2String(notificationInfo));
        }
    }


}
