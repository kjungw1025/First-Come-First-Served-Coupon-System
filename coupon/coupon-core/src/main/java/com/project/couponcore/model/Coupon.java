package com.project.couponcore.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity
@Table(name = "coupons")
public class Coupon extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private CouponType couponType;

    // totalQuantity는 Null 허용할 예정 -> Integer
    private Integer totalQuantity;

    @Column(nullable = false)
    private int issuedQuantity;

    @Column(nullable = false)
    private int discountAmount;

    @Column(nullable = false)
    private int minAvailableAmount;

    @Column(nullable = false)
    private LocalDateTime dateIssueStart;

    @Column(nullable = false)
    private LocalDateTime dateIssueEnd;


    /**
     * 발급 가능 쿠폰 수량 검증
     */
    public boolean availableIssueQuantity() {
        if (totalQuantity == null) {
            return true;
        }
        return totalQuantity > issuedQuantity;
    }

    /**
     * 발급 기한 검증
     */
    public boolean availableIssueDate() {
        LocalDateTime now = LocalDateTime.now();
        return dateIssueStart.isBefore(now) && dateIssueEnd.isAfter(now);
    }

    /**
     * 발급 쿠폰 수량 증가
     */
    public void issue() {
        if (!availableIssueQuantity()) {
            throw new RuntimeException("수량 검증");
        }

        if (!availableIssueDate()) {
            throw new RuntimeException("기한 검증");
        }

        issuedQuantity++;
    }
}
