package com.project.locusapi.dto.address;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.project.locusapi.domain.CEP;
import com.project.locusapi.dto.address.personal.PersonalAddressRequestDTO;
import com.project.locusapi.dto.address.rentable.RentableAddressRequestDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
        property = "type"
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

    @NotBlank(message = "neighborhood is required")
    private String neighborhood;

    @NotBlank(message = "Country is required")
    private String country;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "house number is required")
    @JsonProperty("number")
    @JsonAlias("houseNumber")
    @Pattern(
            regexp = "^[A-Za-z0-9\\s/-]{1,10}$",
            message = "O número do endereço contém caracteres inválidos ou é muito longo."
    )
    private String houseNumber;

    @NotNull(message = "ZIP Code is required")
    @JsonProperty("zipCode")
    @JsonAlias("cep")
    private CEP cep;
}