package com.qunar.qchat.constants;

import org.apache.http.util.TextUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


public class Config {
//    private static final Logger LOGGER = LoggerFactory.getLogger(Config.class);

    public static final String QTALK_PUSH_URL = getProperty("qtalk_push_url");
    public static final String QTALK_PUSH_KEY = getProperty("qtalk_push_key");

    private static Properties props;

    public final static String P_URL_USER_VCARD = "url.user.vcard.qchat";

    public static final String OPS_PRODUCER_CONNECT_PARAMS = getProperty("ops.producer.broker.connect.params");
    public static final String OPS_CONSUMER_ZOOKEEPER_PARAMS = getProperty("ops.consumer.zookeeper.params");
    public static final String OPS_HOSTS_GROUPID = getProperty("ops.hosts.groupid");
    public static final String OPS_KAFKA_AESKEY = getProperty("ops.kafka.aeskey");

    public static final String OPS_SPOOL_MESSAGE_TOPIC_CHAT = getProperty("ops.spool.message.topic.qtalk_chat");
    public static final String OPS_SPOOL_MESSAGE_TOPIC_GROUP = getProperty("ops.spool.message.topic.qtalk_group");
    public static final String OPS_RESEND_MESSAGE_TOPIC_QTALK = getProperty("ops.resend.sms.topic.qtalk");
    public static final String OPS_SPOOL_MESSAGE_TOPIC = getProperty("ops.spool.message.topic");
    public static final int OPS_KAFKA_THREADCOUNT = getIntProperty("ops.kafka.threadcount", 1);

    public static final String IOS_PUSH_PKG_QTALK = getProperty("ios_push_bid");
    public static final String IOS_PUSH_CERT_QTALK = getProperty("ios_push_cer_qtalk");
    public static final String IOS_PUSH_CERT_PWD = getProperty("ios_push_cer_pwd");
    public static final String IOS_PUSH_PKG_BETA_QTALK = getProperty("ios_push_bid_beta");
    public static final String IOS_PUSH_CERT_QTALK_BETA = getProperty("ios_push_cer_qtalk_beta");
    public static final String IOS_PUSH_CERT_PWD_QTALK_BETA = getProperty("ios_push_cer_pwd_beta");
//    token验证
    public static final String IOS_PUSH_AUTH_TEAM_ID = getProperty("team_id");
    public static final String IOS_PUSH_AUTH_TOKEN_ID = getProperty("token_key_id");
    public static final String IOS_PUSH_AUTH_TOKEN_FILE = getProperty("key_file_location");
    public static final String IOS_PUSH_AUTH_TOPIC = getProperty("ios_push_bid");
    public static final String IOS_TRUSTED_AAA_CERT = getProperty("ios_trusted_aaa_file");
    public static final String IOS_TRUSTED_GEO_CERT = getProperty("ios_trusted_geo_file");



    /**私有化push接口url*/
    public static final String PRIVATE_PUSH_URL = getProperty("private.push.url");

