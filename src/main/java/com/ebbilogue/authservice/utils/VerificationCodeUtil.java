package com.ebbilogue.authservice.utils;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Component
public class VerificationCodeUtil {
    
    private final RedisTemplate<String, String> redisTemplate;
    private static final String CODE_PREFIX = "verification_code:";
    private static final int CODE_LENGTH = 6;
    private static final long CODE_EXPIRATION = 10; // 10分钟过期
    
    public VerificationCodeUtil(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    // 生成6位数字验证码
    public String generateCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
    
    // 保存验证码到Redis
    public void saveCode(String email, String code) {
        String key = CODE_PREFIX + email;
        redisTemplate.opsForValue().set(key, code, CODE_EXPIRATION, TimeUnit.MINUTES);
    }
    
    // 验证验证码
    public boolean verifyCode(String email, String code) {
        String key = CODE_PREFIX + email;
        String savedCode = redisTemplate.opsForValue().get(key);
        if (savedCode != null && savedCode.equals(code)) {
            // 验证成功后删除验证码
            redisTemplate.delete(key);
            return true;
        }
        return false;
    }
} 