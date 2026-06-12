package com.project.locusapi.mapper;

import com.project.locusapi.dto.rental.RentalResponseDTO;
import com.project.locusapi.model.RentableAddressModel;
import com.project.locusapi.model.Rental;
import com.project.locusapi.model.UserModel;
import com.project.locusapi.model.s3filemetadata.RentableAddressImage;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Component
public class RentalMapper {

    public RentalResponseDTO toResponse(Rental rental) {
        RentableAddressModel address = rental.getRentableAddress();
        UserModel host = address.getUser();
        UserModel guest = rental.getRenter();

        LocalDate checkIn = rental.getCheckIn().toLocalDate();
        LocalDate checkOut = rental.getCheckOut().toLocalDate();
        int nights = (int) ChronoUnit.DAYS.between(checkIn, checkOut);
        int pricePerNight = rental.getPriceAtTheTime() != null ? rental.getPriceAtTheTime().intValue() : 0;

        UUID coverImageId = address.getImages().stream()
                .filter(RentableAddressImage::isMain)
                .map(RentableAddressImage::getId)
                .findFirst()
                .orElseGet(() -> address.getImages().stream()
                        .map(RentableAddressImage::getId)
                        .findFirst()
                        .orElse(null));

        return new RentalResponseDTO(
                rental.getId(),
                address.getId(),
                address.getTitle(),
                address.getCity(),
                coverImageId,
                host.getId(),
                host.getName(),
                guest.getId(),
                guest.getName(),
                checkIn,
                checkOut,
                rental.getNumberOfGuests(),
                nights,
                pricePerNight,
                nights * pricePerNight,
                rental.getStatus(),
                rental.getMessage(),
                rental.getCreatedAt()
        );
    }
}
