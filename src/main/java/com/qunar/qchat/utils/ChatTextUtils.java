package com.qunar.qchat.utils;

import com.qunar.qchat.dao.model.PushInfo;
import org.apache.http.util.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatTextUtils {
    public static final int AUTO_REPLY_MESSAGE = 13;

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatTextUtils.class);


    private final static Map<Integer,String> defaultMsg = new HashMap<Integer,String>();

    private final static String objPattern = "\\[obj type=\"([\\w]+)\" value=\"([\\S]+)\"([\\w|=|\\s|\\.]+)?\\]";
    private final static Pattern compiledPattern = Pattern.compile(objPattern);

    static {
        defaultMsg.put(ProtoMessageOuterClass.MessageType.MessageTypeFile_VALUE, "msg.notification.file");
        defaultMsg.put(ProtoMessageOuterClass.MessageType.MessageTypeLocalShare_VALUE, "msg.notification.location");
        defaultMsg.put(ProtoMessageOuterClass.MessageType.MessageTypeTopic_VALUE, "msg.notification.newmsg");
        defaultMsg.put(ProtoMessageOuterClass.MessageType.MessageTypeActionRichText_VALUE, "msg.notification.rich");
        defaultMsg.put(ProtoMessageOuterClass.MessageType.MessageTypeRichText_VALUE, "msg.notification.rich");
        defaultMsg.put(ProtoMessageOuterClass.MessageType.MessageTypeNotice_VALUE, "msg.notification.notification");
        defaultMsg.put(ProtoMessageOuterClass.MessageType.MessageTypeSystem_VALUE, "msg.notification.system");
        defaultMsg.put(ProtoMessageOuterClass.MessageType.MessageTypeBurnAfterRead_VALUE, "msg.notification.burn");
        defaultMsg.put(ProtoMessageOuterClass.MessageType.MessageTypeSmallVideo_VALUE, "msg.notification.videomsg");
        defaultMsg.put(ProtoMessageOuterClass.MessageType.MessageTypeRobotAnswer_VALUE, "msg.notification.robotmsg");
        defaultMsg.put(ProtoMessageOuterClass.MessageType.MessageTypeVoice_VALUE, "msg.notification.voice");
        defaultMsg.put(ProtoMessageOuterClass.MessageType.MessageTypeShock_VALUE, "msg.notification.shake");
        defaultMsg.put(ProtoMessageOuterClass.MessageType.MessageTypeRedPack_VALUE, "msg.notification.redpkg");
        defaultMsg.put(ProtoMessageOuterClass.MessageType.MessageTypeRedPackInfo_VALUE, "msg.notification.redpkgnoc");
        defaultMsg.put(ProtoMessageOuterClass.MessageType.MessageTypeActivity_VALUE, "msg.notification.activity");
        defaultMsg.put(ProtoMessageOuterClass.MessageType.MessageTypeAA_VALUE, "msg.notification.aapay");
        defaultMsg.put(ProtoMessageOuterClass.MessageType.MessageTypeAAInfo_VALUE, "msg.notification.aapaynoc");
        defaultMsg.put(ProtoMessageOuterClass.MessageType.MessageTypeNote_VALUE, "msg.notification.productdtl");
        defaultMsg.put(ProtoMessageOuterClass.MessageType.MessageTypeShareLocation_VALUE, "msg.notification.sharelocation");
        defaultMsg.put(ProtoMessageOuterClass.MessageType.MessageTypeProduct_VALUE, "msg.notification.productlink");
        defaultMsg.put(ProtoMessageOuterClass.MessageType.MessageTypeTransChatToCustomerService_VALUE, "msg.notification.sestransfer");
        defaultMsg.put(ProtoMessageOuterClass.MessageType.MessageTypeTransChatToCustomer_VALUE, "msg.notification.sestransfer");
        defaultMsg.put(ProtoMessageOuterClass.MessageType.MessageTypeConsult_VALUE, "[来生意了]");
        defaultMsg.put(ProtoMessageOuterClass.MessageType.MessageTypeCommonTrdInfo_VALUE, "msg.notification.linkcard");
        defaultMsg.put(ProtoMessageOuterClass.MessageType.MessageTypeRevoke_VALUE, "msg.notification.recall");
        defaultMsg.put(ProtoMessageOuterClass.MessageType.MessageTypeRevoke_VALUE, "msg.notification.recall");
        defaultMsg.put(ProtoMessageOuterClass.MessageType.WebRTC_MsgType_VideoCall_VALUE, "msg.notification.videocall");
        defaultMsg.put(ProtoMessageOuterClass.MessageType.WebRTC_MsgType_Video_Group_VALUE, "msg.notification.videogroup");
        defaultMsg.put(ProtoMessageOuterClass.MessageType.WebRTC_MsgType_Video_VALUE, "atom_ui_tip_client_too_low");
        defaultMsg.put(ProtoMessageOuterClass.MessageType.WebRTC_MsgType_Audio_VALUE, "atom_ui_tip_client_too_low");
        defaultMsg.put(ProtoMessageOuterClass.MessageType.MessageTypeRobotQuestionList_VALUE, "msg.notification.questionlist");
        defaultMsg.put(ProtoMessageOuterClass.MessageType.MessageTypeRobotTurnToUser_VALUE, "msg.notification.turntocs");
        defaultMsg.put(ProtoMessageOuterClass.MessageType.WebRTC_MsgType_AudioCall_VALUE, "msg.notification.audiocall");
        defaultMsg.put(ProtoMessageOuterClass.MessageType.MessageTypeGrabMenuVcard_VALUE, "msg.notification.grabmsg");
        defaultMsg.put(ProtoMessageOuterClass.MessageType.MessageTypeGrabMenuResult_VALUE, "msg.notification.grabmsg");
        defaultMsg.put(ProtoMessageOuterClass.MessageType.MessageTypeImageNew_VALUE, "msg.notification.sticker");
        defaultMsg.put(ProtoMessageOuterClass.MessageType.MessageTypeMeetingRemind_VALUE, "msg.notification.meetinginvite");
        defaultMsg.put(ProtoMessageOuterClass.MessageType.MessageTypeEncrypt_VALUE, "msg.notification.encryptmsg");
        defaultMsg.put(ProtoMessageOuterClass.MessageType.MessageTypeSourceCode_VALUE, "msg.notification.sourcecode");
        defaultMsg.put(ProtoMessageOuterClass.MessageType.MessageTypePhoto_VALUE, "msg.notification.photo");

    }

    public static String showContentType(String strText, int msgType, PushInfo info) {

        if (strText == null)
            return "";
        if(msgType == ProtoMessageOuterClass.MessageType.MessageTypeGroupNotify_VALUE) return strText;
        String result = defaultMsg.get(msgType);
        if(!TextUtils.isEmpty(result)) {
            result = getContentByKeyAndLang(result, info.getPlatname());
        }
        if((msgType == ProtoMessageOuterClass.MessageType.MessageTypeText_VALUE
                || msgType == ProtoMessageOuterClass.MessageType.MessageTypeGroupAt_VALUE)
                && TextUtils.isEmpty(result))
            result = replaceSpecialChar(strText, info.getPlatname());
        if(AUTO_REPLY_MESSAGE == msgType)
            result = strText;
        if(TextUtils.isEmpty(result) && !TextUtils.isEmpty(strText))
            result = strText;
        if(TextUtils.isEmpty(result))
            result = getContentByKeyAndLang("msg.notification.versionlow", info.getPlatname());
        return result;
    }

    /**
     *
     * @param key
     * @param platname 里面截取 "_" 获取language和country
     * @return
     */
    public static String getContentByKeyAndLang(String key, String platname) {
        String[] strs = platname.split("_");
        Locale locale ;
        if(strs.length >= 2) {
            String language = strs[1];
            locale = new Locale(language);
//            String country = strs[2];
//            locale = new Locale(language, country);
        } else {
            locale = Locale.CHINESE;
        }

        return getStringByLocale(key, locale);
    }


    public static String getStringByLocale(String key, Locale locale) {
        LOGGER.info("getContentByKeyAndLang  key={} locale={}", key, locale.toString());
        try {
            ResourceBundle resourceBundle = ResourceBundle.getBundle("i18n.messages", locale);
            if(resourceBundle != null) {
                //防止中文乱码
//                return new String(resourceBundle.getString(key).getBytes("ISO-8859-1"), "UTF-8");
                return resourceBundle.getString(key);
            }
        } catch (Exception e) {
            LOGGER.error("catch error={} ", e);
        }
        return "";
    }


    private static String replaceSpecialChar(String srcObj, String platname) {
        if (srcObj == null) {
            return "";
        }

        if (srcObj.length() < 21) {
            return srcObj;
        }

        Matcher m = compiledPattern.matcher(srcObj);
        while (m.find()) {
            String oldStr = m.group(0);
            String type = m.group(1);

            if (type.equals("image")) {
                srcObj = srcObj.replace(oldStr, getContentByKeyAndLang("msg.notification.photo", platname));
            } else if (type.equals("emoticon")) {
//                String shortcut = m.group(2);
                srcObj = srcObj.replace(oldStr, getContentByKeyAndLang("msg.notification.sticker", platname));
            } else if (type.equals("url")) {
                srcObj = srcObj.replace(oldStr, m.group(2));
            }
        }
        return srcObj;
    }
}
