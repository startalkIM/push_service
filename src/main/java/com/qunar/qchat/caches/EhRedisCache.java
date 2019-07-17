package com.qunar.qchat.caches;

import com.qunar.qchat.utils.RedisUtil;
import net.sf.ehcache.Element;
import org.apache.http.util.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * create by hubo.hu (lex) at 2018/4/26
 * 两级缓存，一级:ehcache,二级为redisCache
 */
public class EhRedisCache implements Cache {
    private static final Logger LOG = LoggerFactory.getLogger(EhRedisCache.class);

    public static final int REDIS_PUSH_TABLE = 9;
    private String name;
    private net.sf.ehcache.Cache ehCache;
    private RedisTemplate<String, Object> redisTemplate;
    public static long liveTime = 2 * 60 * 60; //默认2h

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Object getNativeCache() {
        return this;
    }

    @Override
    public ValueWrapper get(Object key) {
        try {
            Element value = ehCache.get(key);
//        LOG.info("Cache L1 (ehcache)get :{}={}", key, value);
            if (value != null) {
                return new SimpleValueWrapper(value.getObjectValue());
            }
            String objectValue = RedisUtil.get(REDIS_PUSH_TABLE, key.toString(), String.class);
//        LOG.info("Cache L2 (redis)get :{}={}", key, objectValue);
            if(TextUtils.isEmpty(objectValue)) {
                return null;
            }
            //每次获得，重置缓存过期时间
            RedisUtil.set(REDIS_PUSH_TABLE, key.toString(), objectValue, liveTime, TimeUnit.SECONDS);
            ehCache.put(new Element(key, objectValue));
            //取出来之后缓存到本地
//        LOG.info("Cache L2 (redis) :{}={}", key, objectValue);
            return new SimpleValueWrapper(objectValue);
        } catch (Exception e) {
            LOG.error("processChatMessage Exception={} ", e);
        }
        return null;
    }

    @Override
    public <T> T get(Object key, Class<T> type) {

//        LOG.info("Cache L1 (ehcache) :{}={}", key, type);
        return null;
    }

    @Override
    public <T> T get(Object o, Callable<T> callable) {
        return null;
    }

    @Override
    public void put(Object key, Object value) {
//        LOG.info("Cache L1 (ehcache)put :{}={}", key, value);
        if(key == null || value == null) {
            return;
        }
//        LOG.info("Cache L1 (ehcache)put2 :{}={}", key, value);
        ehCache.put(new Element(key, value));
        RedisUtil.set(REDIS_PUSH_TABLE, key.toString(), value.toString(), liveTime, TimeUnit.SECONDS);
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
//        LOG.info("Cache L1 (ehcache)putIfAbsent :{}={}", key, value);
        return null;
    }

    @Override
    public void evict(Object key) {
//        LOG.info("Cache L1 (ehcache)evict :key={}", key);
        if(key == null) {
            return;
        }
        ehCache.remove(key);
        RedisUtil.remove(REDIS_PUSH_TABLE, key.toString());
    }

    @Override
    public void clear() {
        ehCache.removeAll();
        redisTemplate.execute(new RedisCallback<String>() {
            public String doInRedis(RedisConnection connection) throws DataAccessException {
                connection.flushDb();
                return "clear done.";
            }
        }, true);
    }

    public net.sf.ehcache.Cache getEhCache() {
        return ehCache;
    }

    public void setEhCache(net.sf.ehcache.Cache ehCache) {
        this.ehCache = ehCache;
    }

    public RedisTemplate<String, Object> getRedisTemplate() {
        return redisTemplate;
    }

    public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void setName(String name) {
        this.name = name;
    }

}