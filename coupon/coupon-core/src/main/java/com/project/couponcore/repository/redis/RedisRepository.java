package com.project.couponcore.repository.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.couponcore.exception.CouponIssueException;
import com.project.couponcore.model.dto.request.RequestCouponIssueDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.project.couponcore.exception.ErrorCode.FAIL_COUPON_ISSUE_REQUEST;
import static com.project.couponcore.util.CouponRedisUtils.getIssueRequestKey;
import static com.project.couponcore.util.CouponRedisUtils.getIssueRequestQueueKey;

@Repository
@RequiredArgsConstructor
public class RedisRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private final RedisScript<String> issueScript = issueRequestScript();
    private final String issueRequestQueueKey = getIssueRequestQueueKey();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 발급 요청 추가
    public Long sAdd(String key, String value) {
        return redisTemplate.opsForSet().add(key, value);
    }

    // 수량 조회
    public Long sCard(String key) {
        return redisTemplate.opsForSet().size(key);
    }

    // 중복 발급 요청 여부 확인
    public Boolean sIsMember(String key, String value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    // 쿠폰 발급 큐에 적재하기 위한 리스트 자료구조 활용
    public Long rPush(String key, String value) {
        return redisTemplate.opsForList().rightPush(key, value);
    }

    // --------------------- 발급에서 사용 ----------------------
    // 쿠폰 발급 큐의 사이즈 확인
    public Long lSize(String key) {
        return redisTemplate.opsForList().size(key);
    }

    // 인덱스 값 가져오지
    public String lIndex(String key, long index) {
        return redisTemplate.opsForList().index(key, index);
    }

    // 쿠폰 발급 큐에 적재되었던 내용을 pop 해주는 용도
    public String lPop(String key) {
        return redisTemplate.opsForList().leftPop(key);
    }
    // -------------------------------------------------------

    public void issueRequest(long couponId, long userId, int totalIssueQuantity) {
        String issueRequestKey = getIssueRequestKey(couponId);
        RequestCouponIssueDto requestCouponIssueDto = new RequestCouponIssueDto(couponId, userId);
        try {
            String code = redisTemplate.execute(
                    issueScript,
                    List.of(issueRequestKey, issueRequestQueueKey),
                    String.valueOf(userId),
                    String.valueOf(totalIssueQuantity),
                    objectMapper.writeValueAsString(requestCouponIssueDto)
            );

            CouponIssueRequestCode.checkRequestResult(CouponIssueRequestCode.find(code));

        } catch (JsonProcessingException e) {
            throw new CouponIssueException(FAIL_COUPON_ISSUE_REQUEST, "input: %s".formatted(requestCouponIssueDto));
        }
    }

    // Redis Script
    private RedisScript<String> issueRequestScript() {
        String script = """
                        if redis.call('SISMEMBER', KEYS[1], ARGV[1]) == 1 then
                            return '2'
                        end
                        
                        if tonumber(ARGV[2]) > redis.call('SCARD', KEYS[1]) then
                            redis.call('SADD', KEYS[1], ARGV[1])
                            redis.call('RPUSH', KEYS[2], ARGV[3])
                            return '1'
                        end
                        
                        return '3'
                        """;
        return RedisScript.of(script, String.class);
    }
}
