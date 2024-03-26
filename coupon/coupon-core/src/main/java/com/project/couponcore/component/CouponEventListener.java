package com.project.couponcore.component;

import com.project.couponcore.model.event.CouponIssueCompleteEvent;
import com.project.couponcore.service.CouponCacheService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class CouponEventListener {
    private final CouponCacheService couponCacheService;
    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

    /**
     * issueComplete가 오면, 해당하는 트랜잭션이 커밋된 이후에 아래 리스너가 실행되어
     * 레디스와 로컬 캐시를 모두 업데이트 해준다.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void issueComplete(CouponIssueCompleteEvent event) {
        log.info("issue complete. cache refresh start couponId: %s".formatted(event.couponId()));
        couponCacheService.putCouponCache(event.couponId());
        couponCacheService.putCouponLocalCache(event.couponId());
        log.info("issue complete. cache refresh end couponId: %s".formatted(event.couponId()));
    }
}
