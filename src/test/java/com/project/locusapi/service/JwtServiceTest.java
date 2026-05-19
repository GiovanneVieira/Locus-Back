package com.project.locusapi.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.project.locusapi.config.JwtPropertiesConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do Serviço de JWT (JwtService)")
class JwtServiceTest {

    private final String secretTest = "uma-chave-secreta-muito-segura-e-longa-para-o-projeto-locus";
    private final String issuerTest = "Locus-Test-Issuer";
    private final int accessExpirationTest = 900; // 15 min
    private final int refreshExpirationTest = 86400; // 24h
    @Mock
    private JwtPropertiesConfig jwtPropertiesConfig;
    @InjectMocks
    private JwtService jwtService;
    @Mock
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        // CORREÇÃO 1: Usamos lenient() para stubs globais que podem não ser chamados por todos os subgrupos @Nested
        lenient().when(jwtPropertiesConfig.getSecret()).thenReturn(secretTest);
        lenient().when(jwtPropertiesConfig.getIssuer()).thenReturn(issuerTest);
        lenient().when(userDetails.getUsername()).thenReturn("viajante@locus.com");
    }

    // --- GRUPO: GERAÇÃO DE ACCESS TOKEN ---
    @Nested
    @DisplayName("Método: generateAccessToken")
    class GenerateAccessTokenTests {

        @Test
        @DisplayName("Deve gerar um Access Token válido contendo claims de roles e tipo correto")
        void deveGerarAccessTokenComSucesso() {
            // Arrange
            when(jwtPropertiesConfig.getJwtAccessExpiration()).thenReturn(accessExpirationTest);

            GrantedAuthority authority = () -> "ROLE_USER";
            doReturn(List.of(authority)).when(userDetails).getAuthorities();

            // Act
            String token = jwtService.generateAccessToken(userDetails);

            // Assert
            assertThat(token).isNotNull().isNotBlank();

            DecodedJWT decoded = JWT.decode(token);
            assertThat(decoded.getSubject()).isEqualTo("viajante@locus.com");
            assertThat(decoded.getIssuer()).isEqualTo(issuerTest);
            assertThat(decoded.getClaim("tokenType").asString()).isEqualTo("access");
            assertThat(decoded.getClaim("roles").asList(String.class)).containsExactly("ROLE_USER");
            assertThat(decoded.getExpiresAt()).isNotNull();
        }
    }

    // --- GRUPO: GERAÇÃO DE REFRESH TOKEN ---
    @Nested
    @DisplayName("Método: generateRefreshToken")
    class GenerateRefreshTokenTests {

        @Test
        @DisplayName("Deve gerar um Refresh Token válido contendo apenas o subject e tipo correto")
        void deveGerarRefreshTokenComSucesso() {
            // Arrange
            when(jwtPropertiesConfig.getJwtRefreshExpiration()).thenReturn(refreshExpirationTest);

            // Act
            String token = jwtService.generateRefreshToken(userDetails);

            // Assert
            assertThat(token).isNotNull().isNotBlank();

            DecodedJWT decoded = JWT.decode(token);
            assertThat(decoded.getSubject()).isEqualTo("viajante@locus.com");
            assertThat(decoded.getIssuer()).isEqualTo(issuerTest);
            assertThat(decoded.getClaim("tokenType").asString()).isEqualTo("refresh");

            // CORREÇÃO 2: .isMissing() avalia corretamente a ausência da claim no Auth0 JWT
            assertThat(decoded.getClaim("roles").isMissing()).isTrue();
        }
    }

    // --- GRUPO: VALIDAÇÃO E EXTRAÇÃO ---
    @Nested
    @DisplayName("Métodos de Validação e Extração de Claims")
    class ValidationAndExtractionTests {

        private String validAccessToken;

        @BeforeEach
        void setUpValidToken() {
            when(jwtPropertiesConfig.getJwtAccessExpiration()).thenReturn(accessExpirationTest);
            GrantedAuthority authority = () -> "ROLE_ADMIN";
            doReturn(List.of(authority)).when(userDetails).getAuthorities();

            validAccessToken = jwtService.generateAccessToken(userDetails);
        }

        @Test
        @DisplayName("Deve validar o token e retornar o subject com sucesso")
        void deveValidarTokenERetornarSubject() {
            // Act
            String subject = jwtService.validateToken(validAccessToken);

            // Assert
            assertThat(subject).isEqualTo("viajante@locus.com");
        }

        @Test
        @DisplayName("Deve lançar JWTVerificationException ao validar um token violado/inválido")
        void deveLancarExcecaoParaTokenInvalido() {
            // Arrange
            String tokenViolado = validAccessToken + "burlar-assinatura";

            // Act & Assert
            assertThatThrownBy(() -> jwtService.validateToken(tokenViolado))
                    .isInstanceOf(JWTVerificationException.class);
        }

        @Test
        @DisplayName("Deve extrair o tipo do token corretamente")
        void deveExtrairTokenType() {
            // Act
            String tokenType = jwtService.getTokenType(validAccessToken);

            // Assert
            assertThat(tokenType).isEqualTo("access");
        }

        @Test
        @DisplayName("Deve extrair a lista de roles corretamente")
        void deveExtrairRoles() {
            // Act
            List<String> roles = jwtService.getRoles(validAccessToken);

            // Assert
            assertThat(roles).containsExactly("ROLE_ADMIN");
        }
    }

    // --- GRUPO: COOKIES ---
    @Nested
    @DisplayName("Métodos de Manipulação de Cookies HTTP")
    class CookieTests {

        @Test
        @DisplayName("Deve gerar um cookie de limpeza expirado (maxAge=0)")
        void deveGerarCookieDeLimpeza() {
            // Act
            ResponseCookie cleanCookie = jwtService.getCleanCookie("dummyToken");

            // Assert
            assertThat(cleanCookie).isNotNull();
            assertThat(cleanCookie.getName()).isEqualTo("dummyToken");
            assertThat(cleanCookie.getValue()).isEmpty();
            assertThat(cleanCookie.getMaxAge().getSeconds()).isZero();
            assertThat(cleanCookie.isHttpOnly()).isTrue();
            assertThat(cleanCookie.getPath()).isEqualTo("/");
        }

        @Test
        @DisplayName("Deve gerar uma lista contendo cookies de Access e Refresh preenchidos")
        void deveGerarListaDeCookiesDeAutenticacao() {
            // Arrange
            when(jwtPropertiesConfig.getJwtAccessExpiration()).thenReturn(accessExpirationTest);
            when(jwtPropertiesConfig.getJwtRefreshExpiration()).thenReturn(refreshExpirationTest);

            // Act
            List<ResponseCookie> cookies = jwtService.generateCookies("access-value", "refresh-value");

            // Assert
            assertThat(cookies).hasSize(2);

            ResponseCookie accessCookie = cookies.getFirst();
            assertThat(accessCookie.getName()).isEqualTo("accessToken");
            assertThat(accessCookie.getValue()).isEqualTo("access-value");
            assertThat(accessCookie.getMaxAge().getSeconds()).isEqualTo(accessExpirationTest);
            assertThat(accessCookie.isHttpOnly()).isTrue();
            assertThat(accessCookie.getSameSite()).isEqualTo("Lax");

            ResponseCookie refreshCookie = cookies.get(1);
            assertThat(refreshCookie.getName()).isEqualTo("refreshToken");
            assertThat(refreshCookie.getValue()).isEqualTo("refresh-value");
            assertThat(refreshCookie.getMaxAge().getSeconds()).isEqualTo(refreshExpirationTest);
            assertThat(refreshCookie.isHttpOnly()).isTrue();
            assertThat(refreshCookie.getSameSite()).isEqualTo("Lax");
        }
    }
}