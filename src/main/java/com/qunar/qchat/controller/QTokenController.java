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
            if(map == null) map = new HashMap<>();
        } catch (Exception e) {
            LOGGER.debug("catch error ", e);
            return JsonResultUtils.fail(0, "服务器操作异常:\n " + ExceptionUtils.getStackTrace(e));
        }

        return JsonResultUtils.success(map);
    }

    @RequestMapping(value = "/sendMessageToPush.qunar", method = RequestMethod.POST)
    @RecordAccessLog
    public JsonResult<?> sendMessageToPush(@RequestBody String message) {

        try {
            LOGGER.info("接口sendMessageToPush消息msg:[{}]", message);
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