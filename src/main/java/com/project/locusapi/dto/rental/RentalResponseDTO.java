package com.project.locusapi.dto.rental;

import com.project.locusapi.constant.RentalStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record RentalResponseDTO(
        UUID id,
        UUID addressId,
        String addressTitle,
        String addressCity,
        UUID coverImageId,
        UUID hostId,
        String hostName,
        UUID guestId,
        String guestName,
        LocalDate checkIn,
        LocalDate checkOut,
        Integer guests,
        Integer nights,
        Integer pricePerNight,
        Integer totalPrice,
        RentalStatus status,
        String message,
        LocalDateTime createdAt
) {}
