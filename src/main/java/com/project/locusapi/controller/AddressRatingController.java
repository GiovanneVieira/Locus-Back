package com.project.locusapi.controller;

import com.project.locusapi.dto.rating.RatingRequestDTO;
import com.project.locusapi.dto.rating.RatingResponseDTO;
import com.project.locusapi.mapper.rating.RatingMapper;
import com.project.locusapi.model.UserModel;
import com.project.locusapi.service.ratings.RatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/address/rentable/rating")
@RequiredArgsConstructor
public class AddressRatingController {

    private final RatingService ratingService;
    private final RatingMapper ratingMapper;

    @PostMapping("/{addressId}")
    public ResponseEntity<?> postRating(@PathVariable("addressId") UUID addressId,
                                        @RequestBody @Valid RatingRequestDTO ratingRequestDTO,
                                        @AuthenticationPrincipal UserModel user) {
        var rating = ratingService.postRatingToAddress(ratingRequestDTO, user, addressId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ratingMapper.toRatingResponseDTO(rating));
    }

    @GetMapping("/{addressId}")
    public ResponseEntity<List<RatingResponseDTO>> getAllRatingsToAddress(@PathVariable("addressId") UUID addressId) {
        var response = ratingService.getAllRatingsToAddress(addressId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/avg/{addressId}")
    public ResponseEntity<?> getRatingAvg(@PathVariable("addressId") UUID addressId) {
        var response = ratingService.getAverageRatingsToAddress(addressId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
