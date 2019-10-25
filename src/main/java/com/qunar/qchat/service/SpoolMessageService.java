package com.qunar.qchat.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.qunar.qchat.adrpushserver.*;
import com.qunar.qchat.constants.AdrPushConstants;
import com.qunar.qchat.constants.Config;
import com.qunar.qchat.constants.MessageSettingsTag;
import com.qunar.qchat.constants.MessageType;
import com.qunar.qchat.dao.model.NotificationInfo;
import com.qunar.qchat.dao.model.PushInfo;
import com.qunar.qchat.utils.*;
import org.apache.http.util.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lffan.liu on 2018/1/23.
 */
@Service
public class SpoolMessageService {

    @Autowired
    private AndroidPushService androidPushService;
    @Autowired
    private IosNewPushService iosPushService;

    @Autowired
    private PubimPrivatePushService pubimPrivatePushService;

    @Autowired
    private PushInfoServiceImpl pushInfoService;
    @Autowired
    DispatchService mDispatchService;

    @Autowired
    SendMessageService sendMessageService;

    Map<String, Integer> mapPartitions = new HashMap<String, Integer>();

    private static final Logger LOGGER = LoggerFactory.getLogger(SpoolMessageService.class);


    static {
        //mipush and third
        if (!TextUtils.isEmpty(Config.QT_PACKAGE_NAME) && !TextUtils.isEmpty(Config.MIPUSH_QT_SECRET_KEY)) {
            QPushServer.addPushServerManager(new MiPushServerManager(Config.QT_PACKAGE_NAME, Config.MIPUSH_QT_SECRET_KEY));
            QPushServer.addPushServerManager(new ThirdPushServerManager(Config.QT_PACKAGE_NAME, Config.MIPUSH_QT_SECRET_KEY, AdrPushConstants.NAME_THIRD));
        }
        //hwpush
        if (!TextUtils.isEmpty(Config.QT_PACKAGE_NAME) && !TextUtils.isEmpty(Config.HWPUSH_QT_SECRET_KEY)) {
            QPushServer.addPushServerManager(new HWPushServerManager(Config.QT_PACKAGE_NAME, Config.HWPUSH_QT_SECRET_KEY));
        }
        //oppo push
        if (!TextUtils.isEmpty(Config.QT_PACKAGE_NAME) && !TextUtils.isEmpty(Config.OPUSH_QT_SECRET_KEY)) {
            QPushServer.addPushServerManager(new OPushServerManager(Config.QT_PACKAGE_NAME, Config.OPUSH_QT_SECRET_KEY));
        }
        //mzpush
        if (!TextUtils.isEmpty(Config.QT_PACKAGE_NAME) && !TextUtils.isEmpty(Config.MZPUSH_QT_SECRET_KEY)) {
            QPushServer.addPushServerManager(new MZPushServerManager(Config.QT_PACKAGE_NAME, Config.MZPUSH_QT_SECRET_KEY));
        }
        //vivo
        if (!TextUtils.isEmpty(Config.VPUSH_QT_APP_SECRET_KEY) && !TextUtils.isEmpty(Config.VPUSH_QT_APP_KEY) && Config.VPUSH_QT_APP_ID > 0) {
            QPushServer.addPushServerManager(new VivoPushServerManager(Config.QT_PACKAGE_NAME, Config.VPUSH_QT_APP_ID, Config.VPUSH_QT_APP_KEY, Config.VPUSH_QT_APP_SECRET_KEY));
        }
        //fcm
        if (!TextUtils.isEmpty(Config.FCMPUSH_QT_SERVER_KEY)) {
            QPushServer.addPushServerManager(new FCMPushServerManager(Config.QT_PACKAGE_NAME, Config.FCMPUSH_QT_SERVER_KEY));
        }
    }


