package com.project.locusapi.mapper.address;

import com.project.locusapi.dto.address.AddressRequestDTO;
import com.project.locusapi.dto.address.AddressResponseDTO;
import com.project.locusapi.model.Address;

public interface TypeMapper<M extends Address, Q extends AddressRequestDTO, R extends AddressResponseDTO> {
    boolean supports(Class<?> clazz);
    boolean supports(Address model);

    M toModel(Q dto);
    R toResponseDto(M model);
    void update(Q dto, M model);
}

