package com.project.locusapi.controller;

import com.project.locusapi.dto.address.AddressRequestDTO;
import com.project.locusapi.dto.address.AddressResponseDTO;
import com.project.locusapi.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/address")
@RequiredArgsConstructor
public class AddressController {
    private final AddressService addressService;

    @PostMapping()
    public ResponseEntity<AddressResponseDTO> postAddress(@RequestBody AddressRequestDTO addressRequestDTO) {
        var response = addressService.createAddress(addressRequestDTO);
        if (response == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping()
    public ResponseEntity<List<AddressResponseDTO>> getAddress() {
        var addresses = addressService.getAllAddress();
        return ResponseEntity.ok(addresses);
    }
}
