package com.qunar.qchat.adrpushserver;

import com.qunar.qchat.dao.model.NotificationInfo;
import com.qunar.qchat.service.SpoolMessageService;
import com.qunar.qchat.utils.JacksonUtils;
import com.qunar.qtalk.ss.common.utils.watcher.QMonitor;
import org.apache.http.util.TextUtils;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class QPushServer {
    private static List<QPushServerManager> managers = new ArrayList<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(SpoolMessageService.class);

    public static void addPushServerManager(QPushServerManager serverManager) {
        for (QPushServerManager item : managers) {
            // 避免重复添加
            if (item.getClass().equals(serverManager.getClass())) {
                return;
            }
        }
        managers.add(serverManager);
    }


    public static void sendMessageToAlias(String name, String pkgname, List<String> alias, String title, String messagePayload) throws IOException, ParseException {
        for (QPushServerManager item : managers) {
            if(item.getName().equals(name) && item.getPkgname().equals(pkgname)){
                item.sendMessageToAlias(alias, title, messagePayload);
            }
        }
    }


    public static void sendMessageToTags(String name, String pkgname, List<String> tags, String messagePayload) throws IOException, ParseException {
        for (QPushServerManager item : managers) {
            if(item.getName().equals(name) && item.getPkgname().equals(pkgname)){
                item.sendMessageToTags(tags, messagePayload);
            }
        }
    }

    public static void sendMessageToAll(String name, String pkgname, String messagePayload) throws IOException, ParseException {
        for (QPushServerManager item : managers) {
            if(item.getName().equals(name) && item.getPkgname().equals(pkgname)){
                item.sendMessageToAll(messagePayload);
            }
        }
    }

    public static void sendNotifyToAlias(NotificationInfo notificationInfo) throws IOException, ParseException {
        for (QPushServerManager item : managers) {
//            LOGGER.info("android sendNotifyToAlias item={} notificationInfo={} touser={}", item.toString(), JacksonUtils.obj2String(notificationInfo), notificationInfo.toUserName);
            if(!TextUtils.isEmpty(notificationInfo.platname) && notificationInfo.platname.contains(item.getName()) && item.getPkgname().equals(notificationInfo.pkgname)){
//                LOGGER.info("android push1 send notificationInfo={} touser={}", JacksonUtils.obj2String(notificationInfo), notificationInfo.toUserName);
                item.sendNotifyToAlias(notificationInfo);
                LOGGER.info("android push send notificationInfo={} touser={}", JacksonUtils.obj2String(notificationInfo), notificationInfo.toUserName);
                QMonitor.recordOne("send_adr_message");
            }
        }
    }


    public static void sendNotifyToTags(List<String> tags, String title, String description, String messagePayload) throws IOException, ParseException {
        for (QPushServerManager item : managers) {
            item.sendNotifyToTags(tags, title, description, messagePayload);
        }
    }

    public static void sendNotifyToAll(String name, String pkgname, String title, String description, String messagePayload) throws IOException, ParseException {
        for (QPushServerManager item : managers) {
            if(item.getName().equals(name) && item.getPkgname().equals(pkgname)){
                item.sendNotifyToAll(title, description, messagePayload);
            }
        }
    }


    public static void sendLinkNotifyToAlias(List<String> alias, String title, String description, String url) throws IOException, ParseException {
        for (QPushServerManager item : managers) {
            item.sendLinkNotifyToAlias(alias, title, description, url);
        }
    }

    public static void sendLinkNotifyToTags(List<String> tags, String title, String description, String url) throws IOException, ParseException {
        for (QPushServerManager item : managers) {
            item.sendLinkNotifyToTags(tags, title, description, url);
        }
    }

    public static void sendLinkNotifyToAll(String title, String description, String url) throws IOException, ParseException {
        for (QPushServerManager item : managers) {
            item.sendLinkNotifyToAll(title, description, url);
        }
    }


}
