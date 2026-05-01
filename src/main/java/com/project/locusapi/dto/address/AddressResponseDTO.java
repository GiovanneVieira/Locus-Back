package com.project.locusapi.dto.address;

import com.project.locusapi.model.UserModel;

import java.util.UUID;

public record AddressResponseDTO(UUID id, String city, String street, String country, String state,
                                 Integer houseNumber, String cep, UUID userId) {
}
