package com.project.locusapi.mapper;

import com.project.locusapi.dto.destination.DestinationRequestDTO;
import com.project.locusapi.dto.destination.DestinationResponseDTO;
import com.project.locusapi.dto.destination.TouristPointDTO;
import com.project.locusapi.dto.destination.TouristPointResponseDTO;
import com.project.locusapi.model.Destination;
import com.project.locusapi.model.TouristPoint;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DestinationMapper {

    public Destination toEntity(DestinationRequestDTO dto) {
        return Destination.builder()
                .country(dto.country().trim())
                .city(dto.city().trim())
                .build();
    }

    public DestinationResponseDTO toResponseDTO(Destination destination) {
        return new DestinationResponseDTO(
                destination.getId(),
                destination.getCountry(),
                destination.getCity(),
                destination.getTouristPoints()
                        .stream()
                        .map(this::toResponseDTO)
                        .toList()
        );
    }

    public TouristPoint toEntity(TouristPointDTO dto) {
        return TouristPoint.builder()
                .name(dto.nome().trim())
                .description(dto.descricao().trim())
                .category(dto.categoria().trim())
                .build();
    }

    public List<TouristPoint> toTouristPointEntities(List<TouristPointDTO> dtos) {
        return dtos.stream()
                .map(this::toEntity)
                .toList();
    }

    private TouristPointResponseDTO toResponseDTO(TouristPoint touristPoint) {
        return new TouristPointResponseDTO(
                touristPoint.getId(),
                touristPoint.getName(),
                touristPoint.getDescription(),
                touristPoint.getCategory()
        );
    }
}
