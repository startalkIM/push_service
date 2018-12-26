package com.qunar.qchat.constants;

/**
 * create by hubo.hu (lex) at 2018/4/23
 */
public class MessageSettingsTag {

    public static final int SHOW_CONTENT = 0x01;//是否显示推送详情
    public static final int PUSH_ONLINE = SHOW_CONTENT << 1;//pc端在线时是否收推送
    public static final int SOUND_INAPP = PUSH_ONLINE << 1;//app在前端是否有声音
    public static final int VIBRATE_INAPP = SOUND_INAPP << 1;//app在前端是否有震动
    public static final int PUSH_SWITCH = VIBRATE_INAPP << 1;//push总开关

    //默认在线收push是关着的
    public static final int DEFAULT_ALL = SHOW_CONTENT | SOUND_INAPP | VIBRATE_INAPP | PUSH_SWITCH;

    /**
     * 获取flag状态是否存在
     * @param status  数据库值
     * @param flag  要获取
     * @return
     */
    public static boolean isExistTag(int status, int flag){
        return (status & flag) != 0;
    }
}
