package com.qunar.qchat.service;

import com.qunar.qchat.constants.Config;
import com.qunar.qchat.model.IosPushModel;
import org.apache.http.util.TextUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * create by hubo.hu (lex) at 2018/12/27
 */
@Service
public class QIosPushServer {
    private HashMap<String, IosPushModel> serviceMap = null;
    private HashMap<String, IosPushModel> serviceMapBeta = null;

    public QIosPushServer() {
        init();
    }

    private void init() {
        if(serviceMap == null) {
            serviceMap = new HashMap<>();
            //prid
            if(!TextUtils.isEmpty(Config.IOS_PUSH_CERT_QTALK)
                    && !TextUtils.isEmpty(Config.IOS_PUSH_PKG_QTALK)
                    && !TextUtils.isEmpty(Config.IOS_PUSH_CERT_PWD)) {
                final String qtalkpro = Config.class.getClassLoader().getResource(Config.IOS_PUSH_CERT_QTALK).getPath();
                serviceMap.put(Config.IOS_PUSH_PKG_QTALK, new IosPushModel(qtalkpro, Config.IOS_PUSH_CERT_PWD));
            }else if(!TextUtils.isEmpty(Config.IOS_PUSH_AUTH_TEAM_ID)
                    && !TextUtils.isEmpty(Config.IOS_PUSH_AUTH_TOKEN_ID)
                    && !TextUtils.isEmpty(Config.IOS_PUSH_AUTH_TOKEN_FILE)
                    && !TextUtils.isEmpty(Config.IOS_PUSH_AUTH_TOPIC)){
                final String qtalkpro = Config.class.getClassLoader().getResource(Config.IOS_PUSH_AUTH_TOKEN_FILE).getPath();
                serviceMap.put(Config.IOS_PUSH_AUTH_TOPIC, new IosPushModel(qtalkpro, Config.IOS_PUSH_AUTH_TEAM_ID, Config.IOS_PUSH_AUTH_TOKEN_ID));
            }
        }
        if(serviceMapBeta == null) {
            serviceMapBeta = new HashMap<>();
            //beta
            if(!TextUtils.isEmpty(Config.IOS_PUSH_CERT_QTALK_BETA)
                    && !TextUtils.isEmpty(Config.IOS_PUSH_PKG_BETA_QTALK)
                    && !TextUtils.isEmpty(Config.IOS_PUSH_CERT_PWD)) {
                final String qtalkdev = Config.class.getClassLoader().getResource(Config.IOS_PUSH_CERT_QTALK_BETA).getPath();
                serviceMapBeta.put(Config.IOS_PUSH_PKG_BETA_QTALK, new IosPushModel(qtalkdev, Config.IOS_PUSH_CERT_PWD));
            }
        }
    }

    public boolean isBeta(String bid) {
        return serviceMapBeta.containsKey(bid);
    }

    public boolean isProd(String bid) {
        return serviceMap.containsKey(bid);
    }

    public IosPushModel getBetaCert(String bid) {
        return serviceMapBeta.get(bid);
    }

    public IosPushModel getProCert(String bid) {
        return serviceMap.get(bid);
    }



}
