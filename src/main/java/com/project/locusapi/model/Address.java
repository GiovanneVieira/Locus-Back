package com.project.locusapi.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "address_id")
    private UUID id;

    private String street;
    private String city;
    private String country;
    private String state;
    private Integer houseNumber;
    private String cep;
    private Boolean isRentable;

    public Address(String street, String city, String country, String state, Integer houseNumber, String cep, Boolean isRentable) {
        this.street = street;
        this.city = city;
        this.country = country;
        this.state = state;
        this.houseNumber = houseNumber;
        this.cep = cep;
        this.isRentable = isRentable;
    }

    public Address() {

    }
}
