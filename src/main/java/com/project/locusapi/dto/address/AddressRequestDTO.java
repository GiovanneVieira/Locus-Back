package com.project.locusapi.dto.address;

public record AddressRequestDTO(String city, String street, String country, String state, Integer houseNumber, String cep) {
}
