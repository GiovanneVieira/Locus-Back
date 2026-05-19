package com.project.locusapi.service;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.project.locusapi.dto.auth.AuthRequestDTO;
import com.project.locusapi.dto.auth.AuthResultDTO;
import com.project.locusapi.dto.user.UserRequestDTO;
import com.project.locusapi.dto.user.UserResponseDTO;
import com.project.locusapi.mapper.UserMapper;
import com.project.locusapi.model.RefreshToken;
import com.project.locusapi.model.UserModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do Serviço de Autenticação (AuthService)")
class AuthServiceTest {

    @Mock private UserService userService;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private UserMapper userMapper;
    @Mock private UserDetailsService userDetailsService;
    @Mock private EmailService emailService;
    @Mock private OTPService otpService;

    @InjectMocks
    private AuthService authService;

    private UserModel userModel;
    private RefreshToken mockRefreshTokenEntity;
    private ResponseCookie mockAccessCookie;
    private ResponseCookie mockRefreshCookie;

    @BeforeEach
    void setUp() {
        userModel = new UserModel();
        userModel.setEmail("viajante@locus.com");
        userModel.setName("John Doe");

        // Instancia o mock do RefreshToken real do projeto para evitar NPEs no relacionamento bilateral
        mockRefreshTokenEntity = mock(RefreshToken.class);

        mockAccessCookie = ResponseCookie.from("accessToken", "access-token-string").build();
        mockRefreshCookie = ResponseCookie.from("refreshToken", "refresh-token-string").build();
    }

    // --- GRUPO: REGISTRO DE USUÁRIO ---
    @Nested
    @DisplayName("Método: registerUser")
    class RegisterUserTests {