    public void processChatMessage(String key, Map<String, Object> chatMessage) {
        try {
            if (chatMessage != null) {
                if (MessageType.GROUPCHAT.equals(key)
                        || (MessageType.REVOKE.equalsIgnoreCase(key) && MessageType.GROUPCHAT.equalsIgnoreCase(chatMessage.get("subtype").toString()))) {
                    List<String> toUsers = null;
                    if (chatMessage.containsKey("userlist")) {
                        toUsers = (List<String>) chatMessage.get("userlist");
                    }
                    if (toUsers == null) return;

                    for (int i = 0; i < toUsers.size(); i++) {
                        String user = toUsers.get(i);
                        String toUser = QtalkStringUtils.parseId(user);
                        String toDomain = QtalkStringUtils.parseDomain(user);
                        try {
                            getMesaageToSend(key, chatMessage, toUser, toDomain);
                        } catch (Exception e) {
                            LOGGER.error("processChatMessage group continue Exception={} msg={}", e, JSON.toJSONString(chatMessage));
                            continue;
                        }
                    }
                } else {
                    String toUser = "";
                    String toDomain = "";
                    if (chatMessage.containsKey("m_to")) {
                        toUser = chatMessage.get("m_to").toString();
                    }
                    if (chatMessage.containsKey("to_host")) {
                        toDomain = chatMessage.get("to_host").toString();
                    }

                    getMesaageToSend(key, chatMessage, toUser, toDomain);
                }

            }

        } catch (Exception e) {
            LOGGER.error("processChatMessage Exception={} msg={}", e, JSON.toJSONString(chatMessage));
        }
    }

    private void getMesaageToSend(String type, Map<String, Object> chatMessage, String toUser, String toDomain) {
        try {
            PushInfo info = null;
            //获取push_info
            int status = MessageSettingsTag.DEFAULT_ALL;
            String pushinfocache = pushInfoService.cachePushInfo(toUser, toDomain);
            if (!TextUtils.isEmpty(pushinfocache)) {
                try {
                    info = JSON.parseObject(pushinfocache, PushInfo.class);
                } catch (Exception e) {
                    LOGGER.error("防止缓存异常数据 Exception  info={} e={}", pushinfocache, e.getMessage());
                    pushInfoService.clearPushinfoCache(toUser, toDomain);
                    pushinfocache = pushInfoService.cachePushInfo(toUser, toDomain);
                    if (!TextUtils.isEmpty(pushinfocache)) {
                        info = JSON.parseObject(pushinfocache, PushInfo.class);
                    }
                }
            }
            if (info != null) {
                status = Integer.valueOf(info.getPush_flag());
                LOGGER.info("processChatMessage PushInfo={} touser={}", JSON.toJSONString(info), QtalkStringUtils.userId2Jid(toUser, toDomain));
            } else {
                info = new PushInfo();
                LOGGER.info("processChatMessage push no regist touser={}", QtalkStringUtils.userId2Jid(toUser, toDomain));
            }
            //获取push_info end
            //push开关
            if (!MessageSettingsTag.isExistTag(status, MessageSettingsTag.PUSH_SWITCH)) {
                LOGGER.info("processChatMessage push_switch={} touser={}", false, QtalkStringUtils.userId2Jid(toUser, toDomain));
                return;
            }
            //解析消息
            NotificationInfo notificationInfo = formatBaseNotification(type, chatMessage, toUser, toDomain, info);
            if (notificationInfo == null) {
                LOGGER.info("processChatMessage notificationInfo={} touser={}", "", QtalkStringUtils.userId2Jid(toUser, toDomain));
                LOGGER.info("processChatMessage 解析消息异常 touser={}", QtalkStringUtils.userId2Jid(toUser, toDomain));
                return;
            }
            //push不显示明文
            if (!MessageSettingsTag.isExistTag(status, MessageSettingsTag.SHOW_CONTENT)) {
                notificationInfo.title = "Startalk";
                notificationInfo.description = ChatTextUtils.getContentByKeyAndLang("msg.notification.defaultcontent", info.getPlatname());
                notificationInfo.body = ChatTextUtils.getContentByKeyAndLang("msg.notification.defaultcontent", info.getPlatname());;
            }
            //是否@自己
            boolean isAtMyself = isAtMyself(type, chatMessage, toUser, toDomain, notificationInfo);
//            boolean isHasAnyAway = false;
            boolean isHasOtherPlatOnline = false;
            boolean isSubsript = false;
            if (!isAtMyself) {
                //未订阅不通知
                isSubsript = mDispatchService.getServiceByDomain(toDomain).isSubscribGroup(toUser, toDomain, notificationInfo.fromjid);
                if (!isSubsript) {
                    LOGGER.info("processChatMessage 群消息未订阅 isSubscribGroup={} touser={}", isSubsript, QtalkStringUtils.userId2Jid(toUser, toDomain));
                    return;
                }

                if (!MessageSettingsTag.isExistTag(status, MessageSettingsTag.PUSH_ONLINE)) {
//                    isHasAnyAway = CommonRedisUtil.isHasPlatAway(toUser, toDomain);
                    isHasOtherPlatOnline = CommonRedisUtil.isHasPlatOnline(toUser, toDomain);
                    //modify 20181219 在线push开关关闭情况下，在线就不发push，不判离开状态
                    if (isHasOtherPlatOnline) {//没有离开&有在线的不发
                        LOGGER.info("processChatMessage isAtMyself={} isHasOtherPlatOnline={} touser={}", isAtMyself, isHasOtherPlatOnline, QtalkStringUtils.userId2Jid(toUser, toDomain));
                        return;
                    }
                }

            }
//        LOGGER.info("isAtMyself={} isHasAnyAway={} isHasOtherPlatOnline={} touser={}", isAtMyself, isHasAnyAway, isHasOtherPlatOnline, toDomain);

            if (!TextUtils.isEmpty(Config.PRIVATE_PUSH_URL)) {
                pubimPrivatePushService.sendPush(notificationInfo);
                return;
            }

            NotificationInfo mnotificationInfo = formatNotification(info, notificationInfo);
            if (mnotificationInfo == null) {
                LOGGER.info("processChatMessage notificationInfo={} isAtMyself={} isHasOtherPlatOnline={} isSubsript={} touser={}", notificationInfo.toString(), isAtMyself, isHasOtherPlatOnline, isSubsript, QtalkStringUtils.userId2Jid(toUser, toDomain));
                return;
            } else {
                mnotificationInfo.os = info.getOs();
                LOGGER.info("processChatMessage mnotificationInfo={} isAtMyself={} isHasOtherPlatOnline={} isSubsript={} touser={}", mnotificationInfo.toString(), isAtMyself, isHasOtherPlatOnline, isSubsript, QtalkStringUtils.userId2Jid(toUser, toDomain));
            }

            if(!TextUtils.isEmpty(Config.QTALK_PUSH_URL) && !TextUtils.isEmpty(Config.QTALK_PUSH_KEY)) {
                sendMessageService.sendMessage(mnotificationInfo);
            } else {
                sendMessagePush(mnotificationInfo);
            }

        } catch (Exception e) {
            LOGGER.error("getMesaageToSend Exception={} msg={} touser={}", e, JSON.toJSONString(chatMessage), QtalkStringUtils.userId2Jid(toUser, toDomain));
        }
    }

