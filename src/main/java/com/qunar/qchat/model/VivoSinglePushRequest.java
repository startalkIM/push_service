package com.qunar.qchat.model;

import java.util.HashMap;

/**
 * create by hubo.hu (lex) at 2019/7/23
 */
public class VivoSinglePushRequest {

    /**
     * regId : 12345678901234567890123
     * notifyType : 1
     * title : 标题 1
     * content : 内容 1
     * timeToLive : 86400
     * skipType : 2
     * skipContent : http://www.vivo.com
     * networkType : 1
     * clientCustomMap : {"key1":"vlaue1","key2":"vlaue2"}
     * extra : {"callback":"http://www.vivo.com","callback.param":"vivo"}
     * requestId : 25509283-3767-4b9e-83fe-b6e55ac6b123
     */

    public String regId;
    public int notifyType;
    public String title;
    public String content;
    public int timeToLive;
    public int skipType;
    public String skipContent;
    public String networkType;
    public HashMap<String, String> clientCustomMap;
    public HashMap<String, String> extra;
    public String requestId;

}
