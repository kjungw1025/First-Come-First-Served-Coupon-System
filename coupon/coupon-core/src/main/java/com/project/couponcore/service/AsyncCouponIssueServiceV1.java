package com.project.couponcore.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.couponcore.component.DistributeLockExecutor;
import com.project.couponcore.exception.CouponIssueException;
import com.project.couponcore.model.CouponRedisEntity;
import com.project.couponcore.model.dto.request.RequestCouponIssueDto;
import com.project.couponcore.repository.redis.RedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.project.couponcore.exception.ErrorCode.*;
import static com.project.couponcore.util.CouponRedisUtils.getIssueRequestKey;
import static com.project.couponcore.util.CouponRedisUtils.getIssueRequestQueueKey;

@Service
@RequiredArgsConstructor
public class AsyncCouponIssueServiceV1 {
    private final RedisRepository redisRepository;
    private final CouponIssueRedisService couponIssueRedisService;
    private final CouponCacheService couponCacheService;

    private final DistributeLockExecutor distributeLockExecutor;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void issue(long couponId, long userId) {

        // 캐시를 통해 쿠폰에 대한 유효성 검증 수행
        CouponRedisEntity coupon = couponCacheService.getCouponCache(couponId);
        coupon.checkIssuableCoupon();

        // 레디스 동시성 이슈 해결하기 위함
        distributeLockExecutor.execute("lock_%s".formatted(couponId), 3000, 3000, () -> {
            couponIssueRedisService.checkCouponIssueQuantity(coupon, userId);
            issueRequest(couponId, userId);
        });
    }

    /**
     * 쿠폰 발급 요청 추가
     */
    private void issueRequest(long couponId, long userId) {
        RequestCouponIssueDto requestCouponIssueDto = new RequestCouponIssueDto(couponId, userId);
        // String 타입으로 직렬화
        try {
            String value = objectMapper.writeValueAsString(requestCouponIssueDto);

            redisRepository.sAdd(getIssueRequestKey(couponId), String.valueOf(userId));
            // 쿠폰 발급 큐에 적재
            redisRepository.rPush(getIssueRequestQueueKey(), value);
        } catch (JsonProcessingException e) {
            throw new CouponIssueException(FAIL_COUPON_ISSUE_REQUEST, "input: %s".formatted(requestCouponIssueDto));
        }
    }
}
