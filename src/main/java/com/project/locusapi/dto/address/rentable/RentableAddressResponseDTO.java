package com.project.locusapi.dto.address.rentable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.project.locusapi.dto.address.AddressResponseDTO;
import com.project.locusapi.model.Rental;
import com.project.locusapi.model.s3filemetadata.RentableAddressImage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
public class RentableAddressResponseDTO extends AddressResponseDTO {
    private String title;
    private String description;
    private String complement;
    private Integer pricePerNight;
    private Integer maxGuests;
    @JsonIgnoreProperties("address")
    private List<RentableAddressImage> images;
    private List<String> amenities;
    private LocalDate availableFrom;
    private LocalDate availableTo;

    @JsonIgnoreProperties("rentableAddress")
    private List<Rental> rentals;
    private LocalDateTime createdAt;

}
