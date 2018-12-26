package com.qunar.qchat.utils;

import com.alibaba.fastjson.JSON;
import com.qunar.qchat.caches.EhRedisCache;
import org.apache.http.util.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * create by hubo.hu (lex) at 2018/6/11
 */
public class CommonRedisUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonRedisUtil.class);
    /**
     * 是否是公共域
     * @param toDomain
     * @return
     */
    public static boolean isPubim(String toDomain) {
        if(toDomain.equalsIgnoreCase("ejabhost1")
                || toDomain.equalsIgnoreCase("ejabhost2")) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 是否有平台在线
     * @param toUser
     * @param toDomain
     * @return
     */
    public static boolean isHasPlatOnline(String toUser, String toDomain){
        boolean isHasOtherPlatOnline = false;
        if(TextUtils.isEmpty(toDomain) || TextUtils.isEmpty(toUser)) {
            return isHasOtherPlatOnline;
        }
        String key = "ejabberd:sm:other:" + QtalkStringUtils.userId2Jid(toUser, toDomain);
        Map<String,String> result = null;
        result = RedisUtil.hGetAll(7, key);
//        LOGGER.info("isHasPlatOnline result={} touser={}", result, QtalkStringUtils.userId2Jid(toUser, toDomain));
        if(!CollectionUtils.isEmpty(result)){
            for(String value : result.values()) {
//                LOGGER.info("isHasPlatOnline value={} touser={} ", value, QtalkStringUtils.userId2Jid(toUser, toDomain));
                Map<String, String> resouce = JSON.parseObject(value, Map.class);
//                LOGGER.info("isHasPlatOnline resouce={} touser={} ", resouce, QtalkStringUtils.userId2Jid(toUser, toDomain));
                if(!CollectionUtils.isEmpty(resouce)){
                    String r = resouce.get("r").toString();
                    String f = resouce.get("f").toString();

                    if("normal".equalsIgnoreCase(f)
                            || "online".equalsIgnoreCase(f)
                            || "push".equalsIgnoreCase(f)){
                        isHasOtherPlatOnline = true;
                        LOGGER.info("isHasPlatOnline r={} f={} touser={}", r, f, QtalkStringUtils.userId2Jid(toUser, toDomain));
                        return isHasOtherPlatOnline;
                    }
                }
            }
        }
        return isHasOtherPlatOnline;
    }

    /**
     * 是否有平台离开状态
     * @param toUser
     * @param toDomain
     * @return
     */
    public static boolean isHasPlatAway(String toUser, String toDomain) {
        boolean isHasAnyAway = false;
        if(TextUtils.isEmpty(toDomain) || TextUtils.isEmpty(toUser)) {
            return isHasAnyAway;
        }
        String key = "ejabberd:sm:other:" + QtalkStringUtils.userId2Jid(toUser, toDomain);
        Map<String,String> result = null;
        result = RedisUtil.hGetAll(7, key);
//        LOGGER.info("isHasPlatAway result={} touser={}", result, QtalkStringUtils.userId2Jid(toUser, toDomain));
        if(!CollectionUtils.isEmpty(result)){
            for(String value : result.values()) {
//                LOGGER.info("isHasPlatAway value={} touser={} ", value, QtalkStringUtils.userId2Jid(toUser, toDomain));
                Map<String, String> resouce = JSON.parseObject(value, Map.class);
//                LOGGER.info("isHasPlatAway resouce={} touser={} ", resouce, QtalkStringUtils.userId2Jid(toUser, toDomain));

                if(!CollectionUtils.isEmpty(resouce)){
//                    String r = resouce.get("r").toString();
                    String f = resouce.get("f").toString();
                    if("away".equalsIgnoreCase(f)){
                        isHasAnyAway = true;
                        return isHasAnyAway;
                    }
                }
            }
        }
        return isHasAnyAway;
    }

    public static int isSubscriptGroup(String username, String host, String mucname){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("user:grop:subcribe:");
        stringBuilder.append(username);
        stringBuilder.append("_");
        stringBuilder.append(host);
        stringBuilder.append("_");
        stringBuilder.append(mucname);
        int isSubscript = -1;
        try {
            isSubscript = RedisUtil.get(EhRedisCache.REDIS_PUSH_TABLE, stringBuilder.toString(), int.class);
            LOGGER.info("isSubscriptGroup try key={} value={} touser={}", stringBuilder.toString(), isSubscript, QtalkStringUtils.userId2Jid(username, host));
        } catch (Exception e) {
            String value = RedisUtil.get(EhRedisCache.REDIS_PUSH_TABLE, stringBuilder.toString(), String.class);
            LOGGER.info("isSubscriptGroup catch e={} key={} value={} touser={}", e.getMessage(), stringBuilder.toString(), isSubscript, QtalkStringUtils.userId2Jid(username, host));
            if(!TextUtils.isEmpty(value)) {
                isSubscript = "true".equalsIgnoreCase(value) ? 1 : 0;
                setSubscriptGrop(username, host, mucname, isSubscript);
            }
        }
        LOGGER.info("isSubscriptGroup key={} value={} touser={}", stringBuilder.toString(), isSubscript, QtalkStringUtils.userId2Jid(username, host));
        return isSubscript;
    }

    public static void setSubscriptGrop(String username, String host, String mucname, int value){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("user:grop:subcribe:");
        stringBuilder.append(username);
        stringBuilder.append("_");
        stringBuilder.append(host);
        stringBuilder.append("_");
        stringBuilder.append(mucname);
        RedisUtil.set(EhRedisCache.REDIS_PUSH_TABLE, stringBuilder.toString(), value, EhRedisCache.liveTime, TimeUnit.SECONDS);
    }
}
