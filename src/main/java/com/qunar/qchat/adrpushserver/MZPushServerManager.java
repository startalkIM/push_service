package com.qunar.qchat.adrpushserver;

import com.alibaba.fastjson.JSONObject;
import com.meizu.push.sdk.server.IFlymePush;
import com.meizu.push.sdk.server.constant.ResultPack;
import com.meizu.push.sdk.server.model.push.PushResult;
import com.meizu.push.sdk.server.model.push.VarnishedMessage;
import com.qunar.qchat.constants.AdrPushConstants;
import com.qunar.qchat.constants.Config;
import com.qunar.qchat.constants.SchemeConstants;
import com.qunar.qchat.dao.model.NotificationInfo;
import com.qunar.qchat.utils.ProtoMessageOuterClass;
import org.apache.http.client.utils.URIBuilder;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * create by hubo.hu (lex) at 2018/8/23
 */
public class MZPushServerManager implements QPushServerManager{

    private final String appPackageName;
    private final String appSecretKey;
    public static final long MEIZU_APP_ID = Config.MZPUSH_QT_SECRET_APPID;
    private static final Logger LOGGER = LoggerFactory.getLogger(MZPushServerManager.class);

    public MZPushServerManager(String appPackageName, String appSecretKey) {
        this.appPackageName = appPackageName;
        this.appSecretKey = appSecretKey;
    }

    public VarnishedMessage getNotificationMessage(NotificationInfo notificationInfo) {
        //组装消息

        URIBuilder uriBuilder = new URIBuilder();
        uriBuilder.setScheme(SchemeConstants.getScheme(true));
        if(notificationInfo.type == ProtoMessageOuterClass.SignalType.SignalTypeGroupChat_VALUE) {
            uriBuilder.setHost(SchemeConstants.HOST_GROUPCHAT);
        } else {
            uriBuilder.setHost(SchemeConstants.HOST_SINGLECHAT);
        }
        uriBuilder.addParameter("jid", notificationInfo.fromjid);
        uriBuilder.addParameter("type", notificationInfo.type + "");
        uriBuilder.addParameter("chatid", notificationInfo.chatid);
        uriBuilder.addParameter("realjid", notificationInfo.realjid);
//        String scheme = "qtalkaphone://qunarchat/openSingleChat?jid=" + notificationInfo.fromjid;

        Map map = new HashMap<>();
        map.put("type", notificationInfo.type);
        map.put("jid", notificationInfo.fromjid);
        map.put("chatid", notificationInfo.chatid);
        map.put("realjid", notificationInfo.realjid);

        VarnishedMessage message = new VarnishedMessage.Builder().appId(MEIZU_APP_ID)
                .title(notificationInfo.title)
                .content(notificationInfo.description)
                .validTime(1)
//                .clickType(3)
//                .customAttribute(scheme)
                .clickType(1)
                .activity("com.qunar.im.ui.activity.TabMainActivity")
                .parameters(new JSONObject(map))
//                .clickType(2)
//                .url(uriBuilder.toString())
                .build();

        return message;
    }

    @Override
    public void sendMessageToAlias(List<String> alias, String title, String messagePayload) throws IOException, ParseException {

    }

    @Override
    public void sendMessageToTags(List<String> tags, String messagePayload) throws IOException, ParseException {

    }

    @Override
    public void sendMessageToAll(String messagePayload) throws IOException, ParseException {

    }

    @Override
    public void sendNotifyToAlias(NotificationInfo notificationInfo) throws IOException, ParseException {
        //推送对象
        IFlymePush push = new IFlymePush(appSecretKey);

        VarnishedMessage message = getNotificationMessage(notificationInfo);

        //目标用户
        List<String> pushIds = new ArrayList<String>();
        for(String alias : notificationInfo.platkeys) {
            pushIds.add(alias);
        }
        // 1 调用推送服务
        ResultPack<PushResult> result = push.pushMessage(message, pushIds);
        if (result.isSucceed()) {
            // 2 调用推送服务成功 （其中map为设备的具体推送结果，一般业务针对超速的code类型做处理）
            PushResult pushResult = result.value();
//            String msgId = pushResult.getMsgId();//推送消息ID，用于推送流程明细排查
            Map<String, List<String>> targetResultMap = pushResult.getRespTarget();//推送结果，全部推送成功，则map为empty
            if (targetResultMap != null && !targetResultMap.isEmpty()) {
                LOGGER.info("mzpush qtalk send failed token={} touser={} ", targetResultMap, notificationInfo.toUserName);
            }
            LOGGER.info("mzpush qtalk send key={} result={} touser={}", pushIds.get(0) , pushResult.toString(), notificationInfo.toUserName);
        } else {
            // 调用推送接口服务异常 eg: appId、appKey非法、推送消息非法.....
            // result.code(); //服务异常码
            // result.comment();//服务异常描述
            LOGGER.info("mzpush qtalk send failed code={} comment={} touser={}", result.code(), result.comment(), notificationInfo.toUserName);
        }
    }

    @Override
    public void sendNotifyToTags(List<String> tags, String title, String description, String messagePayload) throws IOException, ParseException {

    }

    @Override
    public void sendNotifyToAll(String title, String description, String messagePayload) throws IOException, ParseException {

    }

    @Override
    public void sendLinkNotifyToAlias(List<String> alias, String title, String description, String url) throws IOException, ParseException {

    }

    @Override
    public void sendLinkNotifyToTags(List<String> tags, String title, String description, String url) throws IOException, ParseException {

    }

    @Override
    public void sendLinkNotifyToAll(String title, String description, String url) throws IOException, ParseException {

    }

    @Override
    public String getName() {
        return AdrPushConstants.NAME_MEIZU;
    }

    @Override
    public String getPkgname() {
        return appPackageName;
    }
}
