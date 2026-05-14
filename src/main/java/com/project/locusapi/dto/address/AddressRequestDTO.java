package com.project.locusapi.dto.address;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.project.locusapi.dto.address.personal.PersonalAddressRequestDTO;
import com.project.locusapi.dto.address.rentable.RentableAddressRequestDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type" // O campo que o frontend enviará
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = PersonalAddressRequestDTO.class, name = "PERSONAL"),
        @JsonSubTypes.Type(value = RentableAddressRequestDTO.class, name = "RENTABLE")}
)
public abstract class AddressRequestDTO {
    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "Street is required")
    private String street;

    @NotBlank(message = "Country is required")
    private String country;

    @NotBlank(message = "State is required")
    private String state;

    @NotNull(message = "house number is required")
    private Integer houseNumber;

    @NotBlank(message = "ZIP Code is required")
    private String cep;
}