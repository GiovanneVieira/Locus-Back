package com.project.locusapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "rentable_address_table")
@Getter
@Setter
@NoArgsConstructor
public class RentableAddressModel extends Address {

    @ManyToOne(fetch = FetchType.LAZY) // Lazy para performance
    @JoinColumn(name = "user_id")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private UserModel user;

    @ElementCollection
    @CollectionTable(name = "address_images", joinColumns = @JoinColumn(name = "address_id"))
    private List<String> imageUrls = new ArrayList<>();

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String complement;
    private Integer pricePerNight;
    private Integer maxGuests;

    @ElementCollection // Necessário para List<String>
    @CollectionTable(name = "address_amenities", joinColumns = @JoinColumn(name = "address_id"))
    private List<String> amenities = new ArrayList<>();

    private LocalDateTime availableFrom;
    private LocalDateTime availableTo;

    @OneToMany(fetch = FetchType.LAZY)
    private List<Rental> rentals;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public RentableAddressModel(
            String street,
            String city,
            String country,
            String state,
            Integer integer,
            String cep,
            String title,
            String description,
            String complement,
            Integer pricePerNight,
            Integer maxGuests,
            LocalDateTime availableFrom,
            LocalDateTime availableTo) {
        super(street, city, country, state, integer, cep, true);
        this.imageUrls = new ArrayList<>();
        this.rentals = new ArrayList<>();
        this.amenities = new ArrayList<>();
        this.title = title;
        this.description = description;
        this.complement = complement;
        this.pricePerNight = pricePerNight;
        this.maxGuests = maxGuests;
        this.availableFrom = availableFrom;
        this.availableTo = availableTo;

    }
}
