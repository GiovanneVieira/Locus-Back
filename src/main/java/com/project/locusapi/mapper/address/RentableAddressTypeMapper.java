package com.project.locusapi.mapper.address;

import com.project.locusapi.dto.address.rentable.RentableAddressRequestDTO;
import com.project.locusapi.dto.address.rentable.RentableAddressResponseDTO;
import com.project.locusapi.model.Address;
import com.project.locusapi.model.RentableAddressModel;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class RentableAddressTypeMapper extends BaseAddressTypeMapper<RentableAddressModel, RentableAddressRequestDTO, RentableAddressResponseDTO> {

    @Override
    public boolean supports(Class<?> clazz) {
        return RentableAddressModel.class.equals(clazz);
    }

    @Override
    public boolean supports(Address model) {
        return model instanceof RentableAddressModel;
    }

    @Override
    public RentableAddressModel toModel(RentableAddressRequestDTO dto) {
        if (dto == null) return null;

        // Criando a instância usando o construtor super/base (ajustado para os campos comuns)
        RentableAddressModel model = new RentableAddressModel();

        // Campos herdados de Address (via setters ou lógica comum)
        model.setStreet(dto.getStreet());
        model.setCity(dto.getCity());
        model.setCountry(dto.getCountry());
        model.setState(dto.getState());
        model.setHouseNumber(dto.getHouseNumber());
        model.setCep(dto.getCep());
        model.setIsRentable(true);

        // Campos específicos de RentableAddressModel
        model.setTitle(dto.getTitle());
        model.setDescription(dto.getDescription());
        model.setComplement(dto.getComplement());
        model.setPricePerNight(dto.getPricePerNight());
        model.setMaxGuests(dto.getMaxGuests());
        model.setAvailableFrom(dto.getAvailableFrom());
        model.setAvailableTo(dto.getAvailableTo());

        // Tratamento de Listas para evitar NullPointerException e manter encapsulamento
        if (dto.getImageUrls() != null) {
            model.setImageUrls(new ArrayList<>(dto.getImageUrls()));
        }

        if (dto.getAmenities() != null) {
            model.setAmenities(new ArrayList<>(dto.getAmenities()));
        }

        return model;
    }

    @Override
    public RentableAddressResponseDTO toResponseDto(RentableAddressModel model) {
        if (model == null) return null;

        var builder = RentableAddressResponseDTO.builder();

        mapCommonFieldsToResponse(model, builder);

        // Mapeamento dos campos específicos no builder do Response
        builder.title(model.getTitle())
                .description(model.getDescription())
                .complement(model.getComplement())
                .pricePerNight(model.getPricePerNight())
                .maxGuests(model.getMaxGuests())
                .imageUrls(model.getImageUrls())
                .amenities(model.getAmenities())
                .availableFrom(model.getAvailableFrom())
                .availableTo(model.getAvailableTo());

        return builder.build();
    }

    @Override
    public void update(RentableAddressRequestDTO dto, RentableAddressModel model) {
        updateCommonFields(dto, model); // Atualiza base

        if (dto.getTitle() != null) model.setTitle(dto.getTitle());
        if (dto.getDescription() != null) model.setDescription(dto.getDescription());
        if (dto.getPricePerNight() != null) model.setPricePerNight(dto.getPricePerNight());
        if (dto.getMaxGuests() != null) model.setMaxGuests(dto.getMaxGuests());
        if (dto.getImageUrls() != null) model.setImageUrls(new ArrayList<>(dto.getImageUrls()));
        if (dto.getAmenities() != null) model.setAmenities(new ArrayList<>(dto.getAmenities()));
        if (dto.getAvailableFrom() != null) model.setAvailableFrom(dto.getAvailableFrom());
        if (dto.getAvailableTo() != null) model.setAvailableTo(dto.getAvailableTo());
    }
}