    /**
     * 发送push
     * @param mnotificationInfo 通知消息对戏
     */
    public void sendMessagePush(NotificationInfo mnotificationInfo) {
        if ("ios".equalsIgnoreCase(mnotificationInfo.os)) {
            iosPushService.iosPush(mnotificationInfo);
        } else if ("android".equalsIgnoreCase(mnotificationInfo.os)) {
            //目前小米支持撤销消息类型
            if (MessageType.REVOKE.equalsIgnoreCase(mnotificationInfo.originType)) {
                if (mnotificationInfo.platname.equalsIgnoreCase(AdrPushConstants.NAME_HUAWEI)
                        || mnotificationInfo.platname.equalsIgnoreCase(AdrPushConstants.NAME_MEIZU)) {
                    return;
                }
            }

            androidPushService.notifyPushMesg(mnotificationInfo);
        }
        if("qunar-message".equalsIgnoreCase(QtalkStringUtils.parseId(mnotificationInfo.fromjid))) {
        } else if("ivrmsg".equalsIgnoreCase(QtalkStringUtils.parseId(mnotificationInfo.fromjid))) {
        }
    }

    private boolean isMackeyValid(String mackey) {
        if (TextUtils.isEmpty(mackey)
                || "(null)".equalsIgnoreCase(mackey)
                || mackey.contains("null")) {
            return false;
        }
        return true;
    }

