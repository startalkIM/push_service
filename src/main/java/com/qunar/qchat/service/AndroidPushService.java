package com.qunar.qchat.service;

import com.qunar.qchat.adrpushserver.QPushServer;
import com.qunar.qchat.dao.model.NotificationInfo;
import com.qunar.qchat.utils.JacksonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Created by lffan.liu on 2018/1/23.
 */
@Service
public class AndroidPushService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AndroidPushService.class);

    /**
     */
    public void notifyPushMesg(NotificationInfo notificationInfo){
        try {
//            QPushServer.sendMessageToAlias(platname, pkgname, platkey, from, json);
            QPushServer.sendNotifyToAlias(notificationInfo);

        } catch (Exception e) {
            LOGGER.error("android push exception={} cnotificationInfo={} touser={}", e.getMessage(), JacksonUtils.obj2String(notificationInfo), notificationInfo.toUserName);
        }
    }

}
