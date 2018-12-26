package com.qunar.qchat.service;

import com.alibaba.fastjson.JSON;
import com.qunar.qchat.constants.AdrPushConstants;
import com.qunar.qchat.dao.IPlatKeyDao;
import com.qunar.qchat.dao.model.PushInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * create by hubo.hu (lex) at 2018/4/27
 */
@Service
public class PushInfoServiceImpl implements PushInfoService {
//    private static final Logger LOG = LoggerFactory.getLogger(PushInfoServiceImpl.class);

    @Autowired
    private IPlatKeyDao platKeyDao;

    @Cacheable(value = "pushinfo_cache", key = "'pushinfo:'+#username+'_'+#host")
// @Cacheable("pushinfo_cache")
    @Override
    public String cachePushInfo(String username, String host) {
//        LOG.info("visit pushinfo service getPushInfo,username:{} host:{}", username, host);
        PushInfo info = platKeyDao.selectPlatKey(AdrPushConstants.TABLE_NAME_PUSH, username, host);
//        LOG.info("visit pushinfo service 测试 infos:{}", JSON.toJSON(infos));
        if (info == null) {
            return "";
        }
        String json = JSON.toJSONString(info);
//        LOG.info("visit pushinfo service getPushInfo,json:{}", json);
        return json;
    }

    @CacheEvict(value = "pushinfo_cache", key = "'pushinfo:'+#username+'_'+#host")
    public boolean clearPushinfoCache(String username, String host){
        return true;
    }

    @CacheEvict(value = "user_group_notify_cache", key = "'user:grop:subcribe:'+#username+'_'+#host+'_'+#mucname")
    public boolean clearGroupNotifyCache(String username, String host, String mucname){
        return true;
    }
}
