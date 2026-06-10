package com.project.locusapi.controller;

import com.project.locusapi.dto.rental.RentalRequestDTO;
import com.project.locusapi.dto.rental.RentalResponseDTO;
import com.project.locusapi.dto.rental.RentalStatusUpdateDTO;
import com.project.locusapi.model.UserModel;
import com.project.locusapi.service.RentalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/rentals")
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;

    // Solicita uma reserva para um imóvel
    @PostMapping("/address/{addressId}")
    public ResponseEntity<RentalResponseDTO> create(
            @PathVariable UUID addressId,
            @RequestBody @Valid RentalRequestDTO dto,
            @AuthenticationPrincipal UserModel authenticatedUser) {

        RentalResponseDTO response = rentalService.create(addressId, authenticatedUser.getId(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Reservas solicitadas pelo usuário (hóspede)
    @GetMapping("/me")
    public ResponseEntity<List<RentalResponseDTO>> getMyBookings(
            @AuthenticationPrincipal UserModel authenticatedUser) {
        return ResponseEntity.ok(rentalService.getMyBookings(authenticatedUser.getId()));
    }

    // Reservas recebidas pelo usuário (anfitrião)
    @GetMapping("/host")
    public ResponseEntity<List<RentalResponseDTO>> getHostBookings(
            @AuthenticationPrincipal UserModel authenticatedUser) {
        return ResponseEntity.ok(rentalService.getHostBookings(authenticatedUser.getId()));
    }

    // Aceitar/recusar (anfitrião) ou cancelar (hóspede)
    @PatchMapping("/{rentalId}/status")
    public ResponseEntity<RentalResponseDTO> updateStatus(
            @PathVariable UUID rentalId,
            @RequestBody @Valid RentalStatusUpdateDTO dto,
            @AuthenticationPrincipal UserModel authenticatedUser) {

        RentalResponseDTO response = rentalService.updateStatus(rentalId, authenticatedUser.getId(), dto.status());
        return ResponseEntity.ok(response);
    }
}
