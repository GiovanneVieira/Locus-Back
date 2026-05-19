package com.project.locusapi.service.strategy;

import com.project.locusapi.constant.AddressType;
import com.project.locusapi.dto.address.AddressRequestDTO;
import com.project.locusapi.dto.address.AddressResponseDTO;
import com.project.locusapi.exception.business.AddressNotFoundException;
import com.project.locusapi.exception.business.PersonalAddressAlreadyExistsException;
import com.project.locusapi.exception.business.UserNotFoundException;
import com.project.locusapi.mapper.address.AddressMapper;
import com.project.locusapi.model.PersonalAddressModel;
import com.project.locusapi.repository.PersonalAddressRepository;
import com.project.locusapi.repository.UserRepository;
import com.project.locusapi.service.address.AddressStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PersonalAddressStrategy implements AddressStrategy {

    private final PersonalAddressRepository personalAddressRepository;
    private final UserRepository userRepository;
    private final AddressMapper addressMapper;

    @Override
    public AddressType getSupportedType() {
        return AddressType.PERSONAL;
    }

    @Override
    @Transactional
    public AddressResponseDTO create(AddressRequestDTO dto, UUID userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (user.getPersonalAddress() != null) {
            throw new PersonalAddressAlreadyExistsException();
        }

        var address = addressMapper.toModel(dto, PersonalAddressModel.class);
        address.setUser(user);

        // Salvando o endereço, o JPA cuida do vínculo se mapeado com Cascade ou relacionamento correto
        personalAddressRepository.save(address);
        return addressMapper.toResponseDTO(address);
    }

    @Override
    @Transactional
    public AddressResponseDTO update(UUID addressId, UUID authenticatedUserId, AddressRequestDTO dto) {
        var address = personalAddressRepository.findById(addressId)
                .orElseThrow(() -> new AddressNotFoundException(addressId, "Endereço pessoal"));

        validateOwnership(address.getUser().getId(), authenticatedUserId);
        addressMapper.updateModel(dto, address);

        return addressMapper.toResponseDTO(personalAddressRepository.save(address));
    }

    @Override
    @Transactional
    public void delete(UUID addressId, UUID authenticatedUserId) {
        var address = personalAddressRepository.findById(addressId)
                .orElseThrow(() -> new AddressNotFoundException(addressId, "Endereço pessoal"));

        validateOwnership(address.getUser().getId(), authenticatedUserId);
        personalAddressRepository.delete(address);
    }

    private void validateOwnership(UUID ownerId, UUID authenticatedUserId) {
        if (!ownerId.equals(authenticatedUserId)) {
            throw new AccessDeniedException("Você não tem permissão para alterar este endereço pessoal.");
        }
    }
}