    private boolean isAtMyself(String type, Map<String, Object> chatMessage, String toUser, String toDomain, NotificationInfo notificationInfo) {
        boolean isAtMyself = false;
        //代收消息如果是群消息并且@自己，则单独处理
        if (notificationInfo.type == ProtoMessageOuterClass.SignalType.SignalTypeCollection_VALUE) {
            String xml = chatMessage.get("m_body").toString();
            Map<String, Object> msgxml = Xml2Json.xmppToMap(xml);
            Map<String, Object> messagexml = (Map<String, Object>) msgxml.get("message");

            String origintype = messagexml.get("origintype").toString();
            if (MessageType.GROUPCHAT.equals(origintype)) {
                String originfrom = messagexml.get("originfrom").toString();
                String originto = messagexml.get("originto").toString();

                if (originfrom.contains("/")) {
                    originfrom = originfrom.split("/")[0];
                }
//                String orfid = QtalkStringUtils.parseId(originfrom);
//                String orfhost = QtalkStringUtils.parseDomain(originfrom);
//                    if(notificationInfo.msg_type != ProtoMessageOuterClass.MessageType.MessageTypeGroupAt_VALUE){//@=realto消息发push
//                        return;
//                    }
                Map<String, Object> bodyxml = (Map<String, Object>) msgxml.get("body");
                if (bodyxml.containsKey("backupinfo") && !TextUtils.isEmpty(bodyxml.get("backupinfo").toString())) {
                    String ext = bodyxml.get("backupinfo").toString();
                    List<Map<String, String>> atInfo = JSON.parseObject(ext, new TypeReference<ArrayList<Map<String, String>>>() {
                    });
                    for (Map<String, String> map : atInfo) {
                        if ("10001".equalsIgnoreCase(map.get("type"))) {
                            List<Map<String, String>> atlist = JSON.parseObject(map.get("data"), new TypeReference<ArrayList<Map<String, String>>>() {
                            });
                            for (Map<String, String> m : atlist) {
                                if (originto.equalsIgnoreCase(m.get("jid"))) {
                                    isAtMyself = true;
                                    break;
                                }
                            }
                            if (isAtMyself) {
                                break;
                            }
                        }
                    }
                }
            }
        } else {
            if ((MessageType.GROUPCHAT.equals(type))) {
                if (notificationInfo.msg_type == ProtoMessageOuterClass.MessageType.MessageTypeGroupAt_VALUE) {//@=realto消息发push
                    String xml = chatMessage.get("packet").toString();
                    Map<String, Object> msgxml = Xml2Json.xmppToMap(xml);

                    Map<String, Object> bodyxml = (Map<String, Object>) msgxml.get("body");
                    if (bodyxml.containsKey("backupinfo") && !TextUtils.isEmpty(bodyxml.get("backupinfo").toString())) {
                        String ext = bodyxml.get("backupinfo").toString();

                        try {
                            List<Map<String, String>> atInfo = JSON.parseObject(ext, new TypeReference<ArrayList<Map<String, String>>>() {
                            });
                            for (Map<String, String> map : atInfo) {
                                if ("10001".equalsIgnoreCase(map.get("type"))) {
                                    List<Map<String, String>> atlist = JSON.parseObject(map.get("data"), new TypeReference<ArrayList<Map<String, String>>>() {
                                    });
                                    for (Map<String, String> m : atlist) {
                                        if (QtalkStringUtils.userId2Jid(toUser, toDomain).equalsIgnoreCase(m.get("jid"))) {
                                            isAtMyself = true;
                                            break;
                                        }
                                    }
                                    if (isAtMyself) {
                                        break;
                                    }
                                }
                            }
                        } catch (Exception e) {
                            Map<String, String> map = JSON.parseObject(ext, new TypeReference<Map<String, String>>() {
                            });
                            if ("10001".equalsIgnoreCase(map.get("type"))) {
                                List<Map<String, String>> atlist = JSON.parseObject(map.get("data"), new TypeReference<ArrayList<Map<String, String>>>() {
                                });
                                for (Map<String, String> m : atlist) {
                                    if (QtalkStringUtils.userId2Jid(toUser, toDomain).equalsIgnoreCase(m.get("jid"))) {
                                        isAtMyself = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }
        return isAtMyself;
    }

    private NotificationInfo formatNotification(PushInfo pushInfo, NotificationInfo notificationInfo) {
        if (pushInfo != null && isMackeyValid(pushInfo.getMac_key())) {
            String platkey = pushInfo.getMac_key();
            String platname = pushInfo.getPlatname();
            String pkgname = pushInfo.getPkgname();
            //发送指定账号
            List<String> alias = new ArrayList<>();
            alias.add(platkey);

            notificationInfo.platkeys = alias;
            notificationInfo.platname = platname;
            notificationInfo.pkgname = pkgname;
            return notificationInfo;
        }
        return null;
    }

    private NotificationInfo formatBaseNotification(String key, Map<String, Object> chatMessage, String toUser, String toDomain, PushInfo info) {
        try {
            NotificationInfo notificationInfo = new NotificationInfo();
            String from = "";
            String fromhost = "";
            String xml = "";
            String fromName = "";
            if (MessageType.GROUPCHAT.equals(key)) {
                if (chatMessage.get("muc_room_name") != null) {
                    from = chatMessage.get("muc_room_name").toString();
                }
                if (chatMessage.get("room_host") != null) {
                    fromhost = chatMessage.get("room_host").toString();
                }
                if (chatMessage.get("packet") != null) {
                    xml = chatMessage.get("packet").toString();
                }
            } else {
                if (chatMessage.get("m_from") != null) {
                    from = chatMessage.get("m_from").toString();
                }
                if (chatMessage.get("from_host") != null) {
                    fromhost = chatMessage.get("from_host").toString();
                }
                if (chatMessage.get("m_body") != null) {
                    xml = chatMessage.get("m_body").toString();
                }
            }

            Map<String, Object> msgxml = Xml2Json.xmppToMap(xml);
            Map<String, Object> messagexml = (Map<String, Object>) msgxml.get("message");
            Map<String, Object> body = (Map<String, Object>) msgxml.get("body");
            String messageid = body.get("id").toString();
            String type = "";
            String chatid = "";
            String realjid = "";
            //自动回复消息不发push
            if (messagexml.containsKey("auto_reply") && Boolean.valueOf(messagexml.get("auto_reply").toString())) {
                return null;
            }
            if (messagexml.get("type") != null) {
                type = messagexml.get("type").toString();
            }
            String message = "";
            if (body.get("content") != null) {
                message = body.get("content").toString();
            }

            int msg_type = 0;
            if (body.get("msgType") != null) {
                msg_type = Integer.valueOf(body.get("msgType").toString());
            }
            if (MessageType.GROUPCHAT.equals(type) || (MessageType.REVOKE.equalsIgnoreCase(type) && MessageType.GROUPCHAT.equalsIgnoreCase(chatMessage.get("subtype").toString()))) {
                if (messagexml.containsKey("to") && messagexml.get("to") != null) {
                    fromName = messagexml.get("to").toString();
                } else {
                    fromName = from + "@" + fromhost;
                }
            } else {
                fromName = QtalkStringUtils.userId2Jid(from, fromhost);
            }

            String notifyid = fromName;
            String cctext = "";
            if (messagexml.containsKey("cctext") && messagexml.get("cctext") != null) {
                cctext = messagexml.get("cctext").toString();
            }
            String toUserName = QtalkStringUtils.userId2Jid(toUser, toDomain);
            String fromNick = "";
            if (MessageType.GROUPCHAT.equals(key)) {
                if (chatMessage.containsKey("nick") && chatMessage.get("nick") != null) {
                    fromNick = chatMessage.get("nick").toString();
                }
            } /*else if (MessageType.CHAT.equals(key)) {
            } */

            //根据msgtype转换文字
            message = ChatTextUtils.showContentType(message, msg_type, info);
            if (body.get("extendInfo") != null) {
                String extendInfo = body.get("extendInfo").toString();
                if (!TextUtils.isEmpty(extendInfo)) {
                    try {

                        Map<String, String> extInfo = JSON.parseObject(extendInfo, new TypeReference<Map<String, String>>() {});
                        if (extInfo.containsKey("pushtext") && extInfo.get("pushtext") != null) {
                            //有pushtext字段显示pushtext字段
                            message = extInfo.get("pushtext").toString();
                        }/* else {
                        message = message + extInfo.get("title").toString();
                    }*/
                    } catch (Exception e) {
                        LOGGER.info("formatBaseNotification extendInfo parse error={} from={} touser={}", JSON.toJSON(chatMessage), from, QtalkStringUtils.userId2Jid(toUser, toDomain));
                    }
                }
            }
            if ((msg_type == ProtoMessageOuterClass.MessageType.MessageTypeRobotTurnToUser_VALUE
                    || msg_type == ProtoMessageOuterClass.MessageType.MessageTypeRobotQuestionList_VALUE)
                    && fromhost.equalsIgnoreCase("ejabhost2")) {
                //qchat ejabhost2 展示机器人消息
                message = ChatTextUtils.getContentByKeyAndLang("msg.notification.rbtmsg", info.getPlatname());
            }
            if (MessageType.COLLECTION.equalsIgnoreCase(type)) {//代收消息
                String orginto = "";
                if (messagexml.get("originto") != null) {
                    orginto = messagexml.get("originto").toString();
                }
                fromName = "代收账号：" + orginto;
                String originfrom = "";
                if (messagexml.get("originfrom") != null) {
                    originfrom = messagexml.get("originfrom").toString();
                }
                if (!TextUtils.isEmpty(originfrom) && originfrom.contains("/")) {
                    originfrom = originfrom.split("/")[0];
                }
                String orfid = QtalkStringUtils.parseId(originfrom);
                String orfhost = QtalkStringUtils.parseDomain(originfrom);
                String origintype = "";
                if (messagexml.get("origintype") != null) {
                    origintype = messagexml.get("origintype").toString();
                }
                notifyid = originfrom;//QtalkStringUtils.userId2Jid(originfrom, originserver);
                if (MessageType.CHAT.equalsIgnoreCase(origintype)) {
                    String tempName = mDispatchService.getServiceByDomain(orfhost).getUserName(orfid, orfhost);
                    message = TextUtils.isEmpty(tempName) ? QtalkStringUtils.parseId(fromName) : tempName + " : " + message;
                } else if (MessageType.GROUPCHAT.equalsIgnoreCase(origintype)) {
                    String tempName = mDispatchService.getServiceByDomain(orfhost).getMucName(originfrom);
//                    String originnick = chatMessage.get("originnick").toString();
                    message = TextUtils.isEmpty(tempName) ? QtalkStringUtils.parseId(originfrom) : tempName + " : " + message;
                }
            } else if (MessageType.CONSULT.equalsIgnoreCase(type)) {
                String realfrom = "";
                String cfrom = "";
                if (messagexml.get("qchatid") != null) {
                    chatid = messagexml.get("qchatid").toString();
                }
                if (messagexml.get("from") != null) {
                    cfrom = messagexml.get("from").toString();
                }
                if (messagexml.get("realfrom") != null) {
                    realfrom = messagexml.get("realfrom").toString();
                }
                realjid = realfrom;
                String cfromname = mDispatchService.getServiceByDomain(QtalkStringUtils.parseDomain(cfrom))
                        .getUserName(QtalkStringUtils.parseId(cfrom), QtalkStringUtils.parseDomain(cfrom));
                cfromname = TextUtils.isEmpty(cfromname) ? QtalkStringUtils.parseId(cfrom) : cfromname;
                String realfromname = mDispatchService.getServiceByDomain(QtalkStringUtils.parseDomain(realfrom))
                        .getUserName(QtalkStringUtils.parseId(realfrom), QtalkStringUtils.parseDomain(realfrom));
                realfromname = TextUtils.isEmpty(realfromname) ? QtalkStringUtils.parseId(realfrom) : realfromname;
                if ("4".equalsIgnoreCase(chatid)) {
                    fromName = cfromname + "-" + realfromname;
                } else if ("5".equalsIgnoreCase(chatid)) {
                    fromName = cfromname;
                }
                notifyid = cfrom;
            } else if (MessageType.HEADLINE.equalsIgnoreCase(type) || "subscription".equalsIgnoreCase(type)) {
                fromName = ChatTextUtils.getContentByKeyAndLang("msg.notification.system", info.getPlatname());
            } else {
                if (MessageType.CHAT.equals(type) || (MessageType.REVOKE.equalsIgnoreCase(key) && MessageType.CHAT.equalsIgnoreCase(chatMessage.get("subtype").toString()))) {
                    String tempName = mDispatchService.getServiceByDomain(fromhost).getUserName(from, fromhost);
                    fromName = TextUtils.isEmpty(tempName) ? QtalkStringUtils.parseId(fromName) : tempName;
                } else if (MessageType.GROUPCHAT.equals(type)) {
                    String tempName = mDispatchService.getServiceByDomain(QtalkStringUtils.parseDomain(fromName)).getMucName(fromName);
                    fromName = TextUtils.isEmpty(tempName) ? QtalkStringUtils.parseId(fromName) : tempName;
                    String realfrom = "";
                    if (messagexml.containsKey("sendjid") && messagexml.get("sendjid") != null) {
                        realfrom = messagexml.get("sendjid").toString();
                    } else if (messagexml.containsKey("realfrom") && messagexml.get("realfrom") != null) {
                        realfrom = messagexml.get("realfrom").toString();
                    }
                    if (!TextUtils.isEmpty(realfrom)) {
                        if (realfrom.equalsIgnoreCase(QtalkStringUtils.userId2Jid("admin", toDomain))
                                && msg_type == ProtoMessageOuterClass.MessageType.MessageTypeGroupNotify_VALUE) {
                            //群通知类型消息不发送推送push
                            return null;
                        }
                        if (realfrom.equalsIgnoreCase(QtalkStringUtils.userId2Jid(toUser, toDomain))) {
                            //不给自己推送消息
                            return null;
                        }
                        String temprealfrom = mDispatchService.getServiceByDomain(QtalkStringUtils.parseDomain(realfrom)).getUserName(QtalkStringUtils.parseId(realfrom), QtalkStringUtils.parseDomain(realfrom));
                        realfrom = TextUtils.isEmpty(temprealfrom) ? QtalkStringUtils.parseId(realfrom) : temprealfrom;
                        message = realfrom + " : " + message;
                    }
                } else if (MessageType.REVOKE.equalsIgnoreCase(key) && MessageType.GROUPCHAT.equalsIgnoreCase(chatMessage.get("subtype").toString())) {
                    //群消息revoke，from是发消息人  to是群id
                    String realfrom = QtalkStringUtils.userId2Jid(from, fromhost);
//                    fromName = messagexml.get("to").toString();
                    String tempName = mDispatchService.getServiceByDomain(QtalkStringUtils.parseDomain(fromName)).getMucName(fromName);
                    fromName = TextUtils.isEmpty(tempName) ? QtalkStringUtils.parseId(fromName) : tempName;

                    if (!TextUtils.isEmpty(realfrom)) {
                        if (realfrom.equalsIgnoreCase(QtalkStringUtils.userId2Jid(toUser, toDomain))) {
                            //不给自己推送消息
                            return null;
                        }
                        String temprealfrom = mDispatchService.getServiceByDomain(QtalkStringUtils.parseDomain(realfrom)).getUserName(QtalkStringUtils.parseId(realfrom), QtalkStringUtils.parseDomain(realfrom));
                        realfrom = TextUtils.isEmpty(temprealfrom) ? QtalkStringUtils.parseId(realfrom) : temprealfrom;
                        message = realfrom + " : " + message;
                    }
                }
            }

            String json = "Just unused now";//JacksonUtils.obj2String(jsonMap);


            //title length limit is 32
            String title = fromName;
            if(!TextUtils.isEmpty(title) && title.length() > 32) {
                title = title.substring(0, 32);
            }
            notificationInfo.title = title;
            notificationInfo.description = message.length() > 50 ? message.substring(0, 50) : message;
            notificationInfo.fromjid = notifyid;
            notificationInfo.fromName = from;
            notificationInfo.fromHost = fromhost;
            notificationInfo.originType = key;
            notificationInfo.msg_type = msg_type;
            notificationInfo.type = QtalkStringUtils.getSignalType(type);
            notificationInfo.chatid = chatid;
            notificationInfo.realjid = realjid;
            notificationInfo.cctext = cctext;
            notificationInfo.body = message.length() > 50 ? message.substring(0, 50) : message;
            notificationInfo.msgxml = xml;
            notificationInfo.json = json;
            notificationInfo.toUserName = toUserName;
            notificationInfo.fromNick = fromNick;
            notificationInfo.messageId = messageid;
            return notificationInfo;
        } catch (Exception e) {
            LOGGER.error("formatBaseNotification Exception={} msg={}", e, JSON.toJSONString(chatMessage));
        }
        return null;
    }
}
