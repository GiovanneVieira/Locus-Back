package com.project.locusapi.service;

import com.project.locusapi.constant.metrics.CriticalFailureType;
import com.project.locusapi.event.metrics.OtpValidationFailedEvent;
import com.project.locusapi.exception.business.InvalidOtpException;
import com.project.locusapi.exception.business.OtpExpiredException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class OTPService {

    private static final String REDIS_OTP_PREFIX = "otp:";
    private static final String REDIS_OTP_TOKEN_SUFIX = "otp-token:";
    private static final int EXPIRE_MINUTES = 5;
    private final StringRedisTemplate redisTemplate;
    private final ApplicationEventPublisher eventPublisher;

    public OTPService(StringRedisTemplate redisTemplate, ApplicationEventPublisher eventPublisher) {
        this.redisTemplate = redisTemplate;
        this.eventPublisher = eventPublisher;
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
            eventPublisher.publishEvent(new OtpValidationFailedEvent(email, CriticalFailureType.OTP_EXPIRED, "OTP expirado ou inexistente", LocalDateTime.now()));
            throw new OtpExpiredException();
        }

        if (!savedCode.equals(codeInput)) {
            eventPublisher.publishEvent(new OtpValidationFailedEvent(email, CriticalFailureType.OTP_INVALID, "Código OTP inválido", LocalDateTime.now()));
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
            eventPublisher.publishEvent(new OtpValidationFailedEvent(email, CriticalFailureType.OTP_TOKEN_INVALID, "Token OTP expirado ou inexistente", LocalDateTime.now()));
            throw new InvalidOtpException();
        }
        if (!savedToken.equals(token)) {
            eventPublisher.publishEvent(new OtpValidationFailedEvent(email, CriticalFailureType.OTP_TOKEN_INVALID, "Token OTP inválido", LocalDateTime.now()));
            throw new InvalidOtpException();
        }
        redisTemplate.delete(key);
        return true;
    }

}
