package com.qunar.qchat.adrpushserver;


import com.qunar.qchat.dao.model.NotificationInfo;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.List;

public interface QPushServerManager {

    void sendMessageToAlias(List<String> alias, String title, String messagePayload) throws IOException, ParseException;

    void sendMessageToTags(List<String> tags, String messagePayload) throws IOException, ParseException;

    void sendMessageToAll(String messagePayload) throws IOException, ParseException;

    void sendNotifyToAlias(NotificationInfo notificationInfo) throws IOException, ParseException;

    void sendNotifyToTags(List<String> tags, String title, String description, String messagePayload) throws IOException, ParseException;

    void sendNotifyToAll(String title, String description, String messagePayload) throws IOException, ParseException;


    /**
     * 该通知栏消息是推送一个超链接，打开外部浏览器，不建议使用，建议使用普通通知栏消息实现，打开内容的浏览器，从而提高日活
     */
    @Deprecated
    void sendLinkNotifyToAlias(List<String> alias, String title, String description, String url) throws IOException, ParseException;

    /**
     * 该通知栏消息是推送一个超链接，打开外部浏览器，不建议使用，建议使用普通通知栏消息实现，打开内容的浏览器，从而提高日活
     */
    @Deprecated
    void sendLinkNotifyToTags(List<String> tags, String title, String description, String url) throws IOException, ParseException;

    /**
     * 该通知栏消息是推送一个超链接，打开外部浏览器，不建议使用，建议使用普通通知栏消息实现，打开内容的浏览器，从而提高日活
     */
    @Deprecated
    void sendLinkNotifyToAll(String title, String description, String url) throws IOException, ParseException;

    String getName();

    String getPkgname();
}
