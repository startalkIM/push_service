package com.qunar.qchat.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.serializer.RedisSerializer;
import redis.clients.util.Hashing;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RedisUtil {
    private static final Logger logger = LoggerFactory.getLogger(RedisUtil.class);
    private static volatile StringRedisTemplate redisTemplate;
    private static final ObjectMapper objectMapper = new ObjectMapper();


    public static <T> void set(int table,String key, T value, long timeout, TimeUnit timeUnit) {
        ((JedisConnectionFactory) redisTemplate.getConnectionFactory()).setDatabase(table);
        set(key,value,timeout,timeUnit);
    }

        /**
         * 向redis存数据，利用Jackson对对象进行序列化后再存储
         *
         * @param key      key
         * @param value    value
         * @param timeout  过期时间
         * @param timeUnit 过期时间的单位 {@link TimeUnit}
         * @param <T>      value的类型
         */
    public static <T> void set(String key, T value, long timeout, TimeUnit timeUnit) {
        try {
            ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
         //   byte[] valueStr = redisTemplate.getValueSerializer().serialize(value);
        //    String valueStr = JacksonUtil.obj2String(value);
            String valueStr = obj2String(value);
            valueOperations.set(key, valueStr, timeout, timeUnit);
        } catch (Exception e) {
            logger.error("向redis中存数据失败，key:{},value:{}", key, value, e);
        }

    }

    /**
     * 从redis获取数据，利用Jackson对json进行反序列化
     *
     * @param key   key
     * @param clazz 需要反序列化成的对象的class对象
     * @param <T>   class对象保留的对象类型
     * @return 取出来并反序列后的对象
     */

    public static <T> T get(int table,String key, Class<T> clazz) {
        ((JedisConnectionFactory) redisTemplate.getConnectionFactory()).setDatabase(table);
        return get(key,clazz);
    }

    public static <T> T get(String key, Class<T> clazz) {
        try {
            ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
            String valueStr = valueOperations.get(key);
            return string2Obj(valueStr, clazz);
        } catch (Exception e) {
            logger.error("从redis中取数据失败，key:{}", key, e);
            return null;
        }
    }


    public static <T> T get(int table,String key, TypeReference<T> typeReference) {
        ((JedisConnectionFactory) redisTemplate.getConnectionFactory()).setDatabase(table);
        return get(key,typeReference);
    }

    /**
     * 从redis获取数据，利用Jackson对json进行反序列化
     *
     * @param key           key
     * @param typeReference 需要反序列成的对象，针对有泛型的对象
     * @param <T>           保留的对象类型
     * @return 取出来并反序列化后的对象
     */
    public static <T> T get(String key, TypeReference<T> typeReference) {
        try {
            ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
            return string2Obj(valueOperations.get(key), typeReference);
        } catch (Exception e) {
            logger.error("从redis中取数据失败，key:{}", key, e);
            return null;
        }
    }

    public static void remove(int table,String key) {
        ((JedisConnectionFactory) redisTemplate.getConnectionFactory()).setDatabase(table);
        remove(key);
    }


        /**
         * 把redis的某个key-value对进行删除
         *
         * @param key key
         */
    public static void remove(String key) {
        try {
            ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
            valueOperations.getOperations().delete(key);
        } catch (Exception e) {
            logger.error("从redis中删除数据失败，key:{}", key, e);
        }
    }

    /**
     * @param redisKey   key
     * @param increValue increValue
     * @param <T>        类型
     */
    @SuppressWarnings("unchecked")
    public static <T> void incr(final T redisKey, final long increValue) {
        redisTemplate.execute(new RedisCallback<T>() {
            @Override
            public T doInRedis(RedisConnection connection) throws DataAccessException {
                RedisSerializer redisSerializer = redisTemplate.getValueSerializer();
                byte[] key = redisSerializer.serialize(redisKey);
                connection.incrBy(key, increValue);
                return null;
            }
        });

    }

    public static  String hGet(int table,String key, String hashKey) {
        ((JedisConnectionFactory) redisTemplate.getConnectionFactory()).setDatabase(table);
        return hGet(key,hashKey);
    }

    public static String hGet(String key, String hashKey) {
        try {
            HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
            return hashOperations.get(key, hashKey);
        } catch (Exception e) {
            logger.error("从redis中获取hash数据失败，key:{},hashKey:{}", key, hashKey, e);
            return null;
        }
    }

    public static Map<String,String> hGetAll(int table,String key) {
        ((JedisConnectionFactory) redisTemplate.getConnectionFactory()).setDatabase(table);
        return hGetAll(key);
    }

        public static Map<String,String> hGetAll(String key) {
        try {
            HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
            return hashOperations.entries(key);

        } catch (Exception e) {
            logger.error("从redis中获取hGetAll数据失败，key:{},key:{}", key, e);
            return null;
        }
    }

    public static <T> void hPut(int table,String key, String hashKey, T hashValue) {
        ((JedisConnectionFactory) redisTemplate.getConnectionFactory()).setDatabase(table);
        hPut(key,hashKey,hashValue);
    }

    public static <T> void hPut(String key, String hashKey, T hashValue) {
        try {
            if (null == redisTemplate){
//                int a = 10;
//                a = 20;
            }

            HashOperations<String, String, T> hashOperations = redisTemplate.opsForHash();
            hashOperations.put(key, hashKey, hashValue);
        } catch (Exception e) {
            logger.error("向redis中插入hash数据失败，key:{},hashKey:{},hashValue:{}", key, hashKey, hashValue, e);
        }
    }

    public static <T> void hDel(int table,String key, String hashKey) {
        ((JedisConnectionFactory) redisTemplate.getConnectionFactory()).setDatabase(table);
        hDel(key,hashKey);
    }

    public static <T> void hDel(String key, String hashKey) {
        try {
            if (null == redisTemplate){
//                int a = 10;
//                a = 20;
            }
            HashOperations<String, String, T> hashOperations = redisTemplate.opsForHash();
            hashOperations.delete(key,hashKey);
        } catch (Exception e) {
            logger.error("向redis中删除hash数据失败，key:{},hashKey:{},e:{}", key, hashKey, e);
        }

    }

    /**
     * redis的key生成器
     *
     * @param prefix  key的前缀
     * @param objects 后面加的一些唯一标示
     * @return key
     */
    public static String keyGenerator(String prefix, Object... objects) {
        StringBuilder sb = new StringBuilder(prefix);
        for (Object obj : objects) {
            sb.append("_").append(obj.toString());
        }
        return Long.toString(Hashing.MURMUR_HASH.hash(sb.toString()));
    }

    public static <T> String obj2String(T obj) {

        if (obj == null) {
            return null;
        }
        try {
            return obj instanceof String ? (String) obj : objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            logger.error("serialize Object to String failed,Object:{}", obj.getClass(), e);
            return null;
        }
    }

    public static <T> T string2Obj(String json, Class<T> clazz) {
        if (StringUtils.isBlank(json) || clazz == null) {
            return null;
        }
        try {
            return String.class.equals(clazz) ? (T) json : objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            logger.error(" deserialize String to Object failed,json:{},class:{}", json, clazz.getName(), e);
            return null;
        }
    }

    public static <T> T string2Obj(String json, TypeReference<T> typeReference) {
        if (StringUtils.isBlank(json) || typeReference == null) {
            return null;
        }
        try {
            return (T) (String.class.equals(typeReference.getType()) ? json : objectMapper.readValue(json, typeReference));
        } catch (IOException e) {
            logger.error(" deserialize String to Object failed,json:{},type:{}", json, typeReference.getType(), e);
            return null;
        }
    }


    /**
     * 提供给xml注入使用，请不要调用该方法,为了防止该方法混入静态方法，请把该方法始终放在该类最后
     *
     * @param _redisTemplate StringRedisTemplate
     */
    public static void setRedisTemplate(StringRedisTemplate _redisTemplate) {
        redisTemplate = _redisTemplate;
    }
}
