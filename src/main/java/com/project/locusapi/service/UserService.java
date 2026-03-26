package com.project.locusapi.service;

import com.project.locusapi.constant.Role;
import com.project.locusapi.dto.UserRequestDTO;
import com.project.locusapi.dto.UserResponseDTO;
import com.project.locusapi.mapper.UserMapper;
import com.project.locusapi.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.userMapper = new UserMapper();
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public UserResponseDTO createUser(@Valid UserRequestDTO requestDTO) {
        var user = this.userMapper.toUserModel(requestDTO);
        user.setPassword(passwordEncoder.encode(requestDTO.password()));
        user.setRole(Role.USER);
        var response = this.userRepository.save(user);
        return this.userMapper.toUserResponseDTO(response);
    }

    public UserResponseDTO getUserById(UUID id) {
        var user = this.userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Usuario com id " + id + " nao encontrado"));
        return this.userMapper.toUserResponseDTO(user);
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

    public UserResponseDTO deleteUser(UUID id) {
        var user = this.userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Usuario com id " + id + " nao encontrado"));
        this.userRepository.delete(user);
        return this.userMapper.toUserResponseDTO(user);
    }
}