        @Test
        @DisplayName("Deve delegar a criação do usuário para o UserService com sucesso")
        void deveRegistrarUsuarioComSucesso() {
            // Arrange
            UserRequestDTO requestDTO = new UserRequestDTO("John Doe", "viajante@locus.com", "senha123");
            UserResponseDTO expectedResponse = new UserResponseDTO(null, "John Doe", "viajante@locus.com", null, null, null, null);

            when(userService.createUser(requestDTO)).thenReturn(expectedResponse);

            // Act
            UserResponseDTO resultado = authService.registerUser(requestDTO);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.email()).isEqualTo(requestDTO.email());
            verify(userService).createUser(requestDTO);
        }
    }

    // --- GRUPO: AUTENTICAÇÃO DE USUÁRIO ---
    @Nested
    @DisplayName("Método: authenticateUser")
    class AuthenticateUserTests {

        @Mock private Authentication authentication;

        @Test
        @DisplayName("Deve autenticar credenciais válidas e retornar AuthResultDTO preenchido")
        void deveAutenticarComSucesso() {
            // Arrange
            AuthRequestDTO authDTO = new AuthRequestDTO("viajante@locus.com", "senha123");

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(userModel);

            // Stubs para o fluxo interno de generateAuthResult
            when(jwtService.generateAccessToken(userModel)).thenReturn("access-token-string");
            when(jwtService.generateRefreshToken(userModel)).thenReturn("refresh-token-string");

            // Corrige o NPE: Garante que o service retorne o mock do token em vez de null
            when(refreshTokenService.saveRefreshToken(anyString(), any(UserModel.class)))
                    .thenReturn(mockRefreshTokenEntity);

            when(jwtService.generateCookies("access-token-string", "refresh-token-string"))
                    .thenReturn(List.of(mockAccessCookie, mockRefreshCookie));

            // Act
            AuthResultDTO resultado = authService.authenticateUser(authDTO);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.responseDTO().email()).isEqualTo(userModel.getEmail());
            assertThat(resultado.responseDTO().accessToken()).isEqualTo("access-token-string");
            assertThat(resultado.cookies()).containsExactly(mockAccessCookie, mockRefreshCookie);

            verify(refreshTokenService).saveRefreshToken("refresh-token-string", userModel);
        }
    }

    // --- GRUPO: LOGIN OAUTH2 ---
    @Nested
    @DisplayName("Método: loginOAuth2User")
    class LoginOAuth2UserTests {

        @BeforeEach
        void setupOAuthStubs() {
            when(jwtService.generateAccessToken(userModel)).thenReturn("access-token-string");
            when(jwtService.generateRefreshToken(userModel)).thenReturn("refresh-token-string");

            // Corrige o NPE nos testes de OAuth também
            when(refreshTokenService.saveRefreshToken(anyString(), any(UserModel.class)))
                    .thenReturn(mockRefreshTokenEntity);

            when(jwtService.generateCookies("access-token-string", "refresh-token-string"))
                    .thenReturn(List.of(mockAccessCookie, mockRefreshCookie));
        }

        @Test
        @DisplayName("Deve retornar o resultado de autenticação para um usuário OAuth existente")
        void deveLogarUsuarioOAuthExistente() {
            // Arrange
            when(userService.getUserByEmail(userModel.getEmail())).thenReturn(Optional.of(userModel));

            // Act
            AuthResultDTO resultado = authService.loginOAuth2User(userModel.getEmail(), "John", "url", "google");

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(userModel.isEnabled()).isTrue();
            verify(userService, never()).processOAuthUser(any(), any(), any(), any());
            verify(userService).saveUser(userModel);
        }

        @Test
        @DisplayName("Deve criar um novo usuário se o e-mail OAuth não for encontrado")
        void deveCriarELogarNovoUsuarioOAuth() {
            // Arrange
            when(userService.getUserByEmail(userModel.getEmail())).thenReturn(Optional.empty());
            when(userService.processOAuthUser(userModel.getEmail(), "John", "url", "google")).thenReturn(userModel);

            // Act
            AuthResultDTO resultado = authService.loginOAuth2User(userModel.getEmail(), "John", "url", "google");

            // Assert
            assertThat(resultado).isNotNull();
            verify(userService).processOAuthUser(userModel.getEmail(), "John", "url", "google");
            verify(userService).saveUser(userModel);
        }
    }

    // --- GRUPO: REFRESH TOKEN ---
    @Nested
    @DisplayName("Método: refreshToken")
    class RefreshTokenTests {

        private final String oldTokenStr = "old-refresh-token";

        @Test
        @DisplayName("Deve gerar novos tokens com sucesso quando o refresh token antigo for válido")
        void deveRenovarTokensComSucesso() throws Exception {
            // Arrange
            var tokenEntityMock = mock(RefreshToken.class);
            when(tokenEntityMock.getUser()).thenReturn(userModel);

            when(jwtService.validateToken(oldTokenStr)).thenReturn(userModel.getEmail());
            when(jwtService.getTokenType(oldTokenStr)).thenReturn("refresh");
            when(refreshTokenService.findByToken(oldTokenStr)).thenReturn(tokenEntityMock);
            when(userDetailsService.loadUserByUsername(userModel.getEmail())).thenReturn(userModel);

            when(jwtService.generateAccessToken(userModel)).thenReturn("new-access-token");
            when(jwtService.generateRefreshToken(userModel)).thenReturn("new-refresh-token");
            when(jwtService.generateCookies("new-access-token", "new-refresh-token"))
                    .thenReturn(List.of(mockAccessCookie, mockRefreshCookie));

            // Act
            AuthResultDTO resultado = authService.refreshToken(oldTokenStr);

            // Assert
            assertThat(resultado).isNotNull();
            verify(refreshTokenService).deleteByToken(oldTokenStr);
            verify(refreshTokenService).saveRefreshToken("new-refresh-token", userModel);
        }

        @Test
        @DisplayName("Deve lançar JWTVerificationException se o token for inválido ou não for do tipo 'refresh'")
        void deveLancarExcecaoQuandoTokenInvalido() {
            // Arrange
            when(jwtService.validateToken(oldTokenStr)).thenReturn(null);

            // Act & Assert
            assertThatThrownBy(() -> authService.refreshToken(oldTokenStr))
                    .isInstanceOf(JWTVerificationException.class)
                    .hasMessageContaining("Invalid refresh token");
        }
    }

    // --- GRUPO: LOGOUT ---
    @Nested
    @DisplayName("Método: logoutUser")
    class LogoutUserTests {

        @Test
        @DisplayName("Deve deletar o token do banco e retornar cookies de limpeza se o token for informado")
        void deveLimparDadosNoLogout() {
            // Arrange
            String token = "valid-refresh-token";
            ResponseCookie cleanAccess = ResponseCookie.from("accessToken", "").maxAge(0).build();
            ResponseCookie cleanRefresh = ResponseCookie.from("refreshToken", "").maxAge(0).build();

            when(jwtService.getCleanCookie("accessToken")).thenReturn(cleanAccess);
            when(jwtService.getCleanCookie("refreshToken")).thenReturn(cleanRefresh);

            // Act
            List<ResponseCookie> resultado = authService.logoutUser(token);

            // Assert
            assertThat(resultado).containsExactly(cleanAccess, cleanRefresh);
            verify(refreshTokenService).deleteByToken(token);
        }

        @Test
        @DisplayName("Não deve interagir com o banco se o token passado for nulo ou em branco")
        void deveApenasLimparCookiesSeTokenForNulo() {
            // Arrange
            ResponseCookie cleanAccess = ResponseCookie.from("accessToken", "").maxAge(0).build();
            ResponseCookie cleanRefresh = ResponseCookie.from("refreshToken", "").maxAge(0).build();

            when(jwtService.getCleanCookie("accessToken")).thenReturn(cleanAccess);
            when(jwtService.getCleanCookie("refreshToken")).thenReturn(cleanRefresh);

            // Act
            List<ResponseCookie> resultado = authService.logoutUser("   ");

            // Assert
            assertThat(resultado).containsExactly(cleanAccess, cleanRefresh);
            verifyNoInteractions(refreshTokenService);
        }
    }
}