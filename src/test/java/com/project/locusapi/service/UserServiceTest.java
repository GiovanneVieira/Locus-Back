package com.project.locusapi.service;


import com.project.locusapi.constant.Role;
import com.project.locusapi.dto.user.UserRequestDTO;
import com.project.locusapi.dto.user.UserResponseDTO;
import com.project.locusapi.mapper.UserMapper;
import com.project.locusapi.model.UserModel;
import com.project.locusapi.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.UUID;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @InjectMocks
    private UserService userService;

    @Test
    public void createUserShouldReturnUserResponseWhenDataIsValid() {
        // 1. Arrange
        var userRequest = new UserRequestDTO("TestUser", "test@email.com", "password123");
        var userModel = new UserModel();
        userModel.setId(UUID.randomUUID());
        userModel.setName("TestUser");

        var expectedResponse = new UserResponseDTO(userModel.getId(), "TestUser", "test@email.com", Role.USER, null, null, null);

        // Stubbing: Accept ANY UserModel and return our prepared userModel
        Mockito.when(userMapper.toUserModel(userRequest)).thenReturn(userModel);
        Mockito.when(userRepository.save(Mockito.any(UserModel.class))).thenReturn(userModel);
        Mockito.when(userMapper.toUserResponseDTO(userModel)).thenReturn(expectedResponse);

        // 2. Act
        UserResponseDTO response = userService.createUser(userRequest);

        // 3. Assert
        Assertions.assertEquals(userModel.getId(), response.id());

        // Verification: Use Mockito.any() to avoid the "identity" mismatch
        Mockito.verify(userRepository).save(Mockito.any(UserModel.class));
    }
}
