package com.qunar.qchat.redis.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * Created by qitmac000378 on 17/5/25.
 */
public abstract class AbstractBaseRedisDao<K, V> {
    @Autowired
    protected RedisTemplate<K, V> redisTemplate;

    public RedisSerializer<String> getRedisSerializer() {
        return redisTemplate.getStringSerializer();
    }

    public void setRedisTemplate(RedisTemplate<K, V> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
}
