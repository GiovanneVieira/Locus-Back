package com.project.locusapi.dto.address;

import com.project.locusapi.domain.CEP;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public abstract class AddressResponseDTO {
    private UUID id;
    private String city;
    private String street;
    private String country;
    private String state;
    private String houseNumber;
    private CEP cep;
    private Boolean isRentable;
}
