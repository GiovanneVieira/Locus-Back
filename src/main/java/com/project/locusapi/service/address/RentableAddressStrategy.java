package com.project.locusapi.service.address;

import com.project.locusapi.constant.AddressType;
import com.project.locusapi.dto.address.AddressRequestDTO;
import com.project.locusapi.dto.address.AddressResponseDTO;
import com.project.locusapi.dto.address.rentable.RentableAddressRequestDTO;
import com.project.locusapi.exception.business.AddressNotFoundException;
import com.project.locusapi.exception.business.UserNotFoundException;
import com.project.locusapi.mapper.address.AddressMapper;
import com.project.locusapi.model.RentableAddressModel;
import com.project.locusapi.model.s3filemetadata.RentableAddressImage;
import com.project.locusapi.repository.RentableAddressRepository;
import com.project.locusapi.repository.UserRepository;
import com.project.locusapi.service.S3Service;
import com.project.locusapi.service.filestorage.RentableAddressImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RentableAddressStrategy implements AddressStrategy {

    private final RentableAddressRepository rentableAddressRepository;
    private final UserRepository userRepository;
    private final RentableAddressImageService imageService;
    private final AddressMapper addressMapper;
    private final S3Service s3Service;


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

        if (dto instanceof RentableAddressRequestDTO rentableDto) {

            List<UUID> idsParaManter = rentableDto.getImageIds();

            // 1. Identifica e deleta as imagens antigas do S3 e remove da lista (Remoção)
            List<RentableAddressImage> imagensDeletadas = address.getImages().stream()
                    .filter(img -> !idsParaManter.contains(img.getId()))
                    .toList();

            imagensDeletadas.forEach(img -> {
                try {
                    s3Service.deleteFile(img.getS3Key());
                } catch (Exception e) {
                    log.error("Falha ao deletar arquivo do S3: {}", img.getS3Key(), e);
                }
            });

            address.getImages().removeIf(img -> !idsParaManter.contains(img.getId()));

            // MAPEIA OS IDS QUE JÁ ESTÃO ASSOCIADOS ATUALMENTE
            List<UUID> idsAtuais = address.getImages().stream()
                    .map(RentableAddressImage::getId)
                    .toList();

            // ENCONTRA QUAIS IDS VIERAM DO FRONT MAS NÃO ESTÃO NA LISTA DO IMÓVEL (Novas Imagens)
            List<UUID> novosIds = idsParaManter.stream()
                    .filter(id -> !idsAtuais.contains(id))
                    .toList();

            if (!novosIds.isEmpty()) {
                //  BUSCA AS NOVAS IMAGENS (que o uploader criou soltas no banco)
                List<RentableAddressImage> novasImagens = imageService.getAllImagesByIds(novosIds);

                // VINCULA AS NOVAS IMAGENS AO ENDEREÇO (Crucial por causa do mappedBy)
                novasImagens.forEach(img -> {
                    img.setAddress(address); // Seta o FK address_id que estava null no banco
                    address.getImages().add(img); // Adiciona na lista do agregado rastreado pelo Hibernate
                });
            }
        }

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