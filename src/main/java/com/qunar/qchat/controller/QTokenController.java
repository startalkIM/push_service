package com.qunar.qchat.controller;

import com.notnoop.apns.internal.Utilities;
import com.qunar.qchat.aop.RecordAccessLog;
import com.qunar.qchat.constants.AdrPushConstants;
import com.qunar.qchat.constants.MessageSettingsTag;
import com.qunar.qchat.constants.QMonitorConstants;
import com.qunar.qchat.dao.IPlatKeyDao;
import com.qunar.qchat.model.JsonResult;
import com.qunar.qchat.service.DispatchService;
import com.qunar.qchat.service.PushInfoServiceImpl;
import com.qunar.qchat.service.SpoolMessageService;
import com.qunar.qchat.utils.CommonRedisUtil;
import com.qunar.qchat.utils.JacksonUtils;
import com.qunar.qchat.utils.JsonResultUtils;
import com.qunar.qchat.utils.QtalkStringUtils;
import com.qunar.qtalk.ss.common.utils.watcher.QMonitor;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.util.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RequestMapping("/qtapi/token/")
@RestController
public class QTokenController {

    private static final Logger LOGGER = LoggerFactory.getLogger(com.qunar.qchat.controller.QTokenController.class);

    @Autowired
    private IPlatKeyDao platKeyDao;
    @Autowired
    DispatchService mDispatchService;
    @Autowired
    private PushInfoServiceImpl pushInfoService;

    @Autowired
    private SpoolMessageService spoolMessageService;

    @RequestMapping(value = "/setpersonmackey.qunar", method = RequestMethod.GET)
    @RecordAccessLog
    public JsonResult<?> setPersonMacKey(@RequestParam(value = "username") String username,
                                   @RequestParam(value = "domain") String domain,
                                   @RequestParam(value = "mac_key") String mac_key,
                                   @RequestParam(value = "platname", defaultValue = "") String platname,
                                   @RequestParam(value = "pkgname", defaultValue = "") String pkgname,
                                   @RequestParam(value = "os") String os,
                                   @RequestParam(value = "version") String version) {

        String table = AdrPushConstants.TABLE_NAME_PUSH;

        try {
            //验证ios key
            if ("ios".equalsIgnoreCase(os)) {
                Utilities.decodeHex(mac_key);
            }

            if (platKeyDao.updatePlatKey(table, username, domain, mac_key, platname, pkgname, os, version) == 0) {
                platKeyDao.insertPlatKey(table, username, domain, mac_key, platname, pkgname, os, version);
            } else {
                pushInfoService.clearPushinfoCache(username, domain);
            }
        } catch (RuntimeException re) {
            LOGGER.error("catch error={} mac_key={} touser={}", re, mac_key, QtalkStringUtils.userId2Jid(username, domain));
            JsonResultUtils.fail(0, "push key非法");
        } catch (Exception e) {
            QMonitor.recordOne(QMonitorConstants.setpersonmackey);
            LOGGER.error("catch error ", e);
            return JsonResultUtils.fail(0, "服务器操作异常:\n " + ExceptionUtils.getStackTrace(e));
        }

        return JsonResultUtils.success();
    }


    @RequestMapping(value = "/delpersonmackey.qunar", method = RequestMethod.GET)
    @RecordAccessLog
    public JsonResult<?> delPersonPlatKey(@RequestParam(value = "username") String username,
                                         @RequestParam(value = "domain") String domain,
                                         @RequestParam(value = "mac_key") String mac_key,
                                         @RequestParam(value = "platname", defaultValue = "") String platname,
                                         @RequestParam(value = "pkgname", defaultValue = "") String pkgname,
                                         @RequestParam(value = "os") String os,
                                         @RequestParam(value = "version") String version) {

        String table = AdrPushConstants.TABLE_NAME_PUSH;

        try {
            platKeyDao.updateDelPlatKey(table, username, domain, mac_key, platname, pkgname, os, version);
            pushInfoService.clearPushinfoCache(username, domain);
        } catch (Exception e) {
            QMonitor.recordOne(QMonitorConstants.delpersonmackey);
            LOGGER.debug("catch error ", e);
            return JsonResultUtils.fail(0, "服务器操作异常:\n " + ExceptionUtils.getStackTrace(e));
        }

        return JsonResultUtils.success();
    }

    @RequestMapping(value = "/setpushshowcontent.qunar", method = RequestMethod.GET)
    @RecordAccessLog
    public JsonResult<?> setShowContent(@RequestParam(value = "username") String username,
                                         @RequestParam(value = "domain") String domain,
                                         @RequestParam(value = "show_content") int show_content,
                                         @RequestParam(value = "os") String os,
                                         @RequestParam(value = "version") String version) {

        String table = AdrPushConstants.TABLE_NAME_PUSH;
// TODO: 2018/7/19 找个时间一起下掉
        try {
            platKeyDao.updateShowContent(table, username, domain, MessageSettingsTag.SHOW_CONTENT, show_content, os, version);
        } catch (Exception e) {
            QMonitor.recordOne(QMonitorConstants.setpushshowcontent);
            LOGGER.debug("catch error ", e);
            return JsonResultUtils.fail(0, "服务器操作异常:\n " + ExceptionUtils.getStackTrace(e));
        }

        return JsonResultUtils.success();
    }

