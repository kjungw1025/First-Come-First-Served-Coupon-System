package com.project.couponcore.service;

import com.project.couponcore.model.Coupon;
import com.project.couponcore.model.CouponRedisEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.aop.framework.AopContext;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponCacheService {

    private final CouponIssueService couponIssueService;

    @Cacheable(cacheNames = "coupon")
    public CouponRedisEntity getCouponCache(long couponId) {
        Coupon coupon = couponIssueService.findCoupon(couponId);
        return new CouponRedisEntity(coupon);
    }

    /**
     * 기본적으로는 로컬 캐시안에 데이터가 존재하면, 로컬 캐시에서 반환하고
     * 존재하지 않는다면, 레디스 캐시에서 읽어오도록 설정
     */
    @Cacheable(cacheNames = "coupon", cacheManager = "localCacheManager")
    public CouponRedisEntity getCouponLocalCache(long couponId) {
        return proxy().getCouponCache(couponId);
    }

    private CouponCacheService proxy() {
        return ((CouponCacheService) AopContext.currentProxy());
    }
}
