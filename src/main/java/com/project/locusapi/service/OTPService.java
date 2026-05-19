package com.project.locusapi.service;

import com.project.locusapi.exception.business.InvalidOtpException;
import com.project.locusapi.exception.business.OtpExpiredException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class OTPService {

    private static final String REDIS_PREFIX = "otp:";
    private static final int EXPIRE_MINUTES = 5;
    private final StringRedisTemplate redisTemplate;

    public OTPService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String generateAndSaveOtp(String email) {
        String code = String.format("%06d", new Random().nextInt(999999));
        String key = REDIS_PREFIX + email;

        redisTemplate.opsForValue().set(key, code, EXPIRE_MINUTES, TimeUnit.MINUTES);

        return code;
    }

    public boolean validateOtp(String email, String codeInput) {
        String key = REDIS_PREFIX + email;
        String savedCode = redisTemplate.opsForValue().get(key);

        if (savedCode == null) {
            throw new OtpExpiredException();
        }

        if (!savedCode.equals(codeInput)) {
            throw new InvalidOtpException();
        }

        redisTemplate.delete(key);
        return true;
    }
}