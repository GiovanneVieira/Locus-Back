package com.project.locusapi.service;

import com.project.locusapi.dto.address.AddressRequestDTO;
import com.project.locusapi.dto.address.AddressResponseDTO;
import com.project.locusapi.mapper.address.AddressMapper;
import com.project.locusapi.model.PersonalAddressModel;
import com.project.locusapi.model.RentableAddressModel;
import com.project.locusapi.repository.PersonalAddressRepository;
import com.project.locusapi.repository.RentableAddressRepository;
import com.project.locusapi.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        var user = userRepository.findById(userId).orElseThrow(EntityNotFoundException::new);
        if (user.getPersonalAddress() != null) {
            throw new IllegalStateException("User already has a personal address");
        }

        var address = addressMapper.toModel(dto, PersonalAddressModel.class);
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


    @Transactional(readOnly = true)
    public Page<AddressResponseDTO> getAllRentableAddresses(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        var addresses = rentableAddressRepository.findAll(pageable).stream().map(addressMapper::toResponseDTO).toList();
        return new PageImpl<>(addresses, pageable, addresses.size());
    }

    public void deleteRentableAddress(UUID addressId, UUID authenticatedUserId) {
        var address = rentableAddressRepository.findById(addressId)
                .orElseThrow(() -> new EntityNotFoundException("Endereço não encontrado"));

        // Segurança: Impede que um usuário delete o imóvel de outro
        if (!address.getUser().getId().equals(authenticatedUserId)) {
            throw new AccessDeniedException("Você não tem permissão para deletar este imóvel");
        }

        rentableAddressRepository.delete(address);
    }

    public void deletePersonalAddress(UUID addressId, UUID authenticatedUserId) {
        var address = personalAddressRepository.findById(addressId)
                .orElseThrow(() -> new EntityNotFoundException("Endereço não encontrado"));

        // Segurança: Impede que um usuário delete o imóvel de outro
        if (!address.getUser().getId().equals(authenticatedUserId)) {
            throw new AccessDeniedException("Você não tem permissão para deletar este imóvel");
        }

        personalAddressRepository.delete(address);
    }


    public AddressResponseDTO updatePersonalAddress(UUID addressId, UUID authenticatedUserId, AddressRequestDTO dto) throws AccessDeniedException {
        var address = personalAddressRepository.findById(addressId)
                .orElseThrow(() -> new EntityNotFoundException("Endereço não encontrado"));

        if (!address.getUser().getId().equals(authenticatedUserId)) {
            throw new AccessDeniedException("Você não tem permissão para alterar este endereço");
        }

        addressMapper.updateModel(dto, address);

        return addressMapper.toResponseDTO(personalAddressRepository.save(address));
    }

    public AddressResponseDTO updateRentableAddress(UUID addressId, UUID authenticatedUserId, AddressRequestDTO dto) throws AccessDeniedException {
        var address = rentableAddressRepository.findById(addressId)
                .orElseThrow(() -> new EntityNotFoundException("Imóvel não encontrado"));

        if (!address.getUser().getId().equals(authenticatedUserId)) {
            throw new AccessDeniedException("Ação não permitida para este usuário");
        }

        addressMapper.updateModel(dto, address);

        return addressMapper.toResponseDTO(rentableAddressRepository.save(address));
    }

}
