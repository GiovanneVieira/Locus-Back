package com.project.locusapi.service;

import com.project.locusapi.constant.AuthProvider;
import com.project.locusapi.constant.Role;
import com.project.locusapi.dto.user.UserRequestDTO;
import com.project.locusapi.dto.user.UserResponseDTO;
import com.project.locusapi.exception.business.EmailAlreadyExistsException;
import com.project.locusapi.exception.business.UserNotFoundException;
import com.project.locusapi.mapper.UserMapper;
import com.project.locusapi.model.UserModel;
import com.project.locusapi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do Serviço de Usuários (UserService)")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UserModel userModel;
    private UserRequestDTO requestDTO;
    private UserResponseDTO responseDTO;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        // Instancia objetos comuns para reaproveitamento nos testes
        userModel = new UserModel();
        userModel.setId(userId);
        userModel.setEmail("viajante@locus.com");
        userModel.setName("John Doe");
        userModel.setPassword("senhaCriptografada");
        userModel.setRole(Role.USER);
        userModel.setAuthProvider(AuthProvider.DEFAULT);

        requestDTO = new UserRequestDTO("John Doe", "viajante@locus.com", "senha123");
        responseDTO = new UserResponseDTO(userId, "John Doe", "viajante@locus.com", Role.USER, userModel.getCreatedAt(), null, userModel.getPfpUrl());
    }

    // --- GRUPO: CRIEÇÃO DE USUÁRIO ---
    @Nested
    @DisplayName("Método: createUser")
    class CreateUserTests {

        @Test
        @DisplayName("Deve criar um usuário com sucesso quando o e-mail não existir")
        void deveCriarUsuarioComSucesso() {
            // Arrange
            when(userRepository.findByEmail(requestDTO.email())).thenReturn(Optional.empty());
            when(userMapper.toUserModel(requestDTO)).thenReturn(userModel);
            when(passwordEncoder.encode(requestDTO.password())).thenReturn("senhaCriptografada");
            when(userRepository.save(userModel)).thenReturn(userModel);
            when(userMapper.toUserResponseDTO(userModel)).thenReturn(responseDTO);

            // Act
            UserResponseDTO resultado = userService.createUser(requestDTO);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.email()).isEqualTo(requestDTO.email());

            verify(userRepository).save(userModel);
            verify(passwordEncoder).encode(requestDTO.password());
        }

        @Test
        @DisplayName("Deve lançar EmailAlreadyExistsException quando o e-mail já estiver cadastrado")
        void deveLancarexcecaoQuandoEmailJaExistir() {
            // Arrange
            when(userRepository.findByEmail(requestDTO.email())).thenReturn(Optional.of(userModel));

            // Act & Assert
            assertThatThrownBy(() -> userService.createUser(requestDTO))
                    .isInstanceOf(EmailAlreadyExistsException.class)
                    .hasMessageContaining("O e-mail " + requestDTO.email() + " já está cadastrado no sistema.");

            verify(userRepository, never()).save(any());
        }
    }

    // --- GRUPO: BUSCA POR ID ---
    @Nested
    @DisplayName("Método: getUserById")
    class GetUserByIdTests {

        @Test
        @DisplayName("Deve retornar UserResponseDTO quando o ID for encontrado")
        void deveRetornarUsuarioPorId() {
            // Arrange
            when(userRepository.findById(userId)).thenReturn(Optional.of(userModel));
            when(userMapper.toUserResponseDTO(userModel)).thenReturn(responseDTO);

            // Act
            UserResponseDTO resultado = userService.getUserById(userId);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.id()).isEqualTo(userId);
        }

        @Test
        @DisplayName("Deve lançar UserNotFoundException quando o ID não for encontrado")
        void deveLancarExcecaoQuandoIdNaoEncontrado() {
            // Arrange
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> userService.getUserById(userId))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining("Usuário com ID " + userId + " não foi encontrado.");
        }
    }

    // --- GRUPO: LOGIN SOCIAL (OAuth2) ---
    @Nested
    @DisplayName("Método: processOAuthUser")
    class ProcessOAuthUserTests {

        @Test
        @DisplayName("Deve retornar usuário existente se o e-mail do Facebook já estiver no banco")
        void deveRetornarUsuarioExistenteFacebook() {
            // Arrange
            when(userRepository.findByEmail(userModel.getEmail())).thenReturn(Optional.of(userModel));

            // Act
            UserModel resultado = userService.processOAuthUser(userModel.getEmail(), "John", "url", "facebook");

            // Assert
            assertThat(resultado).isEqualTo(userModel);
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve criar um novo usuário com AuthProvider FACEBOOK se não existir")
        void deveCriarNovoUsuarioFacebook() {
            // Arrange
            String email = "fb@locus.com";
            when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
            when(passwordEncoder.encode(anyString())).thenReturn("senhaCriptoRandom");
            when(userRepository.save(any(UserModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            UserModel resultado = userService.processOAuthUser(email, "FB User", "pfp_url", "facebook");

            // Assert
            assertThat(resultado.getEmail()).isEqualTo(email);
            assertThat(resultado.getAuthProvider()).isEqualTo(AuthProvider.FACEBOOK);
            verify(userRepository).save(any(UserModel.class));
        }

        @Test
        @DisplayName("Deve criar um novo usuário com AuthProvider GOOGLE se o provedor for diferente de facebook")
        void deveCriarNovoUsuarioGoogle() {
            // Arrange
            String email = "google@locus.com";
            when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
            when(passwordEncoder.encode(anyString())).thenReturn("senhaCriptoRandom");
            when(userRepository.save(any(UserModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            UserModel resultado = userService.processOAuthUser(email, "Google User", "pfp_url", "google");

            // Assert
            assertThat(resultado.getEmail()).isEqualTo(email);
            assertThat(resultado.getAuthProvider()).isEqualTo(AuthProvider.GOOGLE);
            verify(userRepository).save(any(UserModel.class));
        }
    }

    // --- GRUPO: LISTAGEM DE USUÁRIOS ---
    @Nested
    @DisplayName("Método: getAllUsers")
    class GetAllUsersTests {

        @Test
        @DisplayName("Deve retornar uma lista de usuários cadastrados")
        void deveRetornarListaDeUsuarios() {
            // Arrange
            when(userRepository.findAll()).thenReturn(List.of(userModel));
            when(userMapper.toUserResponseDTO(userModel)).thenReturn(responseDTO);

            // Act
            List<UserResponseDTO> resultado = userService.getAllUsers();

            // Assert
            assertThat(resultado).isNotEmpty().hasSize(1);
        }

        @Test
        @DisplayName("Deve lançar UserNotFoundException quando a lista estiver vazia")
        void deveLancarExcecaoQuandoListaVazia() {
            // Arrange
            when(userRepository.findAll()).thenReturn(Collections.emptyList());

            // Act & Assert
            assertThatThrownBy(() -> userService.getAllUsers())
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining("Nenhum usuário encontrado cadastrado no sistema.");
        }
    }

    // --- GRUPO: ATUALIZAÇÃO ---
    @Nested
    @DisplayName("Método: updateUserById")
    class UpdateUserByIdTests {

        @Test
        @DisplayName("Deve atualizar todos os campos fornecidos do usuário")
        void deveAtualizarUsuarioComSucesso() {
            // Arrange
            when(userRepository.findById(userId)).thenReturn(Optional.of(userModel));
            when(passwordEncoder.encode(requestDTO.password())).thenReturn("novaSenhaCripto");
            when(userRepository.save(any(UserModel.class))).thenAnswer(inv -> inv.getArgument(0));
            when(userMapper.toUserResponseDTO(any(UserModel.class))).thenReturn(responseDTO);

            // Act
            UserResponseDTO resultado = userService.updateUserById(userId, requestDTO);

            // Assert
            assertThat(resultado).isNotNull();
            verify(userRepository).save(userModel);
            assertThat(userModel.getUpdatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
        }
    }

    // --- GRUPO: USUÁRIO AUTENTICADO ---
    @Nested
    @DisplayName("Método: getAuthenticatedUser")
    class GetAuthenticatedUserTests {

        @Mock
        private Authentication authentication;

        @Test
        @DisplayName("Deve retornar o UserModel diretamente se ele for o Principal da Autenticação")
        void deveRetornarUserQuandoPrincipalForUserModel() {
            // Arrange
            when(authentication.getPrincipal()).thenReturn(userModel);

            // Act
            UserModel resultado = userService.getAuthenticatedUser(authentication);

            // Assert
            assertThat(resultado).isEqualTo(userModel);
            verifyNoInteractions(userRepository);
        }

        @Test
        @DisplayName("Deve buscar pelo e-mail caso o Principal não seja uma instância de UserModel")
        void deveBuscarPorEmailQuandoPrincipalNaoForUserModel() {
            // Arrange
            when(authentication.getPrincipal()).thenReturn("string-principal-qualquer");
            when(authentication.getName()).thenReturn("viajante@locus.com");
            when(userRepository.findByEmail("viajante@locus.com")).thenReturn(Optional.of(userModel));

            // Act
            UserModel resultado = userService.getAuthenticatedUser(authentication);

            // Assert
            assertThat(resultado).isEqualTo(userModel);
            verify(userRepository).findByEmail("viajante@locus.com");
        }
    }

    // --- GRUPO: TORNAR HOST ---
    @Nested
    @DisplayName("Método: setUserToHost")
    class SetUserToHostTests {

        @Mock
        private Authentication authentication;

        @Test
        @DisplayName("Deve alterar a Role do usuário para HOST com sucesso")
        void deveMudarRoleParaHost() {
            // Arrange
            when(authentication.getName()).thenReturn("viajante@locus.com");
            when(userRepository.findByEmail("viajante@locus.com")).thenReturn(Optional.of(userModel));
            when(userRepository.save(userModel)).thenReturn(userModel);
            when(userMapper.toUserResponseDTO(any(UserModel.class))).thenAnswer(inv -> {
                UserModel model = inv.getArgument(0);
                return new UserResponseDTO(model.getId(), model.getName(), model.getEmail(), model.getRole(), model.getCreatedAt(), null, model.getPfpUrl());
            });

            // Act
            UserResponseDTO resultado = userService.setUserToHost(authentication);

            // Assert
            assertThat(userModel.getRole()).isEqualTo(Role.HOST);
            verify(userRepository).save(userModel);
        }

        @Test
        @DisplayName("Deve lançar UserNotFoundException se o e-mail na autenticação for nulo")
        void deveLancarExcecaoSeEmailNulo() {
            // Arrange
            when(authentication.getName()).thenReturn(null);

            // Act & Assert
            assertThatThrownBy(() -> userService.setUserToHost(authentication))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    // --- GRUPO: DELETAR USUÁRIO ---
    @Nested
    @DisplayName("Método: deleteUser")
    class DeleteUserTests {

        @Test
        @DisplayName("Deve deletar o usuário se o ID existir")
        void deveDeletarUsuario() {
            // Arrange
            when(userRepository.findById(userId)).thenReturn(Optional.of(userModel));
            when(userMapper.toUserResponseDTO(userModel)).thenReturn(responseDTO);

            // Act
            UserResponseDTO resultado = userService.deleteUser(userId);

            // Assert
            assertThat(resultado).isNotNull();
            verify(userRepository).delete(userModel);
        }
    }
}