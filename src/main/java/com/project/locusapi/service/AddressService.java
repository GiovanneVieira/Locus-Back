package com.project.locusapi.service;

import com.project.locusapi.dto.address.AddressRequestDTO;
import com.project.locusapi.dto.address.AddressResponseDTO;
import com.project.locusapi.mapper.AddressMapper;
import com.project.locusapi.model.Address;
import com.project.locusapi.model.PersonalAddressModel;
import com.project.locusapi.model.RentableAddressModel;
import com.project.locusapi.repository.PersonalAddressRepository;
import com.project.locusapi.repository.RentableAddressRepository;
import com.project.locusapi.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final PersonalAddressRepository personalAddressRepository;
    private final RentableAddressRepository rentableAddressRepository;
    private final UserRepository userRepository;
    private final AddressMapper addressMapper;

    public AddressResponseDTO createPersonalAddress(AddressRequestDTO dto, UUID userId) {
        var address = addressMapper.toModel(dto, PersonalAddressModel.class);
        var user = userRepository.findById(userId).orElseThrow(EntityNotFoundException::new);

        address.setUser(user);
        personalAddressRepository.save(address);

        user.setPersonalAddress(address);
        userRepository.save(user);
        return addressMapper.toResponseDTO(address);
    }

    public AddressResponseDTO createRentableAddress(AddressRequestDTO dto, UUID userId) {
        var address = addressMapper.toModel(dto, RentableAddressModel.class);
        var user = userRepository.findById(userId).orElseThrow(EntityNotFoundException::new);

        address.setUser(user);
        rentableAddressRepository.save(address);

        user.addRentableAddress(address);
        userRepository.save(user);
        return addressMapper.toResponseDTO(address);
    }


    public AddressResponseDTO getPersonalAddressByUserId(UUID id) {
        var address = personalAddressRepository.findByUserId(id).orElseThrow(() -> new EntityNotFoundException("Personal address from user with id " + id + "not found"));
        return addressMapper.toResponseDTO(address);
    }

    public List<AddressResponseDTO> getRentableAddressesByUserId(UUID id) {
        var addresses = rentableAddressRepository.findAllByUserId(id);
        return addresses.stream().map(addressMapper::toResponseDTO).toList();
    }

    public List<AddressResponseDTO> getAllRentableAddresses() {
        return rentableAddressRepository.findAll().stream().map(addressMapper::toResponseDTO).toList();
    }

    public AddressResponseDTO deleteRentableAddress(UUID addressId, UUID authenticatedUserId) {
        var address = rentableAddressRepository.findById(addressId)
                .orElseThrow(() -> new EntityNotFoundException("Endereço não encontrado"));

        // Segurança: Impede que um usuário delete o imóvel de outro
        if (!address.getUser().getId().equals(authenticatedUserId)) {
            throw new AccessDeniedException("Você não tem permissão para deletar este imóvel");
        }

        rentableAddressRepository.delete(address);
        return addressMapper.toResponseDTO(address);
    }

    public AddressResponseDTO deletePersonalAddress(UUID addressId, UUID authenticatedUserId) {
        var address = personalAddressRepository.findById(addressId)
                .orElseThrow(() -> new EntityNotFoundException("Endereço não encontrado"));

        // Segurança: Impede que um usuário delete o imóvel de outro
        if (!address.getUser().getId().equals(authenticatedUserId)) {
            throw new AccessDeniedException("Você não tem permissão para deletar este imóvel");
        }

        personalAddressRepository.delete(address);
        return addressMapper.toResponseDTO(address);
    }


    public AddressResponseDTO updatePersonalAddress(UUID addressId, UUID authenticatedUserId, AddressRequestDTO dto) throws AccessDeniedException {
        var address = personalAddressRepository.findById(addressId)
                .orElseThrow(() -> new EntityNotFoundException("Endereço não encontrado"));

        if (!address.getUser().getId().equals(authenticatedUserId)) {
            throw new AccessDeniedException("Você não tem permissão para alterar este endereço");
        }

        safeUpdateAddress(address, dto);
        return addressMapper.toResponseDTO(personalAddressRepository.save(address));
    }

    public AddressResponseDTO updateRentableAddress(UUID addressId, UUID authenticatedUserId, AddressRequestDTO dto) throws AccessDeniedException {
        var address = rentableAddressRepository.findById(addressId)
                .orElseThrow(() -> new EntityNotFoundException("Imóvel não encontrado"));

        if (!address.getUser().getId().equals(authenticatedUserId)) {
            throw new AccessDeniedException("Ação não permitida para este usuário");
        }

        // 3. Sua lógica segura de merge
        safeUpdateAddress(address, dto);

        return addressMapper.toResponseDTO(rentableAddressRepository.save(address));
    }


    private <T extends Address> void safeUpdateAddress(T address, AddressRequestDTO dto) {
        if (dto.city() != null) {
            address.setCity(dto.city());
        }
        if (dto.country() != null) {
            address.setCountry(dto.country());
        }
        if (dto.cep() != null) {
            address.setCep(dto.cep());
        }
        if (dto.street() != null) {
            address.setStreet(dto.street());
        }
        if (dto.houseNumber() != null) {
            address.setHouseNumber(dto.houseNumber());
        }
        if (dto.state() != null) {
            address.setState(dto.state());
        }
    }
}
