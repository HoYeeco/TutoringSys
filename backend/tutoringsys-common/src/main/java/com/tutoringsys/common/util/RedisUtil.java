package com.tutoringsys.common.util;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {
    private RedisTemplate<String, Object> redisTemplate;

    public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void set(String key, Object value) {
        if (redisTemplate != null) {
            redisTemplate.opsForValue().set(key, value);
        }
    }

    public void set(String key, Object value, long timeout, TimeUnit unit) {
        if (redisTemplate != null) {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
        }
    }

    public Object get(String key) {
        if (redisTemplate != null) {
            return redisTemplate.opsForValue().get(key);
        }
        return null;
    }

    public Boolean delete(String key) {
        if (redisTemplate != null) {
            return redisTemplate.delete(key);
        }
        return false;
    }

    public Boolean expire(String key, long timeout, TimeUnit unit) {
        if (redisTemplate != null) {
            return redisTemplate.expire(key, timeout, unit);
        }
        return false;
    }

    public Long incr(String key) {
        if (redisTemplate != null) {
            return redisTemplate.opsForValue().increment(key);
        }
        return 0L;
    }
}