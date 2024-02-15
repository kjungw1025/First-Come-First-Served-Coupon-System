package com.project.couponcore.service;

import com.project.couponcore.exception.CouponIssueException;
import com.project.couponcore.exception.ErrorCode;
import com.project.couponcore.model.Coupon;
import com.project.couponcore.model.CouponIssue;
import com.project.couponcore.repository.mysql.CouponIssueJpaRepository;
import com.project.couponcore.repository.mysql.CouponIssueRepository;
import com.project.couponcore.repository.mysql.CouponJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CouponIssueService {
    private final CouponJpaRepository couponJpaRepository;
    private final CouponIssueJpaRepository couponIssueJpaRepository;
    private final CouponIssueRepository couponIssueRepository;

    /**
     * 실제 쿠폰에 대한 발급을 검증하고, 발급 처리 진행
     */
    @Transactional
    public void issue(long couponId, long userId) {
        Coupon coupon = findCoupon(couponId);
        coupon.issue();
        saveCouponIssue(couponId, userId);
    }

    /**
     * 쿠폰 번호를 통해 쿠폰 찾기
     */
    @Transactional(readOnly = true)
    public Coupon findCoupon(long couponId) {
        return couponJpaRepository.findById(couponId).orElseThrow(() -> {
            throw new CouponIssueException(ErrorCode.COUPON_NOT_EXIST, "쿠폰 정책이 존재하지 않습니다. %s".formatted(couponId));
        });
    }

    /**
     * 어떤 유저가 어떤 쿠폰을 발급 받았는지 기록
     */
    @Transactional
    public CouponIssue saveCouponIssue(long couponId, long userId) {
        checkAlreadyIssuance(couponId, userId);
        CouponIssue issue = CouponIssue.builder()
                .couponId(couponId)
                .userId(userId)
                .build();
        return couponIssueJpaRepository.save(issue);
    }

    /**
     * 이미 쿠폰이 발급되었는지 확인
     */
    private void checkAlreadyIssuance(long couponId, long userId) {
        CouponIssue issue = couponIssueRepository.findFirstCouponIssue(couponId, userId);

        if (issue != null) {
            throw new CouponIssueException(ErrorCode.DUPLICATED_COUPON_ISSUE, "이미 발급된 쿠폰입니다. user_id : %s, coupon_id : %s".formatted(userId, couponId));
        }
    }
}
