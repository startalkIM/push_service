package com.qunar.qchat.constants;

/**
 * create by hubo.hu (lex) at 2018/8/31
 */
public class SchemeConstants {

    private static final String SCHEME_QTALK = "qtalkaphone";
    private static final String SCHEME_QCHAT = "qchataphone";

    public static String getScheme(boolean isqtalk){
        if(isqtalk) {
            return SCHEME_QTALK;
        } else {
            return SCHEME_QCHAT;
        }
    }

    public static final String HOST_SINGLECHAT = "qunarchat/openSingleChat";
    public static final String HOST_GROUPCHAT = "qunarchat/openGroupChat";
}