    @RequestMapping(value = "/setOnlinePush.qunar", method = RequestMethod.GET)
    @RecordAccessLog
    public JsonResult<?> setOnlinePush(@RequestParam(value = "username") String username,
                                        @RequestParam(value = "domain") String domain,
                                        @RequestParam(value = "online_push") int online_push,
                                        @RequestParam(value = "os") String os,
                                        @RequestParam(value = "version") String version) {

        String table = AdrPushConstants.TABLE_NAME_PUSH;
// TODO: 2018/7/19 找个时间一起下掉
        try {
            platKeyDao.updateOnlinePush(table, username, domain, MessageSettingsTag.PUSH_ONLINE, online_push, os, version);
            pushInfoService.clearPushinfoCache(username, domain);
        } catch (Exception e) {
            QMonitor.recordOne(QMonitorConstants.setOnlinePush);
            LOGGER.debug("catch error ", e);
            return JsonResultUtils.fail(0, "服务器操作异常:\n " + ExceptionUtils.getStackTrace(e));
        }

        return JsonResultUtils.success();
    }

    @RequestMapping(value = "/setInappSound.qunar", method = RequestMethod.GET)
    @RecordAccessLog
    public JsonResult<?> setInappSound(@RequestParam(value = "username") String username,
                                       @RequestParam(value = "domain") String domain,
                                       @RequestParam(value = "inapp_sound") int inapp_sound,
                                       @RequestParam(value = "os") String os,
                                       @RequestParam(value = "version") String version) {

        String table = AdrPushConstants.TABLE_NAME_PUSH;
        // TODO: 2018/7/19 找个时间一起下掉
        try {
            platKeyDao.updateInappSound(table, username, domain, MessageSettingsTag.SOUND_INAPP, inapp_sound, os, version);
        } catch (Exception e) {
            LOGGER.debug("catch error ", e);
            return JsonResultUtils.fail(0, "服务器操作异常:\n " + ExceptionUtils.getStackTrace(e));
        }

        return JsonResultUtils.success();
    }

    @RequestMapping(value = "/setInappVibrate.qunar", method = RequestMethod.GET)
    @RecordAccessLog
    public JsonResult<?> setInappVibrate(@RequestParam(value = "username") String username,
                                       @RequestParam(value = "domain") String domain,
                                       @RequestParam(value = "inapp_vibrate") int inapp_vibrate,
                                       @RequestParam(value = "os") String os,
                                       @RequestParam(value = "version") String version) {
        // TODO: 2018/7/19 找个时间一起下掉
        String table = AdrPushConstants.TABLE_NAME_PUSH;

        try {
            platKeyDao.updateInappVibrate(table, username, domain, MessageSettingsTag.VIBRATE_INAPP, inapp_vibrate, os, version);
            pushInfoService.clearPushinfoCache(username, domain);
        } catch (Exception e) {
//            e.printStackTrace();
            LOGGER.debug("catch error ", e);
            return JsonResultUtils.fail(0, "服务器操作异常:\n " + ExceptionUtils.getStackTrace(e));
        }

        return JsonResultUtils.success();
    }

    @RequestMapping(value = "/setmsgsettings.qunar", method = RequestMethod.GET)
    @RecordAccessLog
    public JsonResult<?> setMsgSettings(@RequestParam(value = "username") String username,
                                        @RequestParam(value = "domain") String domain,
                                        @RequestParam(value = "index") int index,
                                        @RequestParam(value = "status") int status,
                                        @RequestParam(value = "os") String os,
                                        @RequestParam(value = "version") String version) {

        String table = AdrPushConstants.TABLE_NAME_PUSH;
        HashMap<String, String> map = new HashMap<>();
        try {
            if(index == MessageSettingsTag.PUSH_ONLINE){
                platKeyDao.updateOnlinePush(table, username, domain, index, status, os, version);
                pushInfoService.clearPushinfoCache(username, domain);
            }else if(index == MessageSettingsTag.SHOW_CONTENT){
                platKeyDao.updateShowContent(table, username, domain, index, status, os, version);
                pushInfoService.clearPushinfoCache(username, domain);
            }else if(index == MessageSettingsTag.SOUND_INAPP){
                platKeyDao.updateInappSound(table, username, domain, index, status, os, version);
            }else if(index == MessageSettingsTag.VIBRATE_INAPP){
                platKeyDao.updateInappVibrate(table, username, domain, index, status, os, version);
            }else if(index == MessageSettingsTag.PUSH_SWITCH){
                platKeyDao.updatePushSwitch(table, username, domain, index, status, os, version);
                pushInfoService.clearPushinfoCache(username, domain);
            }
        } catch (Exception e) {
            QMonitor.recordOne(QMonitorConstants.setmsgsettings);
            LOGGER.debug("catch error ", e);
            return JsonResultUtils.fail(0, "服务器操作异常:\n " + ExceptionUtils.getStackTrace(e));
        }

        return JsonResultUtils.success(map);
    }

