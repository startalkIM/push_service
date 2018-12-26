package com.qunar.qchat.utils;

import org.apache.http.util.CharsetUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;

public class HttpUtils {
//    private static final Logger LOGGER = LoggerFactory.getLogger(HttpUtils.class);
    public static final Charset UTF8 = CharsetUtils.lookup("utf-8");

    public static String urlDecode(String str) {
        try {
            return URLDecoder.decode(str, "utf-8");
        } catch (UnsupportedEncodingException e) {
            return str;
        }
    }

    public static String urlEncode(String str) {
        try {
            return URLEncoder.encode(str, "utf-8");
        } catch (UnsupportedEncodingException e) {
            return str;
        }
    }
}
