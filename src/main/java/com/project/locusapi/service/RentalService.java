package com.project.locusapi.service;

import com.project.locusapi.constant.RentalStatus;
import com.project.locusapi.dto.rental.RentalRequestDTO;
import com.project.locusapi.dto.rental.RentalResponseDTO;
import com.project.locusapi.exception.business.AddressNotFoundException;
import com.project.locusapi.exception.business.RentalNotFoundException;
import com.project.locusapi.exception.business.UserNotFoundException;
import com.project.locusapi.model.RentableAddressModel;
import com.project.locusapi.model.Rental;
import com.project.locusapi.model.s3filemetadata.RentableAddressImage;
import com.project.locusapi.repository.RentableAddressRepository;
import com.project.locusapi.repository.RentalRepository;
import com.project.locusapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RentalService {

    private final RentalRepository rentalRepository;
    private final RentableAddressRepository rentableAddressRepository;
    private final UserRepository userRepository;

    @Transactional
    public RentalResponseDTO create(UUID addressId, UUID userId, RentalRequestDTO dto) {
        var address = rentableAddressRepository.findById(addressId)
                .orElseThrow(() -> new AddressNotFoundException(addressId, "Imóvel locável"));

        if (address.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Você não pode reservar o seu próprio imóvel.");
        }

        validateDates(dto, address);

        if (dto.guests() > address.getMaxGuests()) {
            throw new IllegalArgumentException(
                    "O imóvel comporta no máximo " + address.getMaxGuests() + " hóspedes.");
        }

        var renter = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        String message = (dto.message() != null && !dto.message().isBlank())
                ? dto.message().trim()
                : null;

        var rental = Rental.builder()
                .renter(renter)
                .rentableAddress(address)
                .checkIn(dto.checkIn().atStartOfDay())
                .checkOut(dto.checkOut().atStartOfDay())
                .numberOfGuests(dto.guests())
                .message(message)
                .priceAtTheTime(address.getPricePerNight().doubleValue())
                .status(RentalStatus.PENDING)
                .build();

        return toResponse(rentalRepository.save(rental));
    }

    @Transactional(readOnly = true)
    public List<RentalResponseDTO> getMyBookings(UUID userId) {
        return rentalRepository.findAllByRenterIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<RentalResponseDTO> getHostBookings(UUID hostId) {
        return rentalRepository.findAllByRentableAddress_User_IdOrderByCreatedAtDesc(hostId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public RentalResponseDTO updateStatus(UUID rentalId, UUID userId, RentalStatus newStatus) {
        var rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new RentalNotFoundException(rentalId));

        boolean isGuest = rental.getRenter().getId().equals(userId);
        boolean isHost = rental.getRentableAddress().getUser().getId().equals(userId);

        if (!isGuest && !isHost) {
            throw new AccessDeniedException("Você não tem permissão sobre esta reserva.");
        }

        switch (newStatus) {
            case CANCELLED -> {
                if (!isGuest) {
                    throw new AccessDeniedException("Apenas o hóspede pode cancelar a reserva.");
                }
                if (rental.getStatus() == RentalStatus.DECLINED || rental.getStatus() == RentalStatus.CANCELLED) {
                    throw new IllegalArgumentException("Esta reserva não pode mais ser cancelada.");
                }
            }
            case CONFIRMED, DECLINED -> {
                if (!isHost) {
                    throw new AccessDeniedException("Apenas o anfitrião pode aceitar ou recusar a reserva.");
                }
                if (rental.getStatus() != RentalStatus.PENDING) {
                    throw new IllegalArgumentException("Só é possível responder a reservas pendentes.");
                }
            }
            default -> throw new IllegalArgumentException("Transição de status inválida.");
        }

        rental.setStatus(newStatus);
        return toResponse(rentalRepository.save(rental));
    }

    // =========================================================================
    // AUXILIARES
    // =========================================================================

    private void validateDates(RentalRequestDTO dto, RentableAddressModel address) {
        if (!dto.checkOut().isAfter(dto.checkIn())) {
            throw new IllegalArgumentException("A data de saída deve ser posterior à data de entrada.");
        }
        if (dto.checkIn().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("A data de entrada não pode estar no passado.");
        }
        if (address.getAvailableFrom() != null && dto.checkIn().isBefore(address.getAvailableFrom())) {
            throw new IllegalArgumentException(
                    "O imóvel só está disponível a partir de " + address.getAvailableFrom() + ".");
        }
        if (address.getAvailableTo() != null && dto.checkOut().isAfter(address.getAvailableTo())) {
            throw new IllegalArgumentException(
                    "O imóvel só está disponível até " + address.getAvailableTo() + ".");
        }
    }

    private RentalResponseDTO toResponse(Rental rental) {
        var address = rental.getRentableAddress();
        var host = address.getUser();
        var guest = rental.getRenter();

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
