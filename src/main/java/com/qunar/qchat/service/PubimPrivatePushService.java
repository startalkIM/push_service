package com.qunar.qchat.service;

import com.alibaba.fastjson.JSON;
import com.qunar.qchat.constants.Config;
import com.qunar.qchat.dao.model.NotificationInfo;
import com.qunar.qchat.utils.HttpClientUtils;
import com.qunar.qchat.utils.JacksonUtils;
import com.qunar.qchat.utils.ProtoMessageOuterClass;
import com.qunar.qchat.utils.QtalkStringUtils;
import com.qunar.qtalk.ss.common.utils.watcher.QMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * create by hubo.hu (lex) at 2018/8/9
 */
@Service
public class PubimPrivatePushService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PubimPrivatePushService.class);

    public void sendPush(NotificationInfo info) {
        String F;
        String B;

        if(info.type == ProtoMessageOuterClass.SignalType.SignalTypeGroupChat_VALUE){
            F = info.title + "/" + info.fromNick;
            B = info.title + "/" + info.fromNick + ":" + info.description;
        } else {
            F = info.fromName;
            B = info.title + ":" + info.description;
        }

        Map<String, Object> map = new HashMap<>();
        map.put("From", F);//发送人/群
        map.put("To", QtalkStringUtils.parseId(info.toUserName));//接收人
        map.put("Body", B);//消息内容(单人【消息内容】；群【说话人：消息内容】)
        map.put("Mtype", info.msg_type);//消息类型
        map.put("Message", info.msgxml);//原始消息

        String res = HttpClientUtils.postJson(Config.PRIVATE_PUSH_URL, JSON.toJSONString(map));
        LOGGER.info("pubimprivate push send result={} type={} notifInfo={} mapjson={} touser={}", res, info.type, JacksonUtils.obj2String(info), JSON.toJSONString(map), info.toUserName);
        QMonitor.recordOne("send_pubimprivate_message");
    }

}
