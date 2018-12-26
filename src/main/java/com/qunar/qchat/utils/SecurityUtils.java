package com.qunar.qchat.utils;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.TextUtils;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

public class SecurityUtils {
//    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityUtils.class);
    private static final Map<String, String> SECRET_KEY_MAP = new HashMap<>();
    private static final long TIME_DEVIATION = 6000L;

    static {
        SECRET_KEY_MAP.put("wechat", "8b1075d35e511a66614bda61d518f9a8c14ec687f878bf9977488d77956bec92");
        SECRET_KEY_MAP.put("tripsters", "8408d27b23afe129df936d4b1933915092f7d5221ec9f32b5bb818c2646ef8fb");
        SECRET_KEY_MAP.put("oniontour", "542d24171cc56d9ff1ea3865eab074cca2ddb6bcce389b970aaf99b9cf83da87");
        SECRET_KEY_MAP.put("nilaitravel", "c26a25a0b05da5c38d06b0e2da2c65d6a6861b00e634ef7ec8f37f6dc8b9126b");
    }

//    private static String secretKey(String agentId) throws UnsupportedEncodingException {
//        String ori = agentId + "qchat_api" + RandomUtils.nextInt(10000, 99999);
//        byte[] bytes = DigestUtils.sha256(ori.getBytes("utf-8"));
//        return Hex.encodeHexString(bytes);
//    }

    public static String generateSign(String agentId, String secretKey, String unixTimestamp) {
        String ori = StringUtils.join(agentId, secretKey, unixTimestamp);
        byte[] md5 = DigestUtils.md5(ori.getBytes(HttpUtils.UTF8));
        return Base64.encodeBase64String(md5);
    }

    public static boolean isValid(String agentId, String sign, String unixTimestamp) {
        if (StringUtils.isEmpty(agentId) || StringUtils.isEmpty(sign) || !isTimeValid(unixTimestamp)) {
            return false;
        }

        String secretKey = SECRET_KEY_MAP.get(agentId);
        if (StringUtils.isEmpty(secretKey)) {
            return false;
        }
        String genSign = generateSign(agentId, secretKey, unixTimestamp);
        return genSign.equals(sign);
    }

    private static boolean isTimeValid(String unixTimestamp) {
        if (!StringUtils.isNumeric(unixTimestamp)) {
            return false;
        }
        long timestamp = Long.parseLong(unixTimestamp);
        long now = System.currentTimeMillis() / 1000L;
        long diff = now - timestamp;
        return diff < TIME_DEVIATION && diff > -TIME_DEVIATION;
    }

    public static String getMD5(String str) {
        try {
            // 生成一个MD5加密计算摘要
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 计算md5函数
            md.update(str.getBytes());
            // digest()最后确定返回md5 hash值，返回值为8为字符串。因为md5 hash值是16位的hex值，实际上就是8位的字符
            // BigInteger函数则将8位的字符串转换成16位hex值，用字符串来表示；得到字符串形式的hash值
            return new BigInteger(1, md.digest()).toString(16);
        } catch (Exception e) {
            return "";
        }
    }

//    private static String forBetaSign(String agId) {
//        String timestamp = String.valueOf(System.currentTimeMillis() / 1000L);
//        return StringUtils.join(HttpUtils.urlEncode(generateSign(agId, SECRET_KEY_MAP.get(agId), timestamp)), "    ", timestamp);
//    }

    public static void main(String[] args) throws UnsupportedEncodingException {
//        System.out.println(secretKey("oniontour"));
//        System.out.println(forBetaSign("oniontour"));
        long s = System.currentTimeMillis() / 1000L;
//        s = 1464691406l;
        System.out.println(s);
        System.out.println(HttpUtils.urlEncode(generateSign("tripsters", "8408d27b23afe129df936d4b1933915092f7d5221ec9f32b5bb818c2646ef8fb", String.valueOf(s))));
    }

    public static int transformStringToInt(String str) {
        if(!TextUtils.isEmpty(str)) {
            if(str.hashCode() == Integer.MIN_VALUE) {
                return Math.abs(str.hashCode()) % 13;
            } else {
                return Math.abs(str.hashCode());
            }
        }
        return 0;
    }
}
