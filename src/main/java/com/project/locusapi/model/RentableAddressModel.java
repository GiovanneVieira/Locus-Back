package com.project.locusapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.locusapi.domain.CEP;
import com.project.locusapi.model.s3filemetadata.RentableAddressImage;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "rentable_address_table")
@Getter
@Setter
@NoArgsConstructor
public class RentableAddressModel extends Address {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private UserModel user;

    @OneToMany(mappedBy = "address", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("address") // Evita o loop infinito de serialização que gerava o erro de profundidade (Nesting Depth)
    private List<RentableAddressImage> images = new ArrayList<>();

    @Column(nullable = false, length = 120)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    private String complement;

    @Column(name = "price_per_night", nullable = false)
    private Integer pricePerNight;

    @Column(name = "max_guests", nullable = false)
    private Integer maxGuests;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "address_amenities", joinColumns = @JoinColumn(name = "address_id"))
    @Column(name = "amenity_name")
    private List<String> amenities = new ArrayList<>();

    @Column(name = "available_from", nullable = false)
    private LocalDate availableFrom;

    @Column(name = "available_to", nullable = false)
    private LocalDate availableTo;

    @OneToMany(mappedBy = "rentableAddress", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("address")
    private List<Rental> rentals = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public RentableAddressModel(
            String street,
            String city,
            String country,
            String state,
            Integer houseNumber,
            CEP cep,
            String title,
            String description,
            String complement,
            Integer pricePerNight,
            Integer maxGuests,
            LocalDate availableFrom,
            LocalDate availableTo) {
        super(street, city, country, state, houseNumber, cep);
        this.title = title;
        this.description = description;
        this.complement = complement;
        this.pricePerNight = pricePerNight;
        this.maxGuests = maxGuests;
        this.availableFrom = availableFrom;
        this.availableTo = availableTo;
    }
}