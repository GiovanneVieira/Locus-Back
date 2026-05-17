package com.project.locusapi.service;

import com.project.locusapi.constant.AuthProvider;
import com.project.locusapi.constant.Role;
import com.project.locusapi.dto.user.ActivateUserDTO;
import com.project.locusapi.dto.user.UserRequestDTO;
import com.project.locusapi.dto.user.UserResponseDTO;
import com.project.locusapi.mapper.UserMapper;
import com.project.locusapi.model.UserModel;
import com.project.locusapi.repository.UserRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    public UserService(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder, OTPService otpService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.otpService = otpService;
    }

    public UserResponseDTO createUser(@Valid UserRequestDTO requestDTO) {

        var existingUser = this.getUserByEmail(requestDTO.email());
        if(existingUser.isPresent()) {
            throw new EntityExistsException("User with email " + requestDTO.email() + " already exists");
        }

        var newUser = this.userMapper.toUserModel(requestDTO);
        newUser.setPassword(passwordEncoder.encode(requestDTO.password()));
        newUser.setRole(Role.USER);
        newUser.setAuthProvider(AuthProvider.DEFAULT);
        newUser.setPfpUrl(null);
        var savedUser = this.userRepository.save(newUser);
        return this.userMapper.toUserResponseDTO(savedUser);
    }

    public UserResponseDTO getUserById(UUID id) {
        var user = this.userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Usuario com id " + id + " nao encontrado"));
        return this.userMapper.toUserResponseDTO(user);
    }

    public Optional<UserModel> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public UserModel processOAuthUser(String email, String name, String pfpUrl, String provider) {

        if(provider.equals("facebook")){
            return getUserByEmail(email)
                    .orElseGet(() -> {
                        UserModel newUser = new UserModel();
                        newUser.setEmail(email);
                        newUser.setName(name);
                        newUser.setPfpUrl(pfpUrl);
                        newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                        newUser.setRole(Role.USER);
                        newUser.setAuthProvider(AuthProvider.FACEBOOK);
                        return userRepository.save(newUser);
                    });
        }

        return getUserByEmail(email)
                .orElseGet(() -> {
                    UserModel newUser = new UserModel();
                    newUser.setEmail(email);
                    newUser.setName(name);
                    newUser.setPfpUrl(pfpUrl);
                    newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                    newUser.setRole(Role.USER);
                    newUser.setAuthProvider(AuthProvider.GOOGLE);
                    return userRepository.save(newUser);
                });
    }

    public List<UserResponseDTO> getAllUsers() {
        var users = this.userRepository.findAll();
        if (users.isEmpty()) {
            throw new EntityNotFoundException("Nenhum usuario encontrado");
        }
        return users.stream().map(this.userMapper::toUserResponseDTO).toList();
    }

    public UserResponseDTO updateUserById(UUID id, UserRequestDTO requestDTO) {
        var user = this.userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Usuario com id " + id + " nao encontrado"));
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

    public UserResponseDTO enableUser(ActivateUserDTO activateDto) {
        UserModel user = this.getUserByEmail(activateDto.email()).orElseThrow(() -> new EntityNotFoundException("User with email "+ activateDto.email() + " not found"));
        user.setEnabled(true);
        this.userRepository.save(user);
        return userMapper.toUserResponseDTO(user);
    }


    public UserResponseDTO deleteUser(UUID id) {
        var user = this.userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Usuario com id " + id + " nao encontrado"));
        this.userRepository.delete(user);
        return this.userMapper.toUserResponseDTO(user);
    }
  
  
}