    @RequestMapping(value = "/getpushshowcontent.qunar", method = RequestMethod.GET)
    @RecordAccessLog
    public JsonResult<?> getShowContent(@RequestParam(value = "username") String username,
                                        @RequestParam(value = "domain") String domain,
                                        @RequestParam(value = "os") String os,
                                        @RequestParam(value = "version") String version) {
        // TODO: 2018/7/19 可以随时准备下掉
        String table = AdrPushConstants.TABLE_NAME_PUSH;
        HashMap<String, Object> map = new HashMap<>();
        try {
             map = platKeyDao.selectShowContent(table, username, domain, os, version);
            //兼容旧版本,旧版本是show_content字段
            if(map.containsKey("push_flag")){
                boolean isShowcontent = MessageSettingsTag.isExistTag(Integer.valueOf(map.get("push_flag").toString()), MessageSettingsTag.SHOW_CONTENT);
                map.put("show_content", isShowcontent ? "1" : "0");
            }
        } catch (Exception e) {
            LOGGER.debug("catch error ", e);
            return JsonResultUtils.fail(0, "服务器操作异常:\n " + ExceptionUtils.getStackTrace(e));
        }

        return JsonResultUtils.success(map);
    }

    @RequestMapping(value = "/getmsgsettings.qunar", method = RequestMethod.GET)
    @RecordAccessLog
    public JsonResult<?> getMsgSettings(@RequestParam(value = "username") String username,
                                        @RequestParam(value = "domain") String domain,
                                        @RequestParam(value = "os") String os,
                                        @RequestParam(value = "version") String version) {

        String table = AdrPushConstants.TABLE_NAME_PUSH;
        HashMap<String, String> map = new HashMap<>();
        try {
            map = platKeyDao.selectMsgSettings(table, username, domain, os, version);

        } catch (Exception e) {
            QMonitor.recordOne(QMonitorConstants.getmsgsettings);
            LOGGER.debug("catch error ", e);
            return JsonResultUtils.fail(0, "服务器操作异常:\n " + ExceptionUtils.getStackTrace(e));
        }

        return JsonResultUtils.success(map);
    }

    @RequestMapping(value = "/setgroupnotification.qunar", method = RequestMethod.GET)
    @RecordAccessLog
    public JsonResult<?> setGroupNotification(@RequestParam(value = "username") String username,
                                        @RequestParam(value = "domain") String domain,
                                        @RequestParam(value = "muc_name") String muc_name,
                                        @RequestParam(value = "muc_domain", defaultValue = "") String muc_domain,
                                        @RequestParam(value = "subscribe_flag") int subscribe_flag) {

        String table = AdrPushConstants.TABLE_NAME_MUC_USERS;
        try {
            // TODO: 2018/7/12 该接口新版已修改为个人配置表，后续该接口可以下掉，
            platKeyDao.updateGroupNotification(table, username, domain, muc_name, muc_domain, subscribe_flag);
            pushInfoService.clearGroupNotifyCache(username, domain, QtalkStringUtils.roomId2Jid(muc_name, muc_domain));
            //修改redis缓存
            CommonRedisUtil.setSubscriptGrop(username, domain, QtalkStringUtils.roomId2Jid(muc_name, muc_domain), subscribe_flag);
        } catch (Exception e) {
            QMonitor.recordOne(QMonitorConstants.setgroupnotification);
            LOGGER.debug("catch error ", e);
            return JsonResultUtils.fail(0, "服务器操作异常:\n " + ExceptionUtils.getStackTrace(e));
        }

        return JsonResultUtils.success();
    }

    @RequestMapping(value = "/sendMessageToPush.qunar", method = RequestMethod.POST)
    @RecordAccessLog
    public JsonResult<?> sendMessageToPush(@RequestBody String message) {

        try {
            LOGGER.info("接口sendMessageToPush消息msg:[{}]", message);
            QMonitor.recordOne(QMonitorConstants.SEND_MESSAGE_PUSH);
            Map<String, Object> chatMessage = JacksonUtils.string2Obj(message, Map.class);
            if(chatMessage == null) return JsonResultUtils.fail(0, "消息异常msg:" + message);
            String type = "";
            if(chatMessage.containsKey("topic") && chatMessage.get("topic") != null) {
                type = chatMessage.get("topic").toString();
            }
            if(TextUtils.isEmpty(type)) {
                return JsonResultUtils.fail(0, "topic is null 消息异常msg:" + message);
            }
            spoolMessageService.processChatMessage(type, chatMessage);
        } catch (Exception e) {
            LOGGER.debug("sendMessageToPush catch error ", e);
            return JsonResultUtils.fail(0, "服务器操作异常:\n " + ExceptionUtils.getStackTrace(e));
        }

        return JsonResultUtils.success();
    }


}