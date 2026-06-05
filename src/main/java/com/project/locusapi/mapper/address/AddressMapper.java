package com.project.locusapi.mapper.address;

import com.project.locusapi.dto.address.AddressRequestDTO;
import com.project.locusapi.dto.address.AddressResponseDTO;
import com.project.locusapi.model.Address;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AddressMapper {

    private final List<TypeMapper<? extends Address, ? extends AddressRequestDTO, ? extends AddressResponseDTO>> mappers;

    @SuppressWarnings("unchecked")
    public <T extends Address> T toModel(AddressRequestDTO dto, Class<T> targetClass) {
        TypeMapper mapper = findMapper(targetClass);
        return (T) mapper.toModel(dto);
    }

    @SuppressWarnings("unchecked")
    public AddressResponseDTO toResponseDTO(Address address) {
        if (address == null) return null;
        TypeMapper mapper = findMapper(address);
        return mapper.toResponseDto(address);
    }

    private TypeMapper findMapper(Class<?> clazz) {
        return mappers.stream()
                .filter(m -> m.supports(clazz))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Mapper não encontrado para classe: " + clazz.getSimpleName()));
    }

    private TypeMapper findMapper(Address model) {
        return mappers.stream()
                .filter(m -> m.supports(model))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Mapper não encontrado para o modelo: " + model.getClass().getSimpleName()));
    }

    @SuppressWarnings("unchecked")
    public <M extends Address> void updateModel(AddressRequestDTO dto, M model) {
        TypeMapper mapper = findMapper(model);
        mapper.update(dto, model);
    }
}
