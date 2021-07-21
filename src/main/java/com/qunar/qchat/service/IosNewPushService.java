package com.qunar.qchat.service;

import com.alibaba.fastjson.JSON;
//import com.notnoop.apns.PayloadBuilder;
import com.qunar.qchat.constants.Config;
import com.qunar.qchat.dao.model.NotificationInfo;
import com.qunar.qchat.utils.JacksonUtils;
import com.qunar.qchat.utils.ProtoMessageOuterClass;
import com.eatthepath.pushy.apns.util.SimpleApnsPayloadBuilder;
import com.eatthepath.pushy.apns.ApnsClient;
import com.eatthepath.pushy.apns.ApnsClientBuilder;
import com.eatthepath.pushy.apns.DeliveryPriority;
import com.eatthepath.pushy.apns.PushNotificationResponse;
import com.eatthepath.pushy.apns.util.ApnsPayloadBuilder;
import com.eatthepath.pushy.apns.util.SimpleApnsPushNotification;
import com.eatthepath.pushy.apns.util.TokenUtil;
import com.eatthepath.pushy.apns.util.concurrent.PushNotificationFuture;
import com.eatthepath.pushy.apns.auth.ApnsSigningKey;

import io.netty.handler.codec.http2.Http2FrameLogger;
import io.netty.handler.logging.LogLevel;
import org.apache.http.util.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLException;
import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.time.Instant;

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


            client = serviceMap.getOrDefault(notificationInfo.pkgname, null);
            String cert = "";
            String pwd = "";
            String key = "";
            String teamId = "";
            String keyId = "";
//            获取证书
            if (qIosPushServer.isBeta(notificationInfo.pkgname)) {
                if (qIosPushServer.getBetaCert(notificationInfo.pkgname) != null) {
                    cert = qIosPushServer.getBetaCert(notificationInfo.pkgname).certPath;
                    pwd = qIosPushServer.getBetaCert(notificationInfo.pkgname).certPwd;
                }
            } else if (qIosPushServer.isProd(notificationInfo.pkgname)) {
                if (qIosPushServer.getProCert(notificationInfo.pkgname) != null) {
                    if (qIosPushServer.getProCert(notificationInfo.pkgname).certPath != null) {
                        cert = qIosPushServer.getProCert(notificationInfo.pkgname).certPath;
                        pwd = qIosPushServer.getProCert(notificationInfo.pkgname).certPwd;
                    } else if (qIosPushServer.getProCert(notificationInfo.pkgname).token != null) {
                        key = qIosPushServer.getProCert(notificationInfo.pkgname).token;
                        teamId = qIosPushServer.getProCert(notificationInfo.pkgname).teamId;
                        keyId = qIosPushServer.getProCert(notificationInfo.pkgname).tokenId;
                        LOGGER.info("ios push 证书  key={} teamid={} keyid={}", key, teamId, keyId);
                    }
                }
            }
            if ((TextUtils.isEmpty(cert) || TextUtils.isEmpty(pwd)) && (TextUtils.isEmpty(key) || TextUtils.isEmpty(keyId) || TextUtils.isEmpty(teamId))) {
                if ((TextUtils.isEmpty(cert) || TextUtils.isEmpty(pwd))) {
                    LOGGER.info("ios push 没找到证书  cert={} pwd={} info={}", cert, pwd, JSON.toJSONString(notificationInfo));
                } else {
                    LOGGER.info("ios push 没找到证书  key={} keyId={} teamId={} info={}", key, keyId, teamId, JSON.toJSONString(notificationInfo));
                }
                return;
            } else {
                LOGGER.info("ios push 证书  cert={} pwd={} info ={}", cert, pwd, JSON.toJSONString(notificationInfo));
            }
            if (client == null) {
                try {
                    if (qIosPushServer.isBeta(notificationInfo.pkgname)) {
                        LOGGER.debug("测试环境");
                        client = new ApnsClientBuilder()
                                .setApnsServer(ApnsClientBuilder.DEVELOPMENT_APNS_HOST)
                                .setClientCredentials(new File(cert), pwd)
                                .build();
                    } else if (qIosPushServer.isProd(notificationInfo.pkgname)) {
//                        p8token验证
                        if (!TextUtils.isEmpty(Config.IOS_PUSH_AUTH_TOKEN_FILE)) {
                            try {
                                // 创建pushy客户端
                                client = new ApnsClientBuilder()
                                        .setApnsServer(ApnsClientBuilder.PRODUCTION_APNS_HOST)
                                        .setSigningKey(ApnsSigningKey.loadFromPkcs8File(new File(key), teamId, keyId))
                                        .setConcurrentConnections(4)
                                        // You need modify the code to import cert of X509Certificate
//                                        .setTrustedServerCertificateChain(new File(Config.class.getClassLoader().getResource(Config.IOS_TRUSTED_AAA_CERT).getPath()))
//                                        .setTrustedServerCertificateChain(new File(Config.class.getClassLoader().getResource(Config.IOS_TRUSTED_GEO_CERT).getPath()))
                                        .setFrameLogger(new Http2FrameLogger(LogLevel.DEBUG))
                                        .build();
                                LOGGER.info("apns客户端生成成功");

                            } catch (final SSLException | IllegalStateException e) {
                                LOGGER.error("客户端实例失败", e);
                                e.printStackTrace();
                            }
                        } else if (!TextUtils.isEmpty(Config.IOS_PUSH_CERT_PWD)) {
                            client = new ApnsClientBuilder()
                                    .setApnsServer(ApnsClientBuilder.PRODUCTION_APNS_HOST)
                                    .setClientCredentials(new File(cert), pwd)
                                    .build();
                            LOGGER.info("apns客户端生成成功");
                        }
                        LOGGER.error("发现线上环境...");


                    } else {
                        LOGGER.info("无法获取prod 或者 beta, pkgname:{}", notificationInfo.pkgname);
                        LOGGER.info("service map {}", qIosPushServer.toString());
                    }
                } catch (final Exception e) {
                    LOGGER.error("客户端实例失败", e);
                    e.printStackTrace();
                }
            }
            LOGGER.info("客户端实例完成");

            if (client == null) {
                LOGGER.info("ios push 不存在该bundleid  info ={}", JSON.toJSONString(notificationInfo));
                return;
            }
