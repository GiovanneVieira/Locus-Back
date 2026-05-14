package com.project.locusapi.mapper.address;

import com.project.locusapi.dto.address.AddressRequestDTO;
import com.project.locusapi.dto.address.AddressResponseDTO;
import com.project.locusapi.model.Address;

public abstract class BaseAddressTypeMapper<M extends Address, Q extends AddressRequestDTO, R extends AddressResponseDTO>
        implements TypeMapper<M, Q, R> {

    protected void mapCommonFieldsToResponse(Address model, AddressResponseDTO.AddressResponseDTOBuilder<?, ?> builder) {
        builder.id(model.getId())
                .city(model.getCity())
                .street(model.getStreet())
                .country(model.getCountry())
                .state(model.getState())
                .houseNumber(String.valueOf(model.getHouseNumber()))
                .cep(model.getCep())
                .isRentable(model.getIsRentable());
    }

    protected void updateCommonFields(AddressRequestDTO dto, Address model) {
        if (dto.getCity() != null) model.setCity(dto.getCity());
        if (dto.getCountry() != null) model.setCountry(dto.getCountry());
        if (dto.getCep() != null) model.setCep(dto.getCep());
        if (dto.getStreet() != null) model.setStreet(dto.getStreet());
        if (dto.getHouseNumber() != null) model.setHouseNumber(dto.getHouseNumber());
        if (dto.getState() != null) model.setState(dto.getState());
    }
}
