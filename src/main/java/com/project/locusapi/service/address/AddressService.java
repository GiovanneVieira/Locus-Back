package com.project.locusapi.service.address;

import com.project.locusapi.constant.AddressType;
import com.project.locusapi.constant.Role;
import com.project.locusapi.dto.address.AddressRequestDTO;
import com.project.locusapi.dto.address.AddressResponseDTO;
import com.project.locusapi.exception.business.AddressNotFoundException;
import com.project.locusapi.mapper.address.AddressMapper;
import com.project.locusapi.repository.PersonalAddressRepository;
import com.project.locusapi.repository.RentableAddressRepository;
import com.project.locusapi.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AddressService {

    private final PersonalAddressRepository personalAddressRepository;
    private final RentableAddressRepository rentableAddressRepository;
    private final UserService userService;
    private final AddressMapper addressMapper;
    private final Map<AddressType, AddressStrategy> strategyMap;

    public AddressService(PersonalAddressRepository personalAddressRepository,
                          RentableAddressRepository rentableAddressRepository,
                          UserService userService,
                          AddressMapper addressMapper,
                          List<AddressStrategy> strategies) {
        this.personalAddressRepository = personalAddressRepository;
        this.rentableAddressRepository = rentableAddressRepository;
        this.userService = userService;
        this.addressMapper = addressMapper;
        // Transforma a lista de componentes injetados em um mapa chaveado pelo Enum
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(AddressStrategy::getSupportedType, Function.identity()));
    }

    // =========================================================================
    // OPERAÇÕES ROTEADAS VIA STRATEGY PATTERN (MUTATIONS)
    // =========================================================================

    public AddressResponseDTO createAddress(AddressType type, AddressRequestDTO dto, UUID userId) {
        return getStrategy(type).create(dto, userId);
    }

    public AddressResponseDTO updateAddress(AddressType type, UUID addressId, UUID authenticatedUserId, AddressRequestDTO dto) {
        return getStrategy(type).update(addressId, authenticatedUserId, dto);
    }

    public void deleteAddress(AddressType type, UUID addressId, UUID authenticatedUserId) {
        getStrategy(type).delete(addressId, authenticatedUserId);
    }

    // =========================================================================
    // QUERIES E CONSULTAS (READ-ONLY)
    // =========================================================================

    @Transactional(readOnly = true)
    public AddressResponseDTO getRentableAddressById(UUID addressId) {
        return rentableAddressRepository.findById(addressId)
                .map(addressMapper::toResponseDTO)
                .orElseThrow(() -> new AddressNotFoundException(addressId, "Imóvel locável"));
    }

    @Transactional(readOnly = true)
    public AddressResponseDTO getPersonalAddressByUserId(UUID id) {
        return personalAddressRepository.findByUserId(id)
                .map(addressMapper::toResponseDTO)
                .orElseThrow(() -> new AddressNotFoundException("Endereço pessoal do usuário não encontrado."));
    }

    @Transactional(readOnly = true)
    public List<AddressResponseDTO> getOwnedRentableAddresses(Authentication authentication) {
        var user = this.userService.getAuthenticatedUser(authentication);
        if (user.getRole().equals(Role.USER)) {
            throw new AccessDeniedException("Permissões insuficientes para realizar esta operação.");
        }
        return rentableAddressRepository.findAllByUserId(user.getId())
                .stream()
                .map(addressMapper::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<AddressResponseDTO> getAllRentableAddresses(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        // CORREÇÃO DE BUG: O próprio objeto Page do Spring sabe mapear seu conteúdo interno
        // mantendo os contadores e metadados de paginação originais intactos para o front-end
        return rentableAddressRepository.findAll(pageable).map(addressMapper::toResponseDTO);
    }

    // =========================================================================
    // AUXILIARES
    // =========================================================================

    private AddressStrategy getStrategy(AddressType type) {
        AddressStrategy strategy = strategyMap.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("Estratégia de endereço não implementada para o tipo: " + type);
        }
        return strategy;
    }
}