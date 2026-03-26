package com.project.locusapi.controller;

import com.project.locusapi.dto.user.UserRequestDTO;
import com.project.locusapi.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<?> postUser(@RequestBody @Valid UserRequestDTO userRequestDTO) {
        var user = userService.createUser(userRequestDTO);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao criar usuario");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @GetMapping
    public ResponseEntity<?> getUsers() {
        var users = userService.getAllUsers();
        return ResponseEntity.status(HttpStatus.OK).body(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable UUID id) {
        var user = userService.getUserById(id);
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable UUID id, @RequestBody UserRequestDTO userRequestDTO) {
        var response = userService.updateUserById(id, userRequestDTO);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable UUID id) {
        var user = userService.deleteUser(id);
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }

}
