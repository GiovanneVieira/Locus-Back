package com.project.locusapi.service;

import com.project.locusapi.exception.business.InvalidOtpException;
import com.project.locusapi.exception.business.OtpExpiredException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class OTPService {

    private static final String REDIS_OTP_PREFIX = "otp:";
    private static final String REDIS_OTP_TOKEN_SUFIX = "otp-token:";
    private static final int EXPIRE_MINUTES = 5;
    private final StringRedisTemplate redisTemplate;

    public OTPService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String generateAndSaveOtp(String email) {

        SecureRandom random = new SecureRandom();
        String code = String.format("%06d", random.nextInt(100000));
        String key = REDIS_OTP_PREFIX + email;

        redisTemplate.opsForValue().set(key, code, EXPIRE_MINUTES, TimeUnit.MINUTES);

        return code;
    }

    public String validateOtp(String email, String codeInput) {
        String key = REDIS_OTP_PREFIX + email;
        String savedCode = redisTemplate.opsForValue().get(key);

        if (savedCode == null) {
            throw new OtpExpiredException();
        }

        if (!savedCode.equals(codeInput)) {
            throw new InvalidOtpException();
        }
        redisTemplate.delete(key);

        return generateToken(email);

    }

    public String generateToken(String email) {
        String key = REDIS_OTP_TOKEN_SUFIX + email;
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(key, token, EXPIRE_MINUTES, TimeUnit.MINUTES);
        return token;
    }

    public boolean validateToken(String token, String email) {
        String key = REDIS_OTP_TOKEN_SUFIX + email;
        String savedToken = redisTemplate.opsForValue().get(key);
        if (savedToken == null) {
            throw new InvalidOtpException();
        }
        if (!savedToken.equals(token)) {
            throw new InvalidOtpException();
        }
        redisTemplate.delete(key);
        return true;
    }

}