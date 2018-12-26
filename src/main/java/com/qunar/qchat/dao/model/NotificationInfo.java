package com.qunar.qchat.dao.model;

import com.qunar.qchat.utils.JacksonUtils;

import java.util.List;

/**
 * create by hubo.hu (lex) at 2018/2/11
 */
public class NotificationInfo {
    /**
     * @param fromjid
     * @param platkeys 客户端上传的key
     * @param platname 客户端平台 如mipush
     * @param pkgname 客户端包名 qtalk or qchat
     * @param title 标题
     * @param description 内容
     * @param json
     */
    /**用来做notifyid*/
    public String fromjid;
    /**通知标题，消息的from*/
    public String title;
    /**通知内容，单人消息的message，群消息 realfrom:message*/
    public String description;
    /**消息内容，根据msgtype转化后的message*/
    public String body;
    /**消息的msg_type*/
    public int msg_type;
    /**signaltype  根据消息type转化*/
    public int type;
    /** consult专用 chatid=4：用户->客服  5：客服->用户 */
    public String chatid;
    /**指定通知对象，客户端上传到token*/
    public List<String> platkeys;
    /**Android客户端平台*/
    public String platname;
    /**Android的客户端包名，ios的bundleid*/
    public String pkgname;
    public String json = "Just unused now";
    /**map里面发消息人的id*/
    public String fromName;
    /**map里面发消息人的domain*/
    public String fromHost;
    /** consult消息 realjid */
    public String realjid;
    /**群消息map里面发消息人的nick*/
    public String fromNick;
    /**消息to，带domain*/
    public String toUserName;
    /**消息的xml*/
    public String msgxml;
    public String cctext;

    public String messageId;

    @Override
    public String toString() {
        return JacksonUtils.obj2String(this);
    }
}
