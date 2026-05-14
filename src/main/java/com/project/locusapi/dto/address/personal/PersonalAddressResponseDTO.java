package com.project.locusapi.dto.address.personal;

import com.project.locusapi.dto.address.AddressResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
public class PersonalAddressResponseDTO extends AddressResponseDTO {
    private String addressName;
}
