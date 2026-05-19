package com.project.locusapi.dto.address.rentable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.project.locusapi.dto.address.AddressResponseDTO;
import com.project.locusapi.model.Rental;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
    private UUID hostId;
    private String hostName;
    @JsonIgnoreProperties("address")
    private List<RentableAddressImageResponseDTO> images;
    private List<String> amenities;
    private Double latitude;
    private Double longitude;
    private LocalDate availableFrom;
    private LocalDate availableTo;

    @JsonIgnoreProperties("rentableAddress")
    private List<Rental> rentals;
    private LocalDateTime createdAt;

}
