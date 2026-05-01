package com.project.locusapi.dto.address;

import com.project.locusapi.model.UserModel;

import java.util.UUID;

public record AddressResponseDTO(UUID id, String addressCity, String addressStreet, String country, String city,
                                 Integer houseNumber, String street, UUID userId) {
}
