package com.qunar.qchat.service;

import com.qunar.qchat.aop.routingdatasource.DataSources;
import com.qunar.qchat.aop.routingdatasource.RoutingDataSource;
import com.qunar.qchat.constants.AdrPushConstants;
import com.qunar.qchat.dao.IHostUserDao;
import com.qunar.qchat.dao.IPlatKeyDao;
import com.qunar.qchat.utils.CommonRedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QTalkVCardService implements QVCardService {
    private static final Logger LOGGER = LoggerFactory.getLogger(QTalkVCardService.class);

    @Autowired
    private IPlatKeyDao platKeyDao;
    @Autowired
    private IHostUserDao hostUserDao;

    @Override
    @RoutingDataSource(DataSources.QIM_SLAVE)
    public String getUserName(String from, String fromhost) {
        String name = hostUserDao.selectName(AdrPushConstants.TABLE_NAME_HOST_USERS,
                AdrPushConstants.TABLE_NAME_HOST_INFO, from, fromhost);
        return name;
    }

    @Override
    @RoutingDataSource(DataSources.QIM_SLAVE)
    public String getMucName(String fromname) {
        String tempName = hostUserDao.selectMucName(AdrPushConstants.TABLE_NAME_MUC_VCARD, fromname);
        return tempName;
    }

    @Override
    @RoutingDataSource(DataSources.QIM_SLAVE)
    public void delQimPubOldPushToken(String username, String domain, String mac_key, String os, String version) {
        String table = "person_user_mac_key";
        try {
            platKeyDao.updateDelOldMacKey(table, username, domain, mac_key, os, version);
        } catch (Exception e) {
            LOGGER.error("catch error ", e);
        }
    }

    @Override
    @RoutingDataSource(DataSources.QIM_SLAVE)
    //    @Cacheable(value = "user_group_notify_cache", key = "'user:grop:subcribe:'+#username+'_'+#host+'_'+#mucname")
    public boolean isSubscribGroup(String username, String host, String mucname) {
        boolean isSubscript;
        int value = CommonRedisUtil.isSubscriptGroup(username, host, mucname);
        if(value == -1) {
            isSubscript = platKeyDao.selectGroupIsSubsribeFromClientConfig(AdrPushConstants.TABLE_NAME_CLIENT_CONFIG, username, host, mucname) == 0;
            CommonRedisUtil.setSubscriptGrop(username, host, mucname, isSubscript ? 1 : 0);
        } else {
            isSubscript = (value == 1);
        }

//        LOGGER.info("visit pushinfo service isSubscribGroup,username:{} host:{} result:{}", username, host, result);
        return isSubscript;
    }

}