    /**应用推送图标和颜色*/
    public static final String NOTIFICATION_ICON_NAME = getProperty("adr.notification.icon.name", "");
    public static final String NOTIFICATION_ICON_COLOR = getProperty("adr.notification.icon.color", "");
    /**包名 xiaomi secret key*/
    public static final String QT_PACKAGE_NAME = getProperty("adr.qtalk.pkgname");
    public static final String MIPUSH_QT_SECRET_KEY = getProperty("adr.mipush.qtalk.key");
    public static final String HWPUSH_QT_SECRET_APPID = getProperty("adr.hwpush.qtalk.appid");
    public static final String HWPUSH_QT_SECRET_KEY = getProperty("adr.hwpush.qtalk.key");
    public static final String OPUSH_QT_SECRET_APP_KEY = getProperty("adr.opush.qtalk.app_key");
    public static final String OPUSH_QT_SECRET_KEY = getProperty("adr.opush.qtalk.key");
    public static final int MZPUSH_QT_SECRET_APPID = getIntProperty("adr.mzpush.qtalk.appid", 0);
    public static final String MZPUSH_QT_SECRET_KEY = getProperty("adr.mzpush.qtalk.key");
    public static final int VPUSH_QT_APP_ID = getIntProperty("adr.vpush.qtalk.appid", 0);
    public static final String VPUSH_QT_APP_KEY = getProperty("adr.vpush.qtalk.app_key", "");
    public static final String VPUSH_QT_APP_SECRET_KEY = getProperty("adr.vpush.qtalk.app_secret_key", "");
    public static final String FCMPUSH_QT_SERVER_KEY = getProperty("adr.fcmpush.qtalk.serverkey", "");
    private synchronized static void init() {
        if (props != null) {
            return;
        }
        InputStreamReader isr = null;
        try {
            String filename = "app.properties";
            isr = new InputStreamReader(Config.class.getClassLoader().getResourceAsStream(filename), "UTF-8");
            props = new Properties();

            props.load(isr);
        } catch (IOException e) {
            throw new ExceptionInInitializerError("Initialize the config error!");
        } finally {
            closeStream(isr);
        }
    }

    public static String getProperty(String name) {
        if (props == null) {
            init();
        }
        String val = props.getProperty(name.trim());
        if (val == null) {
            return null;
        } else {
            //去除前后端空格
            return val.trim();
        }
    }

    public static String getProperty(String name, String defaultValue) {
        if (props == null) {
            init();
        }

        String value = getProperty(name);
        if (value == null) {
            value = defaultValue;
        }
        return value.trim();
    }

    //获得整数属性值
    public static int getIntProperty(String name, int defaultVal) {
        if (props == null) {
            init();
        }

        int val = defaultVal;
        String valStr = getProperty(name);
        if (!TextUtils.isEmpty(valStr)) {
            val = Integer.parseInt(valStr);
        }
        return val;
    }

    //获得double属性值
    public static double getDoubleProperty(String name, double defaultVal) {
        if (props == null) {
            init();
        }

        double val = defaultVal;
        String valStr = getProperty(name);
        if (valStr != null) {
            val = Double.parseDouble(valStr);
        }
        return val;
    }

    public static boolean getBooleanItem(String name, boolean defaultValue) {
        if (props == null) {
            init();
        }

        boolean b = defaultValue;
        String valStr = getProperty(name);
        if (valStr != null) {
            b = Boolean.parseBoolean(valStr);
        }
        return b;
    }

//    public static String getPropertyByEncoding(String name, String encoding) {
//        if (props == null) {
//            init();
//        }
//
//        String val = getProperty(name);
//        if (val == null) return null;
//        try {
//            return new String(val.getBytes("ISO8859-1"), "UTF-8");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//            return val;
//        }
//    }

    public static String[] getArrayItem(String name) {
        if (props == null) {
            init();
        }

        String value = getProperty(name, "");
        if (value.trim().isEmpty()) {
            return null;
        }

        String sepChar = ",";
        if (value.contains(";")) {
            sepChar = ";";
        }
        return value.split(sepChar);

    }

    public static List<String> getListItem(String item) {
        if (props == null) {
            init();
        }

        List<String> list = new ArrayList<>();
        String value = getProperty(item, "");
        if (value.trim().isEmpty()) {
            return list;
        }

        String sepChar = ",";
        if (value.contains(";")) {
            sepChar = ";";
        }
        String[] sa = value.split(sepChar);
        for (String aSa : sa) {
            list.add(aSa.trim());
        }
        return list;
    }

    public static void setProperty(String name, String value) {
        if (props == null) {
            init();
        }

        props.setProperty(name, value);
    }

    private static void closeStream(InputStreamReader is) {
        if (is == null) {
            return;
        }

        try {
            is.close();
        } catch (IOException e) {
            throw new ExceptionInInitializerError("Initialize the config error!");
        }
    }
}
