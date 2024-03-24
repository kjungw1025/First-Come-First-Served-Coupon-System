package com.project.couponcore.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.couponcore.model.CouponRedisEntity;
import com.project.couponcore.repository.redis.RedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AsyncCouponIssueServiceV2 {
    private final RedisRepository redisRepository;
    private final CouponCacheService couponCacheService;

    public void issue(long couponId, long userId) {

        // 캐시를 통해 쿠폰에 대한 유효성 검증 수행
        CouponRedisEntity coupon = couponCacheService.getCouponCache(couponId);
        coupon.checkIssuableCoupon();

        // Redis Script 내용 수행
        issueRequest(couponId, userId, coupon.totalQuantity());
    }

    /**
     * 쿠폰 발급 요청 추가
     */
    private void issueRequest(long couponId, long userId, Integer totalIssueQuantity) {
        // null인 경우가 존재하므로 MAX_VALUE로 설정하여 검증 우회
        if (totalIssueQuantity == null) {
            redisRepository.issueRequest(couponId, userId, Integer.MAX_VALUE);
        }
        redisRepository.issueRequest(couponId, userId, totalIssueQuantity);
    }
}
