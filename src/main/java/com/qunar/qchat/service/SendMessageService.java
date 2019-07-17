package com.qunar.qchat.service;

import com.alibaba.fastjson.JSON;
import com.qunar.qchat.constants.Config;
import com.qunar.qchat.dao.model.NotificationInfo;
import com.qunar.qchat.utils.HttpClientUtils;
import com.qunar.qchat.utils.JacksonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * create by hubo.hu (lex) at 2019/1/16
 */
@Service
public class SendMessageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SendMessageService.class);

    public void sendMessage(NotificationInfo notificationInfo) {
        notificationInfo.push_key = Config.QTALK_PUSH_KEY;
        String res = HttpClientUtils.postJson(Config.QTALK_PUSH_URL, JSON.toJSONString(notificationInfo));
        LOGGER.info("startalk push send result={} notifInfo={} mapjson={} touser={}", res, JacksonUtils.obj2String(notificationInfo), notificationInfo.toUserName);
    }

}
