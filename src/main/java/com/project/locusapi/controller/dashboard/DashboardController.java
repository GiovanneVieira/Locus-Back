package com.project.locusapi.controller.dashboard;

import com.project.locusapi.dto.dashboard.BoardResponseDTO;
import com.project.locusapi.dto.dashboard.BoardTaskResponseDTO;
import com.project.locusapi.dto.dashboard.CreateTaskRequestDTO;
import com.project.locusapi.dto.dashboard.UpdateTaskRequestDTO;
import com.project.locusapi.service.dashboard.DashboardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/board")
    public ResponseEntity<BoardResponseDTO> getBoard(Authentication authentication) {
        return ResponseEntity.ok(dashboardService.getBoardForUser(authentication.getName()));
    }

    @PostMapping("/tasks")
    public ResponseEntity<BoardTaskResponseDTO> createTask(
            Authentication authentication,
            @RequestBody @Valid CreateTaskRequestDTO requestDTO
    ) {
        return ResponseEntity.ok(dashboardService.createTask(authentication.getName(), requestDTO));
    }

    @PatchMapping("/tasks/{taskId}")
    public ResponseEntity<BoardTaskResponseDTO> updateTask(
            Authentication authentication,
            @PathVariable UUID taskId,
            @RequestBody UpdateTaskRequestDTO requestDTO
    ) {
        return ResponseEntity.ok(dashboardService.updateTask(authentication.getName(), taskId, requestDTO));
    }

    @DeleteMapping("/tasks/{taskId}")
    public ResponseEntity<Map<String, String>> deleteTask(
            Authentication authentication,
            @PathVariable UUID taskId
    ) {
        dashboardService.deleteTask(authentication.getName(), taskId);
        return ResponseEntity.ok(Map.of("message", "Task removida com sucesso"));
    }
}