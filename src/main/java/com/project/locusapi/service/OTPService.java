package com.project.locusapi.service;

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

    // Gera um número aleatório de 6 dígitos, salva no Redis e retorna
    public String generateAndSaveOtp(String email) {

        String code = String.format("%06d", new Random().nextInt(999999));
        String key = REDIS_PREFIX + email;

        // Salva a chave (ex: "otp:email@email.com") atrelada ao código por 5 minutos
        redisTemplate.opsForValue().set(key, code, EXPIRE_MINUTES, TimeUnit.MINUTES);

        return code;
    }

    // Valida o código enviado pelo usuário
    public boolean validateOtp(String email, String codeInput) {
        String key = REDIS_PREFIX + email;
        String savedCode = redisTemplate.opsForValue().get(key);

        if (savedCode == null) {
            throw new IllegalArgumentException("O código expirou ou nunca foi solicitado.");
        }

        if (!savedCode.equals(codeInput)) {
            throw new IllegalArgumentException("Código de verificação inválido.");
        }

        // Se o código bateu certinho, apaga imediatamente para evitar reuso (Replay Attack)
        redisTemplate.delete(key);
        return true;
    }
}
