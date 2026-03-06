package com.tutoringsys.common.util;

import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {
    public void set(String key, Object value) {
        // Mock implementation
    }

    public void set(String key, Object value, long timeout, TimeUnit unit) {
        // Mock implementation
    }

    public Object get(String key) {
        // Mock implementation
        return null;
    }

    public Boolean delete(String key) {
        // Mock implementation
        return true;
    }

    public Boolean expire(String key, long timeout, TimeUnit unit) {
        // Mock implementation
        return true;
    }

    public Long incr(String key) {
        // Mock implementation
        return 1L;
    }
}
