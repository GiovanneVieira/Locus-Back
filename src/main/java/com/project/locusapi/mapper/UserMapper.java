package com.project.locusapi.mapper;

import com.project.locusapi.dto.user.UserRequestDTO;
import com.project.locusapi.dto.user.UserResponseDTO;
import com.project.locusapi.model.UserModel;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserModel toUserModel(UserRequestDTO userRequestDTO) {
        return UserModel.builder()
                .name(userRequestDTO.name())
                .email(userRequestDTO.email())
                .password(userRequestDTO.password()).build();
    }

    public UserResponseDTO toUserResponseDTO(UserModel userModel) {
        return new UserResponseDTO(
                userModel.getId(),
                userModel.getName(),
                userModel.getEmail(),
                userModel.getRole(),
                userModel.getCreatedAt(),
                userModel.getUpdatedAt(),
                userModel.getPfpUrl());
    }

    public UserRequestDTO toUserRequestDTO(UserModel userModel) {
        return new UserRequestDTO(userModel.getName(), userModel.getEmail(), userModel.getPassword());
    }

    public UserModel responseToModel(UserResponseDTO userResponseDTO) {
        return UserModel
                .builder()
                .email(userResponseDTO.email())
                .name(userResponseDTO.name())
                .role(userResponseDTO.role())
                .build();
    }
}
