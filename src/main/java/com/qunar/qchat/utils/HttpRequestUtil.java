package com.qunar.qchat.utils;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by qitmac000378 on 17/5/25.
 */
public class HttpRequestUtil {
    public static String getIpAddr(HttpServletRequest request) {
        if (request.getHeader("x-forwarded-for") == null) {
            return request.getRemoteAddr();
        }
        return request.getHeader("x-forwarded-for");
    }

    public static String getICheckIp(HttpServletRequest request) {
        if (request.getHeader("I-CheckIp-Ip") == null) {
            return "";
        }
        return request.getHeader("I-CheckIp-Ip");
    }


}
