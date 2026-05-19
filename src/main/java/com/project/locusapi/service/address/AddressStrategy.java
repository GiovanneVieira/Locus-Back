package com.project.locusapi.service.address;

import com.project.locusapi.constant.AddressType;
import com.project.locusapi.dto.address.AddressRequestDTO;
import com.project.locusapi.dto.address.AddressResponseDTO;

import java.util.UUID;

public interface AddressStrategy {

    AddressType getSupportedType();

    AddressResponseDTO create(AddressRequestDTO dto, UUID userId);

    AddressResponseDTO update(UUID addressId, UUID authenticatedUserId, AddressRequestDTO dto);

    void delete(UUID addressId, UUID authenticatedUserId);
}