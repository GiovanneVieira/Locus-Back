package com.project.locusapi.service.filestorage;

import com.project.locusapi.dto.s3.ImageDetailsResponse;
import com.project.locusapi.exception.file.FileStorageException;
import com.project.locusapi.exception.file.StorageFileNotFoundException;
import com.project.locusapi.model.RentableAddressModel;
import com.project.locusapi.model.s3filemetadata.RentableAddressImage;
import com.project.locusapi.repository.s3file.RentableAddressImageRepository;
import com.project.locusapi.service.S3Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class RentableAddressImageService {

    private final S3Service s3Service;
    private final RentableAddressImageRepository imageRepository;

    public RentableAddressImageService(S3Service s3Service, RentableAddressImageRepository imageRepository) {
        this.s3Service = s3Service;
        this.imageRepository = imageRepository;
    }

    /**
     * FASE 1: Upload independente de imagens (Fluxo controlado pelo Front-end).
     * Salva os arquivos como metadados "órfãos" no banco para posterior vinculação.
     */
    @Transactional
    public List<ImageDetailsResponse> uploadIndependentImages(List<MultipartFile> files, UUID hostId) {
        List<ImageDetailsResponse> uploadedImages = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                // Upload físico para o S3
                String s3Key = s3Service.uploadFile(file);

                // Persistência inicial sem Address (null)
                RentableAddressImage imageMetadata = RentableAddressImage.builder()
                        .originalName(file.getOriginalFilename())
                        .s3Key(s3Key)
                        .contentType(file.getContentType())
                        .fileSize(file.getSize())
                        .hostId(hostId)
                        .isMain(false) // Será definido na Fase 2
                        .build();

                RentableAddressImage saved = imageRepository.save(imageMetadata);
                uploadedImages.add(new ImageDetailsResponse(saved.getId(), saved.getOriginalName(), saved.getS3Key()));

            } catch (IOException e) {
                // Envelopa o erro de IO na nossa exceção de infraestrutura tratada no Advice
                throw new FileStorageException("Falha ao processar o upload do arquivo: " + file.getOriginalFilename());
            }
        }

        return uploadedImages;
    }

    /**
     * FASE 2: Método conveniente para vincular as imagens previamente salvas ao endereço definitivo.
     */
    @Transactional
    public void bindImagesToAddress(List<UUID> imageIds, RentableAddressModel address, UUID mainImageId) {
        if (imageIds == null || imageIds.isEmpty()) return;

        List<RentableAddressImage> images = imageRepository.findAllById(imageIds);

        for (RentableAddressImage image : images) {
            image.setAddress(address);
            // Define a imagem principal com base na escolha explícita do usuário no front
            image.setMain(image.getId().equals(mainImageId));

            address.getImages().add(image);
        }

        imageRepository.saveAll(images);
    }

    /**
     * Método Legado / Alternativo: Caso precise salvar uma imagem já vinculada diretamente.
     */
    @Transactional
    public RentableAddressImage storeImage(MultipartFile file, RentableAddressModel address, UUID hostId, boolean isMain) throws IOException {
        if (isMain && address.getId() != null) {
            imageRepository.disableMainImageForAddress(address.getId());
        }

        String s3Key = s3Service.uploadFile(file);

        RentableAddressImage imageMetadata = RentableAddressImage.builder()
                .originalName(file.getOriginalFilename())
                .s3Key(s3Key)
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .address(address)
                .hostId(hostId)
                .isMain(isMain)
                .build();

        return imageRepository.save(imageMetadata);
    }

    /**
     * Busca o InputStream do arquivo no S3. Lança erro limpo 404 se o ID não existir.
     */
    @Transactional(readOnly = true)
    public InputStream getImageContent(UUID imageId) {
        RentableAddressImage metadata = imageRepository.findById(imageId)
                .orElseThrow(() -> new StorageFileNotFoundException("Imagem não encontrada no registro local para o ID: " + imageId));

        return s3Service.downloadFileStream(metadata.getS3Key());
    }

    /**
     * Remove a imagem de forma limpa do banco de dados e do S3.
     */
    @Transactional
    public void deleteImage(UUID imageId) {
        RentableAddressImage metadata = imageRepository.findById(imageId)
                .orElseThrow(() -> new StorageFileNotFoundException("Imagem não encontrada para exclusão com o ID: " + imageId));

        // Deleta do S3 primeiro
        s3Service.deleteFile(metadata.getS3Key());

        // Limpa do banco local
        imageRepository.delete(metadata);
    }

    // ==========================================
    // MÉTODOS CONVENIENTES DE BUSCA ESPECÍFICA
    // ==========================================

    @Transactional
    public List<RentableAddressImage> getAllImagesByIds(List<UUID> imageIds) {
        return imageRepository.findAllById(imageIds);
    }

    @Transactional(readOnly = true)
    public List<RentableAddressImage> getImagesByAddress(UUID addressId) {
        return imageRepository.findByAddressId(addressId);
    }

    @Transactional(readOnly = true)
    public RentableAddressImage getMainImageByAddress(UUID addressId) {
        return imageRepository.findByAddressIdAndIsMainTrue(addressId)
                .orElseThrow(() -> new StorageFileNotFoundException("Nenhuma imagem principal definida para o endereço: " + addressId));
    }

    @Transactional(readOnly = true)
    public Page<RentableAddressImage> searchImagesByNameAndAddress(UUID addressId, String originalName, Pageable pageable) {
        return imageRepository.findByAddressIdAndOriginalNameContainingIgnoreCase(addressId, originalName, pageable);
    }

    @Transactional(readOnly = true)
    public List<RentableAddressImage> getImagesByHost(UUID hostId) {
        return imageRepository.findByHostId(hostId);
    }
}