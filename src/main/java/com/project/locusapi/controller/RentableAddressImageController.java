package com.project.locusapi.controller;

import com.project.locusapi.dto.s3.ImageDetailsResponse;
import com.project.locusapi.service.UserService;
import com.project.locusapi.service.filestorage.RentableAddressImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/s3/rentable-address/image")
@RequiredArgsConstructor
public class RentableAddressImageController {

    private final RentableAddressImageService imageService;
    private final UserService userService;

    /**
     * FASE 1: Endpoint de Upload Independente.
     * O front-end envia as fotos aqui assim que o usuário as seleciona.
     * Retorna a lista de IDs (UUIDs) que o front usará na Fase 2.
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<ImageDetailsResponse>> uploadMultipleImages(
            @RequestParam("files") List<MultipartFile> files,
            Authentication authentication) {

        var user = userService.getAuthenticatedUser(authentication);

        List<ImageDetailsResponse> response = imageService.uploadIndependentImages(files, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Endpoint de Download/Exibição focado em padrão de mercado.
     * Em vez de passar o nome do arquivo na URL (inseguro), passamos o UUID do metadado.
     */
    @GetMapping("/{imageId}/content")
    public ResponseEntity<InputStreamResource> getImageContent(@PathVariable UUID imageId) {
        // Busca o stream diretamente do S3 via Service
        InputStream imageStream = imageService.getImageContent(imageId);

        // Em produção, você pode buscar o originalName e contentType do banco se preferir,
        // mas o InputStreamResource resolve o streaming sem estourar a RAM
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG) // Ou dinâmico baseado no metadado
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline") // "inline" faz abrir direto no navegador/app
                .body(new InputStreamResource(imageStream));
    }

    /**
     * Endpoint para remoção de imagem (caso o usuário clique no "X" da foto antes de salvar o form)
     */
    @DeleteMapping("/{imageId}")
    public ResponseEntity<Void> deleteImage(@PathVariable UUID imageId) {
        imageService.deleteImage(imageId);
        return ResponseEntity.noContent().build();
    }
}