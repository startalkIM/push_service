package com.qunar.qchat.caches;

import com.google.common.collect.Maps;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class QChactUserNameCache {

    private Map<String, Map<String, String>> cache = Maps.newConcurrentMap();
    private static final String CACHE_TIME = "cache_time";
    private static final String CACHE_CONTENT = "cache_content";

    public String get(String key){
        Map<String, String> map = cache.get(key);
        if(map == null){
            return "";
        }
        String lastTime = "0";
        long curTime = System.currentTimeMillis();
        if(map.containsKey(CACHE_TIME)){
            lastTime = map.get(CACHE_TIME);
        }
        if(curTime - Long.valueOf(lastTime) > 1000 * 60 * 60){//缓存1小时
            return "";
        }else {
            return map.get(CACHE_CONTENT);
        }
    }

    public void addOrUpdateCache(String key, String value){
        Map<String, String> map = new HashMap<>();
        map.put(CACHE_TIME, System.currentTimeMillis()+"");
        map.put(CACHE_CONTENT, value);
        cache.put(key, map);

    }

    public void evictCache(String key){
        if(cache.containsKey(key))
            cache.remove(key);
    }

    public void clear(){
        cache.clear();
    }

}
