package com.project.locusapi.dto.rating;

import java.util.UUID;

public record RatingResponseDTO(Double ratingValue,
                                UUID addressId,
                                UUID userId) {
}
