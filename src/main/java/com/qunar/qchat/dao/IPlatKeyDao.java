package com.qunar.qchat.dao;

import com.qunar.qchat.dao.model.PushInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public interface IPlatKeyDao {
    public int updatePlatKey(
            @Param("table") String table,
            @Param("username") String username,
            @Param("domain") String domain,
            @Param("mac_key") String mac_key,
            @Param("platname") String platname,
            @Param("pkgname") String pkgname,
            @Param("os") String os,
            @Param("version") String version);

    public int insertPlatKey(
            @Param("table") String table,
            @Param("username") String username,
            @Param("domain") String domain,
            @Param("mac_key") String mac_key,
            @Param("platname") String platname,
            @Param("pkgname") String pkgname,
            @Param("os") String os,
            @Param("version") String version);

    public int updateDelPlatKey(
            @Param("table") String table,
            @Param("username") String username,
            @Param("domain") String domain,
            @Param("mac_key") String mac_key,
            @Param("platname") String platname,
            @Param("pkgname") String pkgname,
            @Param("os") String os,
            @Param("version") String version);

    public PushInfo selectPlatKey(
            @Param("table") String table,
            @Param("username") String username,
            @Param("domain") String domain);

    public int insertPlatKey(
            @Param("table") String table,
            @Param("username") String username,
            @Param("domain") String domain,
            @Param("show_content") int show_content,
            @Param("os") String os,
            @Param("version") String version);

    public HashMap<String, Object> selectShowContent(
            @Param("table") String table,
            @Param("username") String username,
            @Param("domain") String domain,
            @Param("os") String os,
            @Param("version") String version);

    public int updateDelOldMacKey(
            @Param("table") String table,
            @Param("username") String username,
            @Param("domain") String domain,
            @Param("mac_key") String mac_key,
            @Param("os") String os,
            @Param("version") String version);


    public int updateShowContent(
            @Param("table") String table,
            @Param("username") String username,
            @Param("domain") String domain,
            @Param("index") int index,
            @Param("show_content") int show_content,
            @Param("os") String os,
            @Param("version") String version);

    public int updateOnlinePush(
            @Param("table") String table,
            @Param("username") String username,
            @Param("domain") String domain,
            @Param("index") int index,
            @Param("online_push") int online_push,
            @Param("os") String os,
            @Param("version") String version);

    public int updateInappSound(
            @Param("table") String table,
            @Param("username") String username,
            @Param("domain") String domain,
            @Param("index") int index,
            @Param("inapp_sound") int inapp_sound,
            @Param("os") String os,
            @Param("version") String version);

    public int updateInappVibrate(
            @Param("table") String table,
            @Param("username") String username,
            @Param("domain") String domain,
            @Param("index") int index,
            @Param("inapp_vibrate") int inapp_vibrate,
            @Param("os") String os,
            @Param("version") String version);

    public int updatePushSwitch(
            @Param("table") String table,
            @Param("username") String username,
            @Param("domain") String domain,
            @Param("index") int index,
            @Param("push_switch") int push_switch,
            @Param("os") String os,
            @Param("version") String version);

    public HashMap<String, String> selectMsgSettings(
            @Param("table") String table,
            @Param("username") String username,
            @Param("domain") String domain,
            @Param("os") String os,
            @Param("version") String version);

    public String selectGroupIsSubsribe(
            @Param("table") String table,
            @Param("username") String username,
            @Param("domain") String domain,
            @Param("muc_name") String muc_name);

    public int selectGroupIsSubsribeFromClientConfig(
            @Param("table") String table,
            @Param("username") String username,
            @Param("domain") String domain,
            @Param("muc_name") String muc_name);

    public int updateGroupNotification(
            @Param("table") String table,
            @Param("username") String username,
            @Param("domain") String domain,
            @Param("muc_name") String muc_name,
            @Param("muc_domain") String muc_domain,
            @Param("subscribe_flag") int subscribe_flag);
}