package com.project.locusapi.controller;

import com.project.locusapi.dto.address.AddressRequestDTO;
import com.project.locusapi.dto.address.AddressResponseDTO;
import com.project.locusapi.model.UserModel;
import com.project.locusapi.service.AddressService;
import com.project.locusapi.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    private final UserService userService;

    @PostMapping("/personal")
    public ResponseEntity<AddressResponseDTO> postPersonalAddress(@RequestBody AddressRequestDTO addressRequestDTO, @AuthenticationPrincipal UserModel authenticatedUser) {
        var response = addressService.createPersonalAddress(addressRequestDTO, authenticatedUser.getId());
        if (response == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/rentable")
    public ResponseEntity<AddressResponseDTO> postRentableAddress(@RequestBody AddressRequestDTO addressRequestDTO, @AuthenticationPrincipal UserModel authenticatedUser) {
        var response = addressService.createRentableAddress(addressRequestDTO, authenticatedUser.getId());
        if (response == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/rentable")
    public ResponseEntity<?> getRentableAddresses(
            @RequestParam(name = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(name="size", required = false, defaultValue = "12") Integer pageSize
            ) {
        var addresses = addressService.getAllRentableAddresses(page, pageSize);
        return ResponseEntity.ok(addresses);
    }

    @GetMapping("/personal/{id}")
    public ResponseEntity<AddressResponseDTO> getPersonalAddressByUserId(@PathVariable UUID id) {
        var address = addressService.getPersonalAddressByUserId(id);
        return ResponseEntity.ok(address);
    }

    @GetMapping("/rentable/me")
    public ResponseEntity<List<AddressResponseDTO>> getRentableAddressesByUserId(Authentication authentication) {
        var addresses = addressService.getOwnedRentableAddresses(authentication);
        return ResponseEntity.ok(addresses);
    }

    @GetMapping("/rentable/{id}")
    public ResponseEntity<AddressResponseDTO> getRentableAddressById(@PathVariable UUID id) {
        var address = addressService.getRentableAddressById(id);
        return ResponseEntity.ok(address);
    }

    @PatchMapping("/personal")
    public ResponseEntity<AddressResponseDTO> updatePersonalAddress(@RequestParam(name = "addressid") UUID addressId,
                                                                    @RequestBody AddressRequestDTO addressRequestDTO,
                                                                    @AuthenticationPrincipal UserModel authenticatedUser) {
        var updatedAddress = addressService.updatePersonalAddress(addressId, authenticatedUser.getId(), addressRequestDTO);
        return ResponseEntity.ok(updatedAddress);
    }

    @PatchMapping("/rentable/{id}")
    public ResponseEntity<AddressResponseDTO> updateRentableAddress(
            @PathVariable UUID id,
            @RequestBody @Valid AddressRequestDTO addressRequestDTO,
            @AuthenticationPrincipal UserModel authenticatedUser) {

        var updatedAddress = addressService.updateRentableAddress(id, authenticatedUser.getId(), addressRequestDTO);
        return ResponseEntity.ok(updatedAddress);
    }

    @DeleteMapping("/personal/{id}")
    public ResponseEntity<Void> deletePersonalAddress(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserModel authenticatedUser) {

        addressService.deletePersonalAddress(id, authenticatedUser.getId());
        return ResponseEntity.noContent().build(); // 204 No Content é o padrão ideal para Delete bem sucedido
    }

    @DeleteMapping("/rentable")
    public ResponseEntity<AddressResponseDTO> deleteRentableAddress(@RequestParam(name = "addressid") UUID addressId, @AuthenticationPrincipal UserModel authenticatedUser) {

        addressService.deleteRentableAddress(addressId, authenticatedUser.getId());

        return ResponseEntity.noContent().build();
    }
}





