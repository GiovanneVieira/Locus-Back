package com.project.locusapi.service;

import com.project.locusapi.dto.destination.DestinationAIResponse;
import com.project.locusapi.dto.destination.DestinationRequestDTO;
import com.project.locusapi.dto.destination.DestinationResponseDTO;
import com.project.locusapi.event.destination.DestinationCreatedEvent;
import com.project.locusapi.exception.business.DestinationNotFoundException;
import com.project.locusapi.mapper.DestinationMapper;
import com.project.locusapi.model.Destination;
import com.project.locusapi.repository.DestinationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DestinationService {

    private final DestinationRepository destinationRepository;
    private final DestinationMapper destinationMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public DestinationResponseDTO create(DestinationRequestDTO dto) {
        Destination destination = destinationRepository.save(destinationMapper.toEntity(dto));

        if (destination.getTouristPoints().isEmpty()) {
            eventPublisher.publishEvent(new DestinationCreatedEvent(destination.getId(), destination.getCity()));
        }

        return destinationMapper.toResponseDTO(destination);
    }

    @Transactional(readOnly = true)
    public DestinationResponseDTO findById(UUID id) {
        return destinationRepository.findById(id)
                .map(destinationMapper::toResponseDTO)
                .orElseThrow(() -> new DestinationNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public DestinationResponseDTO findByCity(String city) {
        return destinationRepository.findByCityIgnoreCase(city.trim())
                .map(destinationMapper::toResponseDTO)
                .orElseThrow(() -> new DestinationNotFoundException(city));
    }

    @Transactional(readOnly = true)
    public Page<DestinationResponseDTO> findAll(String city, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Destination> destinations = city == null || city.isBlank()
                ? destinationRepository.findAll(pageable)
                : destinationRepository.findByCityContainingIgnoreCase(city.trim(), pageable);

        return destinations.map(destinationMapper::toResponseDTO);
    }

    @Transactional
    public DestinationResponseDTO update(UUID id, DestinationRequestDTO dto) {
        Destination destination = destinationRepository.findById(id)
                .orElseThrow(() -> new DestinationNotFoundException(id));

        destination.setCountry(dto.country().trim());
        destination.setCity(dto.city().trim());

        return destinationMapper.toResponseDTO(destinationRepository.save(destination));
    }

    @Transactional
    public void delete(UUID id) {
        Destination destination = destinationRepository.findById(id)
                .orElseThrow(() -> new DestinationNotFoundException(id));

        destinationRepository.delete(destination);
    }

    @Transactional
    public void updateTouristPoints(UUID destinationId, DestinationAIResponse aiResponse) {
        Destination destination = destinationRepository.findById(destinationId)
                .orElseThrow(() -> new DestinationNotFoundException(destinationId));

        destination.setCountry(aiResponse.pais().trim());
        destination.setCity(aiResponse.destino().trim());
        destination.replaceTouristPoints(destinationMapper.toTouristPointEntities(aiResponse.pontosTuristicos()));
        destinationRepository.save(destination);
    }
}
