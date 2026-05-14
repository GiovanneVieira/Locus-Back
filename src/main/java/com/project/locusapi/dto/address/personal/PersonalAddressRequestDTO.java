package com.project.locusapi.dto.address.personal;

import com.project.locusapi.dto.address.AddressRequestDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class PersonalAddressRequestDTO extends AddressRequestDTO {

    private String addressName;

}
