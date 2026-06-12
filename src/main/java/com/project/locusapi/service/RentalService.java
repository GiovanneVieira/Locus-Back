package com.project.locusapi.service;

import com.project.locusapi.constant.RentalStatus;
import com.project.locusapi.dto.rental.RentalRequestDTO;
import com.project.locusapi.dto.rental.RentalResponseDTO;
import com.project.locusapi.event.metrics.RentalCreatedEvent;
import com.project.locusapi.event.metrics.RentalStatusChangedEvent;
import com.project.locusapi.exception.business.AddressNotFoundException;
import com.project.locusapi.exception.business.RentalNotFoundException;
import com.project.locusapi.exception.business.UserNotFoundException;
import com.project.locusapi.mapper.RentalMapper;
import com.project.locusapi.model.RentableAddressModel;
import com.project.locusapi.model.Rental;
import com.project.locusapi.repository.RentableAddressRepository;
import com.project.locusapi.repository.RentalRepository;
import com.project.locusapi.repository.UserRepository;
import com.project.locusapi.service.rental.RentalStatusTransitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RentalService {

    private final RentalRepository rentalRepository;
    private final RentableAddressRepository rentableAddressRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final RentalMapper rentalMapper;
    private final RentalStatusTransitionService statusTransitionService;

    @Transactional
    public RentalResponseDTO create(UUID addressId, UUID userId, RentalRequestDTO dto) {
        var address = rentableAddressRepository.findById(addressId)
                .orElseThrow(() -> new AddressNotFoundException(addressId, "Imóvel locável"));

        if (address.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Você não pode reservar o seu próprio imóvel.");
        }

        validateDates(dto, address);
        validateGuestCapacity(dto, address);

        var renter = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        var rental = Rental.builder()
                .renter(renter)
                .rentableAddress(address)
                .checkIn(dto.checkIn().atStartOfDay())
                .checkOut(dto.checkOut().atStartOfDay())
                .numberOfGuests(dto.guests())
                .message(normalizeMessage(dto.message()))
                .priceAtTheTime(address.getPricePerNight().doubleValue())
                .status(RentalStatus.PENDING)
                .build();

        var savedRental = rentalRepository.save(rental);
        eventPublisher.publishEvent(new RentalCreatedEvent(savedRental.getId(), renter.getId(), address.getId(), savedRental.getStatus(), LocalDateTime.now()));
        return rentalMapper.toResponse(savedRental);
    }

    @Transactional(readOnly = true)
    public List<RentalResponseDTO> getMyBookings(UUID userId) {
        return rentalRepository.findBookingsWithDetailsByRenterId(userId)
                .stream()
                .map(rentalMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RentalResponseDTO> getHostBookings(UUID hostId) {
        return rentalRepository.findBookingsWithDetailsByHostId(hostId)
                .stream()
                .map(rentalMapper::toResponse)
                .toList();
    }

    @Transactional
    public RentalResponseDTO updateStatus(UUID rentalId, UUID userId, RentalStatus newStatus) {
        var rental = rentalRepository.findByIdWithDetails(rentalId)
                .orElseThrow(() -> new RentalNotFoundException(rentalId));

        statusTransitionService.validate(rental, userId, newStatus);

        RentalStatus previousStatus = rental.getStatus();
        rental.setStatus(newStatus);

        var savedRental = rentalRepository.save(rental);
        eventPublisher.publishEvent(new RentalStatusChangedEvent(savedRental.getId(), previousStatus, newStatus, LocalDateTime.now()));
        return rentalMapper.toResponse(savedRental);
    }

    private void validateDates(RentalRequestDTO dto, RentableAddressModel address) {
        if (!dto.checkOut().isAfter(dto.checkIn())) {
            throw new IllegalArgumentException("A data de saída deve ser posterior à data de entrada.");
        }
        if (dto.checkIn().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("A data de entrada não pode estar no passado.");
        }
        if (address.getAvailableFrom() != null && dto.checkIn().isBefore(address.getAvailableFrom())) {
            throw new IllegalArgumentException("O imóvel só está disponível a partir de " + address.getAvailableFrom() + ".");
        }
        if (address.getAvailableTo() != null && dto.checkOut().isAfter(address.getAvailableTo())) {
            throw new IllegalArgumentException("O imóvel só está disponível até " + address.getAvailableTo() + ".");
        }
    }

    private void validateGuestCapacity(RentalRequestDTO dto, RentableAddressModel address) {
        if (dto.guests() > address.getMaxGuests()) {
            throw new IllegalArgumentException("O imóvel comporta no máximo " + address.getMaxGuests() + " hóspedes.");
        }
    }

    private String normalizeMessage(String message) {
        return message != null && !message.isBlank() ? message.trim() : null;
    }
}
