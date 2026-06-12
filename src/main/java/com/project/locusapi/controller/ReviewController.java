package com.project.locusapi.controller;

import com.project.locusapi.dto.review.ReviewRequestDTO;
import com.project.locusapi.dto.review.ReviewResponseDTO;
import com.project.locusapi.model.UserModel;
import com.project.locusapi.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // Leitura pública das avaliações de um imóvel
    @GetMapping("/address/{addressId}")
    public ResponseEntity<List<ReviewResponseDTO>> getByAddress(@PathVariable UUID addressId) {
        return ResponseEntity.ok(reviewService.listByAddress(addressId));
    }

    // Cria ou atualiza a avaliação do usuário autenticado
    @PostMapping("/address/{addressId}")
    public ResponseEntity<ReviewResponseDTO> createOrUpdate(
            @PathVariable UUID addressId,
            @RequestBody @Valid ReviewRequestDTO dto,
            @AuthenticationPrincipal UserModel authenticatedUser) {

        ReviewResponseDTO response = reviewService.createOrUpdate(addressId, authenticatedUser.getId(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID reviewId,
            @AuthenticationPrincipal UserModel authenticatedUser) {

        reviewService.delete(reviewId, authenticatedUser.getId());
        return ResponseEntity.noContent().build();
    }
}
