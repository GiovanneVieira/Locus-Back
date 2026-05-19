package com.project.locusapi.model;

import com.project.locusapi.domain.CEP;
import com.project.locusapi.mapper.converter.CepConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "address_id")
    private UUID id;

    @Column(nullable = false)
    private String street;

    @Column(nullable = false)
    private String neighborhood;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String country;

    @Column(nullable = false)
    private String state;

    @Column(name = "house_number", nullable = false)
    private Integer houseNumber;

    // O CepConverter com autoApply=true interceptará esse tipo automaticamente
    @Column(name = "cep_code", length = 8, nullable = false)
    @Convert(converter = CepConverter.class)
    private CEP cep;


    public Address(String street, String city, String country, String state, Integer houseNumber, CEP cep) {
        this.street = street;
        this.city = city;
        this.country = country;
        this.state = state;
        this.houseNumber = houseNumber;
        this.cep = cep;
    }
}