package com.project.locusapi.service;

import com.project.locusapi.dto.address.AddressRequestDTO;
import com.project.locusapi.dto.address.AddressResponseDTO;
import com.project.locusapi.mapper.AddressMapper;
import com.project.locusapi.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final AddressMapper addressMapper = new AddressMapper();

    public AddressResponseDTO createAddress(AddressRequestDTO dto) {
        var address = addressMapper.toModel(dto);
        addressRepository.save(address);
        return addressMapper.toResponseDTO(address);
    }

    public List<AddressResponseDTO> getAllAddress() {
        return addressRepository.findAll().stream().map(address -> new AddressResponseDTO(address.getCity(), address.getStreet())).toList();
    }
    
}
