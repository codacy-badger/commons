/*
 * Copyright 2017 Pamarin.com
 */
package com.pamarin.oauth2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

/**
 *
 * @author jitta
 * @param <T>
 */
public abstract class RedisCacheStoreAdaptor<T> implements CacheStore<T> {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(RedisCacheStoreAdaptor.class);

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    protected abstract String getPrefix();

    private Class<T> typeClass;

    private String makeKey(String key) {
        return getPrefix() + ":" + key;
    }

    @Override
    public void cache(String key, T value) {
        try {
            String fullKey = makeKey(key);
            String json = objectMapper.writeValueAsString(value);
            LOG.debug("Redis set \"{}\" = {}", key, json);
            redisTemplate.opsForValue().set(fullKey, json, getExpiresMinutes(), TimeUnit.MINUTES);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException("Can't parse object to JSON string", ex);
        }
    }

    @Override
    public T get(String key) {
        try {
            String fullKey = makeKey(key);
            String value = redisTemplate.opsForValue().get(fullKey);
            LOG.debug("Redis get \"{}\" = {}", key, value);
            return objectMapper.readValue(value, typeClass);
        } catch (IOException ex) {
            throw new RuntimeException("Can't parse JSON string to object", ex);
        }
    }

    @Override
    public void delete(String key) {
        redisTemplate.delete(makeKey(key));
    }

}
