package com.project.locusapi.mapper;

import com.project.locusapi.dto.address.AddressRequestDTO;
import com.project.locusapi.dto.address.AddressResponseDTO;
import com.project.locusapi.model.AddressModel;
import org.springframework.stereotype.Component;

@Component
public class AddressMapper {

    public AddressModel toModel(AddressRequestDTO dto) {
        return AddressModel.builder()
                .city(dto.city())
                .street(dto.street())
                .build();
    }

    public AddressRequestDTO toRequestDTO(AddressModel model) {
        return new AddressRequestDTO(model.getCity(), model.getStreet());
    }

    public AddressResponseDTO toResponseDTO(AddressModel model) {
        return new AddressResponseDTO(model.getCity(), model.getStreet());
    }
}
