package com.shoesbox.domain.auth.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@RequiredArgsConstructor
@Transactional
@Service
public class RedisService {

    private final RedisTemplate redisTemplate;

    public String getData(String key) {
        return (String) redisTemplate.opsForValue().get(key);
    }

    public void setDataWithExpiration(String key, String value, Long time) {
        if (this.getData(key) != null) {
            redisTemplate.delete(key);
        }
        Duration expireDuration = Duration.ofSeconds(time);
        redisTemplate.opsForValue().set(key, value, expireDuration);
    }
}
