package com.qunar.qchat.service;


public interface QVCardService {

    String getUserName(String from, String fromhost);

    String getMucName(String fromname);

    void delQimPubOldPushToken(String username, String domain, String mac_key, String os, String version);

    boolean isSubscribGroup(String username, String host, String mucname);
}
