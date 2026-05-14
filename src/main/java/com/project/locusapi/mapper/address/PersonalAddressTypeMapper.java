package com.project.locusapi.mapper.address;

import com.project.locusapi.dto.address.personal.PersonalAddressRequestDTO;
import com.project.locusapi.dto.address.personal.PersonalAddressResponseDTO;
import com.project.locusapi.model.Address;
import com.project.locusapi.model.PersonalAddressModel;
import org.springframework.stereotype.Component;

@Component
public class PersonalAddressTypeMapper extends BaseAddressTypeMapper<PersonalAddressModel, PersonalAddressRequestDTO, PersonalAddressResponseDTO> {

    @Override
    public boolean supports(Class<?> clazz) { return PersonalAddressModel.class.equals(clazz); }


    @Override
    public boolean supports(Address model) { return model instanceof PersonalAddressModel; }

    @Override
    public PersonalAddressModel toModel(PersonalAddressRequestDTO dto) {
        return new PersonalAddressModel(
                dto.getStreet(), dto.getCity(), dto.getCountry(),
                dto.getState(), dto.getHouseNumber(), dto.getCep(),
                dto.getAddressName()
        );
    }

    @Override
    public PersonalAddressResponseDTO toResponseDto(PersonalAddressModel model) {
        var builder = PersonalAddressResponseDTO.builder();
        mapCommonFieldsToResponse(model, builder);
        builder.addressName(model.getAddressName());
        return builder.build();
    }

    @Override
    public void update(PersonalAddressRequestDTO dto, PersonalAddressModel model) {
        updateCommonFields(dto, model);
    }
}
