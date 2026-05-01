package com.project.locusapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "rentable_address_table")
@Getter
@Setter
@NoArgsConstructor
public class RentableAddressModel extends Address {

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private UserModel user;

    private boolean isRentable = true;

    public RentableAddressModel(String street, String city, String country, String state, Integer integer, String cep) {
        super(street, city, country, state, integer, cep);
    }
}
