package com.project.couponcore.repository.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RedisRepository {

    private final RedisTemplate<String, String> redisTemplate;

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
}
