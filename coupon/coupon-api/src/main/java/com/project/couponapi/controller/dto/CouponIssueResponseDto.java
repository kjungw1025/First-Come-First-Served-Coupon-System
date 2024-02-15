package com.project.couponapi.controller.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(value = JsonInclude.Include.NON_NULL) // comment가 null인 경우에는 데이터를 보내지 않도록 설정
public record CouponIssueResponseDto(boolean isSuccess, String comment) {
}