//            final String fcert = cert;
            serviceMap.put(notificationInfo.pkgname, client);

            final ApnsPayloadBuilder payloadBuilder = new SimpleApnsPayloadBuilder();
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
            LOGGER.debug("准备发送push");
            String payload = payloadBuilder.build();

            SimpleApnsPushNotification pushNotification = new SimpleApnsPushNotification(token, notificationInfo.pkgname,
                    payload, Instant.now().plus(SimpleApnsPushNotification.DEFAULT_EXPIRATION_PERIOD),
                    DeliveryPriority.IMMEDIATE, notificationInfo.messageId);
            final PushNotificationFuture<SimpleApnsPushNotification, PushNotificationResponse<SimpleApnsPushNotification>>
                    sendNotificationFuture = client.sendNotification(pushNotification);
            LOGGER.info("发送成功，等待服务器相应");

            try {
                final PushNotificationResponse<SimpleApnsPushNotification> pushNotificationResponse =
                        sendNotificationFuture.get();

                if (pushNotificationResponse.isAccepted()) {
                    LOGGER.info("Push notification accepted by APNs gateway.");
                } else {
                    LOGGER.info("Notification rejected by the APNs gateway: " +
                            pushNotificationResponse.getRejectionReason());
                    pushNotificationResponse.getTokenInvalidationTimestamp().ifPresent(timestamp -> {
                        LOGGER.info("\t…and the token is invalid as of " + timestamp);
                    });
                }
            } catch (final Exception e) {
                LOGGER.error("Failed to send push notification.");
                e.printStackTrace();
            }
        } catch (Exception e) {
            LOGGER.error("IosNewPushService Exception={} msg={} ", e, JacksonUtils.obj2String(notificationInfo));
            e.printStackTrace();

        }
    }


}
