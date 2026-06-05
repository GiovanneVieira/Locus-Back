package com.project.locusapi.dto.rating;

import jakarta.validation.constraints.PositiveOrZero;

public record RatingRequestDTO(@PositiveOrZero Double ratingValue) {
}
