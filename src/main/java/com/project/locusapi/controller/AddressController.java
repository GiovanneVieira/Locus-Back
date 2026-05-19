package com.project.locusapi.controller;

import com.project.locusapi.constant.AddressType;
import com.project.locusapi.dto.address.AddressRequestDTO;
import com.project.locusapi.dto.address.AddressResponseDTO;
import com.project.locusapi.dto.address.rentable.RentableAddressRequestDTO;
import com.project.locusapi.model.UserModel;
import com.project.locusapi.service.address.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/address")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    // =========================================================================
    // CRIAÇÃO (POST)
    // =========================================================================

    @PostMapping("/personal")
    public ResponseEntity<AddressResponseDTO> postPersonalAddress(
            @RequestBody @Valid AddressRequestDTO dto,
            @AuthenticationPrincipal UserModel authenticatedUser) {

        // Roteamento limpo via Strategy Pattern
        AddressResponseDTO response = addressService.createAddress(AddressType.PERSONAL, dto, authenticatedUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/rentable")
    public ResponseEntity<AddressResponseDTO> postRentableAddress(
            @RequestBody @Valid RentableAddressRequestDTO dto, // Recebe o DTO especializado com imageIds
            @AuthenticationPrincipal UserModel authenticatedUser) {

        AddressResponseDTO response = addressService.createAddress(AddressType.RENTABLE, dto, authenticatedUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // =========================================================================
    // CONSULTAS / LEITURA (GET)
    // =========================================================================

    @GetMapping("/rentable")
    public ResponseEntity<Page<AddressResponseDTO>> getRentableAddresses(
            @RequestParam(name = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(name = "size", required = false, defaultValue = "12") Integer pageSize) {

        return ResponseEntity.ok(addressService.getAllRentableAddresses(page, pageSize));
    }

    @GetMapping("/personal/{id}")
    public ResponseEntity<AddressResponseDTO> getPersonalAddressByUserId(@PathVariable UUID id) {
        return ResponseEntity.ok(addressService.getPersonalAddressByUserId(id));
    }

    @GetMapping("/rentable/me")
    public ResponseEntity<List<AddressResponseDTO>> getRentableAddressesByUserId(Authentication authentication) {
        return ResponseEntity.ok(addressService.getOwnedRentableAddresses(authentication));
    }

    @GetMapping("/rentable/{id}")
    public ResponseEntity<AddressResponseDTO> getRentableAddressById(@PathVariable UUID id) {
        return ResponseEntity.ok(addressService.getRentableAddressById(id));
    }

    // =========================================================================
    // ATUALIZAÇÃO (PATCH / PUT)
    // =========================================================================

    @PatchMapping("/personal/{id}") // Normalizado para @PathVariable mantendo o padrão RESTful
    public ResponseEntity<AddressResponseDTO> updatePersonalAddress(
            @PathVariable UUID id,
            @RequestBody @Valid AddressRequestDTO dto,
            @AuthenticationPrincipal UserModel authenticatedUser) {

        AddressResponseDTO updated = addressService.updateAddress(AddressType.PERSONAL, id, authenticatedUser.getId(), dto);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/rentable/{id}")
    public ResponseEntity<AddressResponseDTO> updateRentableAddress(
            @PathVariable UUID id,
            @RequestBody @Valid RentableAddressRequestDTO dto, // Suporta os campos de imagem na edição
            @AuthenticationPrincipal UserModel authenticatedUser) {

        AddressResponseDTO updated = addressService.updateAddress(AddressType.RENTABLE, id, authenticatedUser.getId(), dto);
        return ResponseEntity.ok(updated);
    }

    // =========================================================================
    // EXCLUSÃO (DELETE)
    // =========================================================================

    @DeleteMapping("/personal/{id}")
    public ResponseEntity<Void> deletePersonalAddress(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserModel authenticatedUser) {

        addressService.deleteAddress(AddressType.PERSONAL, id, authenticatedUser.getId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/rentable/{id}") // Normalizado de @RequestParam para @PathVariable
    public ResponseEntity<Void> deleteRentableAddress(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserModel authenticatedUser) {

        addressService.deleteAddress(AddressType.RENTABLE, id, authenticatedUser.getId());
        return ResponseEntity.noContent().build();
    }
}