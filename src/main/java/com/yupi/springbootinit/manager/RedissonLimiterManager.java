package com.yupi.springbootinit.manager;

import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author Han
 * @data 2023/10/8
 * @apiNode
 */
@Service
public class RedissonLimiterManager {

    @Resource
    private RedissonClient redissonClient;

    public void doRateLimit(String key) {
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
        // 创建一个限流器，每秒支持访问1次
        rateLimiter.trySetRate(RateType.OVERALL,1,1, RateIntervalUnit.SECONDS);
        // 获取林令牌
        boolean b = rateLimiter.tryAcquire(1);
        if (!b) {
            // 请求频繁
            throw new BusinessException(ErrorCode.PIN_FAN_QING_QIU);
        }

    }

}
