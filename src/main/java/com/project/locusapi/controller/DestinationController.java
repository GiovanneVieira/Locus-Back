package com.project.locusapi.controller;

import com.project.locusapi.dto.destination.DestinationRequestDTO;
import com.project.locusapi.dto.destination.DestinationResponseDTO;
import com.project.locusapi.service.DestinationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/destinations")
@RequiredArgsConstructor
public class DestinationController {

    private final DestinationService destinationService;

    @PostMapping
    public ResponseEntity<DestinationResponseDTO> create(@RequestBody @Valid DestinationRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(destinationService.create(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DestinationResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(destinationService.findById(id));
    }

    @GetMapping("/city/{city}")
    public ResponseEntity<DestinationResponseDTO> findByCity(@PathVariable String city) {
        return ResponseEntity.ok(destinationService.findByCity(city));
    }

    @GetMapping
    public ResponseEntity<Page<DestinationResponseDTO>> findAll(
            @RequestParam(name = "city", required = false) String city,
            @RequestParam(name = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(name = "size", required = false, defaultValue = "12") Integer size) {

        return ResponseEntity.ok(destinationService.findAll(city, page, size));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DestinationResponseDTO> update(
            @PathVariable UUID id,
            @RequestBody @Valid DestinationRequestDTO dto) {

        return ResponseEntity.ok(destinationService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        destinationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
