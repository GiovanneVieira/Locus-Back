package com.project.locusapi.service;

import com.project.locusapi.exception.business.InvalidOtpException;
import com.project.locusapi.exception.business.OtpExpiredException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do Serviço de OTP (OTPService)")
class OTPServiceTest {

    private final String emailTest = "viajante@locus.com";
    private final String redisKeyTest = "otp:viajante@locus.com";
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @InjectMocks
    private OTPService otpService;

    @BeforeEach
    void setUp() {
        // Como o serviço chama .opsForValue() em todos os métodos, configuramos o comportamento globalmente
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    // --- GRUPO: GERAÇÃO DE OTP ---
    @Nested
    @DisplayName("Método: generateAndSaveOtp")
    class GenerateAndSaveOtpTests {

        @Test
        @DisplayName("Deve gerar um código de 6 dígitos e salvar no Redis com expiração de 5 minutos")
        void deveGerarESalvarOtpComSucesso() {
            // Act
            String code = otpService.generateAndSaveOtp(emailTest);

            // Assert
            // 1. Valida se o código retornado possui o formato esperado de 6 dígitos numéricos
            assertThat(code).isNotNull().hasSize(6).matches("\\d{6}");

            // 2. Valida se a operação de escrita no Redis foi chamada com os parâmetros exatos
            verify(valueOperations).set(
                    eq(redisKeyTest),
                    eq(code),
                    eq(5L),
                    eq(TimeUnit.MINUTES)
            );
        }
    }

    // --- GRUPO: VALIDAÇÃO DE OTP ---
    @Nested
    @DisplayName("Método: validateOtp")
    class ValidateOtpTests {

        @Test
        @DisplayName("Deve retornar true e apagar a chave do Redis quando o código for válido e idêntico")
        void deveValidarOtpComSucesso() {
            // Arrange
            String correctCode = "123456";
            when(valueOperations.get(redisKeyTest)).thenReturn(correctCode);

            // Act
            boolean resultado = otpService.validateOtp(emailTest, correctCode);

            // Assert
            assertThat(resultado).isTrue();

            // Garante a proteção contra Replay Attack: o código precisa ser deletado imediatamente após o sucesso
            verify(redisTemplate).delete(redisKeyTest);
        }

        @Test
        @DisplayName("Deve lançar OtpExpiredException quando o código não existir ou estiver expirado no Redis")
        void deveLancarExcecaoQuandoCodigoExpiradoOuInexistente() {
            // Arrange
            when(valueOperations.get(redisKeyTest)).thenReturn(null);

            // Act & Assert
            assertThatThrownBy(() -> otpService.validateOtp(emailTest, "123456"))
                    .isInstanceOf(OtpExpiredException.class)
                    .hasMessageContaining("O código de verificação expirou ou nunca foi solicitado.");

            verify(redisTemplate, never()).delete(anyString());
        }

        @Test
        @DisplayName("Deve lançar InvalidOtpException quando o código enviado for diferente do salvo")
        void deveLancarExcecaoQuandoCodigoForInvalido() {
            // Arrange
            String savedCode = "123456";
            String wrongInputCode = "654321";
            when(valueOperations.get(redisKeyTest)).thenReturn(savedCode);

            // Act & Assert
            assertThatThrownBy(() -> otpService.validateOtp(emailTest, wrongInputCode))
                    .isInstanceOf(InvalidOtpException.class)
                    .hasMessageContaining("Código de verificação inválido.");

            verify(redisTemplate, never()).delete(anyString());
        }
    }
}