package com.qunar.qchat.utils;


import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


/**
 * Created by MSI on 2017/9/13.
 */
public class CreateXmppMsg {


    private static final Logger LOGGER = LoggerFactory.getLogger(CreateXmppMsg.class);

    public static String makeChatCollectionMsg(String From,String To,String RealFrom,String RealTo,
                                           Map<String, Object> commonHandledJsonMap) {
        if (MapUtils.isEmpty(commonHandledJsonMap)) {
            return null;
        }

        DocumentFactory df = DocumentFactory.getInstance();
        Document document = df.createDocument("utf-8");

        Element messageEle = document.addElement("message");
        HashMap<String, String> messageMap = (HashMap<String, String>) commonHandledJsonMap.get("message");

        //  messageEle.addAttribute("channelid", doCreateChannelId(messageMap));
        String type = messageMap.get("type");
        messageMap.put("type", "collection");
        messageMap.put("from", From);
        messageMap.put("to", To);
        messageMap.put("originfrom", RealFrom);
        messageMap.put("originto", RealTo);
        messageMap.put("origintype", type);
        messageMap.remove("msec_times");

        setMapAttrs(messageMap,messageEle);

        Element bodyEle = messageEle.addElement("body");
        doCreateBodyEle(bodyEle, commonHandledJsonMap);

      /* Element timeEle = messageEle.addElement("stime");

        HashMap<String, String> timeMap = (HashMap<String, String>) commonHandledJsonMap.get("time");
        timeMap.put("xmlns","jabber:stime:delay");
        setMapAttrs(timeMap,timeEle);*/

        return trimXml(document);
    }

    public static String makeGroupCollectionMsg(String From,String To,String RealFrom,String RealTo,
                                           Map<String, Object> commonHandledJsonMap) {
        if (MapUtils.isEmpty(commonHandledJsonMap)) {
            return null;
        }

        DocumentFactory df = DocumentFactory.getInstance();
        Document document = df.createDocument("utf-8");

        Element messageEle = document.addElement("message");
        HashMap<String, String> messageMap = (HashMap<String, String>) commonHandledJsonMap.get("message");

        Object realjid = messageMap.get("realfrom");
        if (realjid != null){
            messageMap.put("realjid", realjid.toString());
        }
        //  messageEle.addAttribute("channelid", doCreateChannelId(messageMap));
        String type = messageMap.get("type");
        messageMap.put("type", "collection");
        messageMap.put("from", From);
        messageMap.put("to", To);
        messageMap.put("originfrom", RealFrom);
        messageMap.put("originto", RealTo);
        messageMap.put("origintype", type);
        messageMap.remove("msec_times");

        setMapAttrs(messageMap,messageEle);

        Element bodyEle = messageEle.addElement("body");
        doCreateBodyEle(bodyEle, commonHandledJsonMap);

      /* Element timeEle = messageEle.addElement("stime");

        HashMap<String, String> timeMap = (HashMap<String, String>) commonHandledJsonMap.get("time");
        timeMap.put("xmlns","jabber:stime:delay");
        setMapAttrs(timeMap,timeEle);*/

        return trimXml(document);
    }

    private  static void setMapAttrs(Map<String,String> maps,Element messageEle)
    {
        for (String key : maps.keySet()) {
            String value = maps.get(key);
            if (value != null) {
                messageEle.addAttribute(key, value);
            }
        }
    }

//    private static String doCreateChannelId(Map<String, String> commonHandledJsonMap) {
//        Map<String, Object> channelMap = new HashMap<>();
//        String channelid = commonHandledJsonMap.get("channelid");
//
//        if (channelid == null)
//        {
//           // channelMap.put("d", "send");
//        }else {
//            channelMap = JacksonUtils.string2Map(channelid);
//            // channelMap.put("cn", MapUtils.getString(commonHandledJsonMap, "cn"));
//            channelMap.put("d", "send");
//        }
//        return JacksonUtils.obj2String(channelMap);
//    }

    @SuppressWarnings("unchecked")
    private static void doCreateBodyEle(final Element bodyEle, Map<String, Object> commonJsonMap) {

        HashMap<String, String> xmppMsgContPair = (HashMap<String, String>) commonJsonMap.get("body");

        //向Body标签插入内容
        if (xmppMsgContPair != null) {
            bodyEle.addText(xmppMsgContPair.get("content"));
            xmppMsgContPair.remove("content");

            setMapAttrs(xmppMsgContPair,bodyEle);
            String msgId = xmppMsgContPair.get("id");
            if (StringUtils.isEmpty(msgId)) {
                msgId = "_"+ getUUID() ;
            }
            msgId = getUUID() + "_" + msgId;
            bodyEle.addAttribute("id", msgId);
        }
    }


    public static String getUUID() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString().replace("-", "");
    }

    public static String trimXml(Document document) {
        ByteArrayOutputStream baOs = new ByteArrayOutputStream();
        OutputFormat of = new OutputFormat(null, false, "utf-8");
        of.setSuppressDeclaration(true);
        try {
            XMLWriter xw = new XMLWriter(baOs, of);
            xw.write(document);
            return baOs.toString("utf-8");
        } catch (IOException e) {
            String errorXmpp = document.asXML();
            LOGGER.error("从json转成xmpp出错，errorXmpp[{}]", errorXmpp);
            return errorXmpp;
        }
    }


    public static String getHostbyUser(String str) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }

        int AtSize = str.indexOf("@");
        if(AtSize < 0 ){
            return str;
        }

        int endSize = str.indexOf("/");
        if (endSize < 0){
            endSize = str.length();
        }
        return str.substring(AtSize+1, endSize );
    }

}
