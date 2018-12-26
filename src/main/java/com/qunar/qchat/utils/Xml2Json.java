package com.qunar.qchat.utils;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/7/22.
 */
public class Xml2Json {
    private static final Logger LOGGER = LoggerFactory.getLogger(Xml2Json.class);
//    public static final String BODY_ELE = "<body>";//用于body的标签元素
//    public static final String TEXT_CONT = "<text_cont>";//用于各个标签的CDATA部分
//    private static final Pattern IMG_TYPE_PATTERN = Pattern.compile("\\[( )*obj( )+type=\"image\"( )+value=\"(.?)*\"( )+width=.*( )+height=.*( )*\\]");//用于匹配文本中的图片
//    private static final Pattern URL_TYPE_PATTERN = Pattern.compile("\\[obj( )+type=\"url\"( )+value=\"(.?)*\"( )*\\]");
//    private static final Pattern VALUE_PATTERN = Pattern.compile("value=\"(.?)*\"");


    public static Map<String, Object> xmppToMap(String xmpp) {
        if (StringUtils.isEmpty(xmpp)) {
            return null;
        }

        SAXReader saxReader = new SAXReader();
        Map<String, Object> jsonRes = new HashMap<>();
        Map<String, Object> message = new HashMap<>();
        Map<String, Object> body = new HashMap<>();
        Map<String, Object> time = new HashMap<>();
        //过滤xml无法识别的非法字符
        xmpp = xmpp.replaceAll("[\\x00-\\x08\\x0b-\\x0c\\x0e-\\x1f]", "");

        try {
            Document document = saxReader.read(new ByteArrayInputStream(xmpp.getBytes(HttpUtils.UTF8)));
            Element messageEle = document.getRootElement();
            List<Attribute> msgAttrList = messageEle.attributes();
            for (Attribute msgAttr : msgAttrList) {
                message.put(msgAttr.getQualifiedName(), msgAttr.getValue());
            }

            putAttrlist(messageEle,"body",body);
            putAttrlist(messageEle,"stime",time);

            jsonRes.put("message",message);
            jsonRes.put("body",body);
            jsonRes.put("time",time);

        } catch (DocumentException e) {
            LOGGER.error("解析xmpp消息出错, xmpp: {} ", xmpp, e);
        }
        return jsonRes;
    }

    public static void putAttrlist(Element messageEle,String key,Map<String, Object> attrmap)
    {
        Element element = messageEle.element(key);
        if (element == null)
        {
            attrmap.put(key,"");
        }else{
            List<Attribute> AttrList = element.attributes();
            for (Attribute Attr : AttrList) {
                attrmap.put(Attr.getQualifiedName(), Attr.getValue());
            }
            if (key.equals("body")) {
                attrmap.put("content", element.getText());
            }
        }
    }

}
