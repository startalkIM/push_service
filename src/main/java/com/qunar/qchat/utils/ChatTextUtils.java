package com.qunar.qchat.utils;

import org.apache.http.util.TextUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatTextUtils {
    public final static int EXTEND_MSG = 666;
    public final static int PREDICTION_MSG = 668;
    public final static int EXTEND_OPS_MSG = 667;
    public final static int REVOKE_MESSAGE = -1;
    public final static int TEXT_MESSAGE = 1;
    public final static int IMAGE_MESSAGE = 3;
    public final static int VOICE_MESSAGE = 2;
    public final static int FILE_MESSAGE = 5;
    public static final int MSG_ACTION = 6;
    public static final int MSG_RICH_TEXT = 7;
    public static final int MSG_ACTION_RICH_TEXT = 8;
    public final static int COMMENT_MESSAGE = 9;
    public static final int SHAKE_MESSAGE = 10;
    public static final int MSG_NOTE = 11;
    public static final int MSG_GROUP_AT = 12;
    public static final int AUTO_REPLY_MESSAGE = 13;
    public static final int INVITE_MESSAGE = 15;
    public final static int LOCATION_MESSAGE = 16;
    public final static int IMAGE_NO_BACKGROUD = 30;
    public final static int VIDEO_MESSAGE = 32;
    public final static int ROBOT_ANSWER_MESSAGE = 47;
    public final static int CODE_MESSAGE = 64;
    public final static int READ_TO_DESTROY_MESSAGE = 128;
    public final static int MessageTypeMessageTypeCardShare = 256;
    public static final int MSG_ACTIVITY_MESSAGE = 511;
    public static final int MSG_AA_MESSAGE = 513;
    public static final int MSG_HONGBAO_MESSAGE = 512;
    public static final int MSG_AA_PROMPT = 1025;
    public static final int TRANSFER_TO_SERVER = 1002;
    public static final int TRANSFER_TO_CUSTOMER = 1001;
    public static final int MSG_HONGBAO_PROMPT = 1024;
    public static final int MSG_PRODUCT_CARD = 4096;
    public static final int MSG_ROBOT_ = 65536;
    public static final int MSG_ROBOT = 65537;
    public static final int SHARE_LOCATION = 8192;
    public static final int MSG_TYPE_RBT_SYSTEM = 268435456;
    public static final int MSG_TYPE_RBT_NOTICE = 134217728;
    public static final int MSG_TYPE_RUNSHING_ORDER = 2001;

    public static final int MSG_TYPE_ROB_ORDER = 2003;//qchat抢单
    public static final int MSG_TYPE_ROB_ORDER_RESPONSE = 2004;//qchat抢单状态
    public static final int MSG_TYPE_ZHONG_BAO = 2005;//qchat 众包消息
    public static final int MSG_TYPE_CALL_CENTER_CMD = 16384<<1;
    public static final int MSG_TYPE_WEBRTC = MSG_TYPE_CALL_CENTER_CMD<<1;
    public static final int MSG_TYPE_WEBRTC_AUDIO = MSG_TYPE_WEBRTC<<1;


    
    private final static Map<Integer,String> defaultMsg = new HashMap<Integer,String>();

    private final static String objPattern = "\\[obj type=\"([\\w]+)\" value=\"([\\S]+)\"([\\w|=|\\s|\\.]+)?\\]";
    private final static Pattern compiledPattern = Pattern.compile(objPattern);

    static {
        defaultMsg.put(IMAGE_MESSAGE,"[图片]");
        defaultMsg.put(IMAGE_NO_BACKGROUD,"[图片]");
        defaultMsg.put(FILE_MESSAGE,"[文件]");
        defaultMsg.put(LOCATION_MESSAGE,"[位置]");
        defaultMsg.put(MSG_ACTION,"[新消息]");
        defaultMsg.put(MSG_ACTION_RICH_TEXT,"[富文本消息]");
        defaultMsg.put(MSG_RICH_TEXT,"[富文本消息]");
        defaultMsg.put(MSG_TYPE_RBT_NOTICE,"[通知消息]");
        defaultMsg.put(MSG_TYPE_RBT_SYSTEM,"[系统消息]");
        defaultMsg.put(READ_TO_DESTROY_MESSAGE,"[阅后即焚]");
        defaultMsg.put(MessageTypeMessageTypeCardShare,"[名片]");
        defaultMsg.put(VIDEO_MESSAGE,"[视频]");
        defaultMsg.put(ROBOT_ANSWER_MESSAGE,"[机器人回复]");
        defaultMsg.put(VOICE_MESSAGE,"[语音]");
        defaultMsg.put(CODE_MESSAGE,"[给您发送了一段代码]");
        defaultMsg.put(COMMENT_MESSAGE,"[给您发送了一条消息]");
        defaultMsg.put(SHAKE_MESSAGE,"[窗口抖动]");
        defaultMsg.put(MSG_HONGBAO_MESSAGE,"[红包]");
        defaultMsg.put(MSG_HONGBAO_PROMPT,"[红包领取通知]");
        defaultMsg.put(MSG_ACTIVITY_MESSAGE,"[活动消息]");
        defaultMsg.put(MSG_AA_MESSAGE,"[AA收款]");
        defaultMsg.put(MSG_AA_PROMPT,"[AA收款通知]");
        defaultMsg.put(MSG_NOTE,"[咨询]");
        defaultMsg.put(SHARE_LOCATION,"[共享位置]");
        defaultMsg.put(MSG_PRODUCT_CARD,"[产品链接]");
        defaultMsg.put(TRANSFER_TO_SERVER,"[会话转移]");
        defaultMsg.put(TRANSFER_TO_CUSTOMER,"[会话转移]");
        defaultMsg.put(MSG_TYPE_RUNSHING_ORDER,"[来生意了]");
        defaultMsg.put(EXTEND_MSG,"[链接]");
        defaultMsg.put(EXTEND_OPS_MSG,"[链接]");
        defaultMsg.put(PREDICTION_MSG,"[链接]");
        defaultMsg.put(REVOKE_MESSAGE,"[撤销消息]");
        defaultMsg.put(MSG_TYPE_WEBRTC,"[实时视频]");
        defaultMsg.put(MSG_TYPE_WEBRTC_AUDIO,"[实时音频]");
//        defaultMsg.put(MSG_ROBOT,"[机器人消息]");
//        defaultMsg.put(MSG_ROBOT_,"[机器人消息]");
        defaultMsg.put(MSG_TYPE_ROB_ORDER,"[抢单消息]");
        defaultMsg.put(MSG_TYPE_ROB_ORDER_RESPONSE,"[抢单消息]");
        defaultMsg.put(MSG_TYPE_ZHONG_BAO,"[抢单消息]");


    }

    static public String showContentType(String strText, int msgType) {
        if (strText == null)
            return "";
        if(msgType == INVITE_MESSAGE) return strText;
        String result=defaultMsg.get(msgType);

        if((msgType == TEXT_MESSAGE || msgType == MSG_GROUP_AT)&&result == null)
            result = replaceSpecialChar(strText);
        if(AUTO_REPLY_MESSAGE == msgType)
            result = strText;
        if(result == null&&!TextUtils.isEmpty(strText))
            result = strText;
        if(result==null)
            result = "[不支持该类型消息，请升级到最新版]";
        return result;
    }

    private static String replaceSpecialChar(String srcObj) {
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
                srcObj = srcObj.replace(oldStr, "[图片]");
            } else if (type.equals("emoticon")) {
//                String shortcut = m.group(2);
                srcObj = srcObj.replace(oldStr, "[表情]");
            } else if (type.equals("url")) {
                srcObj = srcObj.replace(oldStr, m.group(2));
            }
        }
        return srcObj;
    }
}
