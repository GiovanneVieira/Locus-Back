package com.project.locusapi.service.address;

import com.project.locusapi.constant.AddressType;
import com.project.locusapi.dto.address.AddressRequestDTO;
import com.project.locusapi.dto.address.AddressResponseDTO;
import com.project.locusapi.dto.address.rentable.RentableAddressRequestDTO;
import com.project.locusapi.exception.business.AddressNotFoundException;
import com.project.locusapi.exception.business.UserNotFoundException;
import com.project.locusapi.mapper.address.AddressMapper;
import com.project.locusapi.model.RentableAddressModel;
import com.project.locusapi.repository.RentableAddressRepository;
import com.project.locusapi.repository.UserRepository;
import com.project.locusapi.service.filestorage.RentableAddressImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RentableAddressStrategy implements AddressStrategy {

    private final RentableAddressRepository rentableAddressRepository;
    private final UserRepository userRepository;
    private final RentableAddressImageService imageService;
    private final AddressMapper addressMapper;

    @Override
    public AddressType getSupportedType() {
        return AddressType.RENTABLE;
    }

    @Override
    @Transactional
    public AddressResponseDTO create(AddressRequestDTO dto, UUID userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        var address = addressMapper.toModel(dto, RentableAddressModel.class);
        address.setUser(user);
        var savedAddress = rentableAddressRepository.save(address);

        // Se for o DTO especializado de locação, vincula as imagens órfãs da Fase 1
        if (dto instanceof RentableAddressRequestDTO rentableDto) {
            imageService.bindImagesToAddress(rentableDto.getImageIds(), savedAddress, rentableDto.getMainImageId());
        }

        return addressMapper.toResponseDTO(savedAddress);
    }

    @Override
    @Transactional
    public AddressResponseDTO update(UUID addressId, UUID authenticatedUserId, AddressRequestDTO dto) {
        var address = rentableAddressRepository.findById(addressId)
                .orElseThrow(() -> new AddressNotFoundException(addressId, "Imóvel locável"));

        validateOwnership(address.getUser().getId(), authenticatedUserId);
        addressMapper.updateModel(dto, address);

        return addressMapper.toResponseDTO(rentableAddressRepository.save(address));
    }

    @Override
    @Transactional
    public void delete(UUID addressId, UUID authenticatedUserId) {
        var address = rentableAddressRepository.findById(addressId)
                .orElseThrow(() -> new AddressNotFoundException(addressId, "Imóvel locável"));

        validateOwnership(address.getUser().getId(), authenticatedUserId);
        rentableAddressRepository.delete(address);
    }

    private void validateOwnership(UUID ownerId, UUID authenticatedUserId) {
        if (!ownerId.equals(authenticatedUserId)) {
            throw new AccessDeniedException("Ação não permitida para este usuário sobre este imóvel.");
        }
    }
}