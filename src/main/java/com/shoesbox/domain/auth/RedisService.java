package com.shoesbox.domain.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@RequiredArgsConstructor
@Transactional
@Service
public class RedisService {
    private final RedisTemplate<String, String> redisTemplate;

    public String getData(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void setDataWithExpiration(String key, String value, Long time) {
        if (this.getData(key) != null) {
            redisTemplate.delete(key);
        }
        Duration expireDuration = Duration.ofMillis(time);
        redisTemplate.opsForValue().set(key, value, expireDuration);
    }
}
