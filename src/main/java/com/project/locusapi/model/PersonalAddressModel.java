package com.project.locusapi.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "personal_address_table")
@Getter
@Setter
@NoArgsConstructor
public class PersonalAddressModel extends Address {


    @OneToOne(mappedBy = "personalAddress")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private UserModel user;

    private boolean isRentable = false;

    public PersonalAddressModel(String street, String city, String country, String state, Integer houseNumber, String cep) {
        super(street, city, country, state, houseNumber, cep);
    }
}