package com.qunar.qchat.redis.impl;

import com.qunar.qchat.redis.IRedisDao;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * Created by qitmac000378 on 17/5/25.
 */
public class RedisDaoImpl extends AbstractBaseRedisDao<String,String> implements IRedisDao {
//    private static final Logger LOGGER = LoggerFactory.getLogger(RedisDaoImpl.class);

    private String STATE_KEY = "ejabberd:sm:other:";
    @Override
    public String getOnlineState(final String jid) {
        String result = "" ;
        if (null == redisTemplate)
            return result;
       // ((JedisConnectionFactory) redisTemplate.getConnectionFactory()).setDatabase(6);

        result = redisTemplate.execute(new RedisCallback<String>() {
            public String doInRedis(RedisConnection connection)
                    throws DataAccessException {
                connection.select(7);
                RedisSerializer<String> serializer = getRedisSerializer();
                byte[] key = serializer.serialize(STATE_KEY + jid);
                byte[] value = connection.get(key);
                if (value == null) {
                    return null;
                }
                String state = serializer.deserialize(value);
                return state;
            }
        });
        return result;
    }

}
