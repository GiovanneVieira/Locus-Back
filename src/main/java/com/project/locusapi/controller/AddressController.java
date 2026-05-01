package com.project.locusapi.controller;

import com.project.locusapi.dto.address.AddressRequestDTO;
import com.project.locusapi.dto.address.AddressResponseDTO;
import com.project.locusapi.model.UserModel;
import com.project.locusapi.service.AddressService;
import com.project.locusapi.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<List<AddressResponseDTO>> getRentableAddresses() {
        var addresses = addressService.getAllRentableAddresses();
        return ResponseEntity.ok(addresses);
    }

    @GetMapping("/personal/{id}")
    public ResponseEntity<AddressResponseDTO> getPersonalAddressByUserId(@PathVariable UUID id) {
        var address = addressService.getPersonalAddressByUserId(id);
        return ResponseEntity.ok(address);
    }

    @GetMapping("/rentable/{id}")
    public ResponseEntity<List<AddressResponseDTO>> getRentableAddressByUserId(@PathVariable UUID id) {
        var addresses = addressService.getRentableAddressesByUserId(id);
        return ResponseEntity.ok(addresses);
    }

    @PatchMapping("personal")
    public ResponseEntity<AddressResponseDTO> updatePersonalAddress(@RequestParam(name = "addressid") UUID addressId,
                                                                    @RequestBody AddressRequestDTO addressRequestDTO,
                                                                    @AuthenticationPrincipal UserModel authenticatedUser) {
        var updatedAddress = addressService.updatePersonalAddress(addressId, authenticatedUser.getId(), addressRequestDTO);
        return ResponseEntity.ok(updatedAddress);
    }

    @PatchMapping("rentable")
    public ResponseEntity<AddressResponseDTO> updateRentableAddress(@RequestParam(name = "addressid") UUID addressId,
                                                                    @RequestBody AddressRequestDTO addressRequestDTO,
                                                                    @AuthenticationPrincipal UserModel authenticatedUser) {
        var updatedAddress = addressService.updatePersonalAddress(addressId, authenticatedUser.getId(), addressRequestDTO);
        return ResponseEntity.ok(updatedAddress);
    }

    @DeleteMapping("personal")
    public ResponseEntity<AddressResponseDTO> deletePersonalAddress(@RequestParam(name = "addressid") UUID addressId, @AuthenticationPrincipal UserModel authenticatedUser) {

        var deletedAddress = addressService.deletePersonalAddress(addressId, authenticatedUser.getId());
        return ResponseEntity.ok(deletedAddress);
    }

    @DeleteMapping("rentable")
    public ResponseEntity<AddressResponseDTO> deleteRentableAddress(@RequestParam(name = "addressid") UUID addressId, @AuthenticationPrincipal UserModel authenticatedUser) {

        var deletedAddress = addressService.deleteRentableAddress(addressId, authenticatedUser.getId());
        return ResponseEntity.ok(deletedAddress);
    }
}





