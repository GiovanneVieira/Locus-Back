package com.project.locusapi.dto.address.rentable;

import com.project.locusapi.dto.address.AddressResponseDTO;
import com.project.locusapi.model.Rental;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

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
    private List<String> imageUrls;
    private List<String> amenities;
    private LocalDateTime availableFrom;
    private LocalDateTime availableTo;
    private List<Rental> rentals;


}
