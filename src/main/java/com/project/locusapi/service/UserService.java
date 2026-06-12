package com.project.locusapi.service;

import com.project.locusapi.constant.AuthProvider;
import com.project.locusapi.constant.Role;
import com.project.locusapi.dto.forgotpassword.ForgotPasswordDTO;
import com.project.locusapi.dto.user.ActivateUserDTO;
import com.project.locusapi.dto.user.UpdateUserDTO;
import com.project.locusapi.dto.user.UserRequestDTO;
import com.project.locusapi.dto.user.UserResponseDTO;
import com.project.locusapi.event.metrics.UserActivatedEvent;
import com.project.locusapi.event.metrics.UserRegisteredEvent;
import com.project.locusapi.exception.business.EmailAlreadyExistsException;
import com.project.locusapi.exception.business.NewPasswordEqualsPreviousPassword;
import com.project.locusapi.exception.business.UserNotFoundException;
import com.project.locusapi.mapper.UserMapper;
import com.project.locusapi.model.UserModel;
import com.project.locusapi.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final OTPService otpService;
    private final ApplicationEventPublisher eventPublisher;

    public UserService(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder, OTPService otpService, ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.otpService = otpService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public UserResponseDTO createUser(@Valid UserRequestDTO requestDTO) {
        var existingUser = this.getUserByEmail(requestDTO.email());
        if (existingUser.isPresent()) {
            throw new EmailAlreadyExistsException(requestDTO.email());
        }

        var newUser = this.userMapper.toUserModel(requestDTO);
        newUser.setPassword(passwordEncoder.encode(requestDTO.password()));
        newUser.setRole(Role.USER);
        newUser.setAuthProvider(AuthProvider.DEFAULT);
        newUser.setPfpUrl(null);
        var savedUser = this.userRepository.save(newUser);
        eventPublisher.publishEvent(new UserRegisteredEvent(savedUser.getId(), savedUser.getEmail(), savedUser.isEnabled(), savedUser.getAuthProvider(), LocalDateTime.now()));
        return this.userMapper.toUserResponseDTO(savedUser);
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(UUID id) {
        var user = this.userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return this.userMapper.toUserResponseDTO(user);
    }

    @Transactional(readOnly = true)
    public Optional<UserModel> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public UserModel saveUser(UserModel user) {
        return this.userRepository.save(user);
    }

    @Transactional
    public UserModel processOAuthUser(String email, String name, String pfpUrl, String provider) {
        AuthProvider authProvider = provider.equals("facebook") ? AuthProvider.FACEBOOK : AuthProvider.GOOGLE;

        return getUserByEmail(email)
                .orElseGet(() -> {
                    UserModel newUser = new UserModel();
                    newUser.setEmail(email);
                    newUser.setName(name);
                    newUser.setPfpUrl(pfpUrl);
                    newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                    newUser.setRole(Role.USER);
                    newUser.setAuthProvider(authProvider);
                    return userRepository.save(newUser);
                });
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        var users = this.userRepository.findAll();
        if (users.isEmpty()) {
            throw new UserNotFoundException("Nenhum usuário encontrado cadastrado no sistema.");
        }
        return users.stream().map(this.userMapper::toUserResponseDTO).toList();
    }

    @Transactional
    public UserResponseDTO updateUserById(UUID id, UserRequestDTO requestDTO) {
        var user = this.userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (requestDTO.name() != null) {
            user.setName(requestDTO.name());
        }
        if (requestDTO.email() != null) {
            user.setEmail(requestDTO.email());
        }
        if (requestDTO.password() != null) {
            user.setPassword(passwordEncoder.encode(requestDTO.password()));
        }
        user.setUpdatedAt(LocalDateTime.now());
        var response = this.userRepository.save(user);
        return this.userMapper.toUserResponseDTO(response);
    }

    @Transactional(readOnly = true)
    public UserModel getAuthenticatedUser(Authentication authentication) {
        if (authentication.getPrincipal() instanceof UserModel userModel) {
            return userModel;
        }

        var email = authentication.getName();
        return this.userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Usuário autenticado não encontrado."));
    }

    @Transactional
    public UserResponseDTO enableUser(ActivateUserDTO activateDto) {
        UserModel user = this.getUserByEmail(activateDto.email())
                .orElseThrow(() -> new UserNotFoundException(activateDto.email()));
        user.setEnabled(true);
        this.userRepository.save(user);
        eventPublisher.publishEvent(new UserActivatedEvent(user.getId(), user.getEmail(), LocalDateTime.now()));
        return userMapper.toUserResponseDTO(user);
    }

    @Transactional
    public UserResponseDTO setUserToHost(Authentication authentication) {
        var email = authentication.getName();
        if (email == null) {
            throw new UserNotFoundException();
        }
        var user = this.getUserByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));

        user.setRole(Role.HOST);
        user.setUpdatedAt(LocalDateTime.now());
        this.userRepository.save(user);
        return userMapper.toUserResponseDTO(user);
    }

    @Transactional
    public UserResponseDTO updateCurrentUser(UpdateUserDTO userDTO, Authentication authentication) {

        var email = authentication.getName();
        if (email == null) {
            throw new UserNotFoundException("User not found");
        }

        var user = this.getUserByEmail(email).orElseThrow(() -> new UserNotFoundException(email));

        // Proteção usando checagem de nulo + conteúdo real (isBlank)
        if (userDTO.name() != null && !userDTO.name().isBlank()) {
            user.setName(userDTO.name());
        }
        if (userDTO.bio() != null && !userDTO.bio().isBlank()) {
            user.setBio(userDTO.bio());
        }
        if (userDTO.pfpUrl() != null && !userDTO.pfpUrl().isBlank()) {
            user.setPfpUrl(userDTO.pfpUrl());
        }
        if (userDTO.phone() != null && !userDTO.phone().isBlank()) {
            user.setPhone(userDTO.phone()); // CORRIGIDO: Agora seta o telefone no campo certo!
        }

        user.setUpdatedAt(LocalDateTime.now());

        saveUser(user);
        return userMapper.toUserResponseDTO(user);
    }


    @Transactional
    public UserResponseDTO deleteUser(UUID id) {
        var user = this.userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        this.userRepository.delete(user);
        return this.userMapper.toUserResponseDTO(user);
    }

    @Transactional
    public UserResponseDTO forgotPassword(ForgotPasswordDTO dto) {
        var user = this.getUserByEmail(dto.email()).orElseThrow(() -> new UserNotFoundException(dto.email()));

        if(!otpService.validateToken(dto.otpToken(), dto.email())){
            throw new RuntimeException("Token expired");
        }

        if(passwordEncoder.matches(dto.password(), user.getPassword())) {
            throw new NewPasswordEqualsPreviousPassword();
        }

        var newPassword = this.passwordEncoder.encode(dto.password());
        user.setPassword(newPassword);
        user.setUpdatedAt(LocalDateTime.now());
        return this.userMapper.toUserResponseDTO(this.userRepository.save(user));
    }

}
