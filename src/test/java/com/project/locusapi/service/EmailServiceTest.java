package com.project.locusapi.service;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do Serviço de E-mail (EmailService)")
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @InjectMocks
    private EmailService emailService;

    private MimeMessage realMimeMessage;

    @BeforeEach
    void setUp() {
        // Criamos uma instância real com Session nula para evitar os NPEs do MimeMessageHelper
        realMimeMessage = new MimeMessage((Session) null);
    }

    // --- GRUPO: ENVIO TEXTO PURO ---
    @Nested
    @DisplayName("Método: sendMsg")
    class SendMsgTests {

        @Test
        @DisplayName("Deve enviar um e-mail simples de texto com os campos corretos")
        void deveEnviarEmailSimplesComSucesso() {
            // Act
            emailService.sendMsg("viajante@locus.com", "Bem-vindo", "Olá mundo!");

            // Assert
            // Capturamos o objeto criado internamente para inspecionar as propriedades
            ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
            verify(mailSender).send(captor.capture());

            SimpleMailMessage mensagemEnviada = captor.getValue();
            assertThat(mensagemEnviada.getTo()).containsExactly("viajante@locus.com");
            assertThat(mensagemEnviada.getSubject()).isEqualTo("Bem-vindo");
            assertThat(mensagemEnviada.getText()).isEqualTo("Olá mundo!");
            assertThat(mensagemEnviada.getFrom()).isEqualTo("Locus");
        }
    }

    // --- GRUPO: ENVIO DE EMAIL HTML (WELCOME) ---
    @Nested
    @DisplayName("Método: sendWelcomeEmail")
    class SendWelcomeEmailTests {

        @Test
        @DisplayName("Deve processar o template Thymeleaf e enviar o e-mail HTML de boas-vindas")
        void deveEnviarEmailBoasVindasComSucesso() {
            // Arrange
            String emailDestino = "viajante@locus.com";
            when(mailSender.createMimeMessage()).thenReturn(realMimeMessage);
            when(templateEngine.process(eq("welcome/html"), any(Context.class)))
                    .thenReturn("<html>Conteudo HTML de boas-vindas</html>");

            // Act
            emailService.sendWelcomeEmail(emailDestino, "Boas-vindas ao Locus!");

            // Assert
            verify(templateEngine).process(eq("welcome/html"), any(Context.class));
            verify(mailSender).send(realMimeMessage);
        }

        @Test
        @DisplayName("Deve lançar RuntimeException se o processamento do template ou envio falhar")
        void deveLancarExcecaoQuandoTemplateFalhar() {
            // Arrange
            when(mailSender.createMimeMessage()).thenReturn(realMimeMessage);
            when(templateEngine.process(anyString(), any(Context.class)))
                    .thenThrow(new RuntimeException("Erro no motor do Thymeleaf"));

            // Act & Assert
            assertThatThrownBy(() -> emailService.sendWelcomeEmail("teste@locus.com", "Subject"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Error rendering or sending email template");
        }
    }

    // --- GRUPO: ENVIO DE OTP ---
    @Nested
    @DisplayName("Método: sendOTPEmail")
    class SendOTPEmailTests {

        @Test
        @DisplayName("Deve processar o template de OTP e enviar com sucesso")
        void deveEnviarEmailOtpComSucesso() {
            // Arrange
            when(mailSender.createMimeMessage()).thenReturn(realMimeMessage);
            when(templateEngine.process(eq("emails/otp"), any(Context.class)))
                    .thenReturn("<html>Seu código é 123456</html>");

            // Act
            emailService.sendOTPEmail("viajante@locus.com", "123456", "John Doe");

            // Assert
            verify(templateEngine).process(eq("emails/otp"), any(Context.class));
            verify(mailSender).send(realMimeMessage);
        }
    }
}