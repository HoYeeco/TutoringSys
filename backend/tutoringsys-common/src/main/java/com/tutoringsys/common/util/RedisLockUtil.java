package com.tutoringsys.common.util;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Component
public class RedisLockUtil {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 尝试获取分布式锁
     * @param lockKey 锁键
     * @param requestId 请求ID
     * @param expireTime 过期时间（秒）
     * @return 是否获取成功
     */
    public boolean tryLock(String lockKey, String requestId, long expireTime) {
        return redisTemplate.opsForValue().setIfAbsent(lockKey, requestId, expireTime, TimeUnit.SECONDS);
    }

    /**
     * 释放分布式锁
     * @param lockKey 锁键
     * @param requestId 请求ID
     * @return 是否释放成功
     */
    public boolean releaseLock(String lockKey, String requestId) {
        String value = (String) redisTemplate.opsForValue().get(lockKey);
        if (requestId.equals(value)) {
            return redisTemplate.delete(lockKey);
        }
        return false;
    }

    /**
     * 生成作业提交锁键
     * @param studentId 学生ID
     * @param assignmentId 作业ID
     * @return 锁键
     */
    public String generateAssignmentLockKey(Long studentId, Long assignmentId) {
        return "assignment:lock:" + studentId + ":" + assignmentId;
    }
}