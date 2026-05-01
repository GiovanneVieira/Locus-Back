package com.project.locusapi.mapper;

import com.project.locusapi.dto.address.AddressRequestDTO;
import com.project.locusapi.dto.address.AddressResponseDTO;
import com.project.locusapi.model.Address;
import com.project.locusapi.model.PersonalAddressModel;
import com.project.locusapi.model.RentableAddressModel;
import org.springframework.stereotype.Component;

@Component
public class AddressMapper {

    public <T extends Address> T toModel(AddressRequestDTO dto, Class<T> targetClass) {
        if (targetClass.equals(PersonalAddressModel.class)) {
            PersonalAddressModel address = new PersonalAddressModel(dto.street(), dto.city(), dto.country(), dto.state(), dto.houseNumber(), dto.cep());
            return targetClass.cast(address);
        }
        if (targetClass.equals(RentableAddressModel.class)) {
            RentableAddressModel address = new RentableAddressModel(dto.street(), dto.city(), dto.country(), dto.state(), dto.houseNumber(), dto.cep());
            return targetClass.cast(address);
        }
        throw new IllegalArgumentException("Tipo de endereço desconhecido");
    }

    public <T extends Address> AddressRequestDTO toRequestDTO(T address) {
        if (address instanceof PersonalAddressModel) {
            return new AddressRequestDTO(address.getCity(), address.getStreet(), address.getCountry(), address.getState(), address.getHouseNumber(), address.getCep());
        }
        if (address instanceof RentableAddressModel) {
            return new AddressRequestDTO(address.getCity(), address.getStreet(), address.getCountry(), address.getState(), address.getHouseNumber(), address.getCep());
        }
        throw new IllegalArgumentException("Tipo de endereço desconhecido");
    }

    public <T extends Address> AddressResponseDTO toResponseDTO(T address) {

        if (address instanceof PersonalAddressModel) {
            var user = ((PersonalAddressModel) address).getUser();
            return new AddressResponseDTO(address.getId(),
                    address.getCity(),
                    address.getStreet(),
                    address.getCountry(),
                    address.getState(),
                    address.getHouseNumber(),
                    address.getCep(),
                    user.getId());
        }
        if (address instanceof RentableAddressModel) {
            var user = ((RentableAddressModel) address).getUser();
            return new AddressResponseDTO(address.getId(),
                    address.getCity(),
                    address.getStreet(),
                    address.getCountry(),
                    address.getState(),
                    address.getHouseNumber(),
                    address.getCep(),
                    user.getId());
        }
        throw new IllegalArgumentException("Tipo de endereço desconhecido");
    }
}
