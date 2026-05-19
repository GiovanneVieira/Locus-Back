package com.project.locusapi.repository.s3file;

import com.project.locusapi.model.s3filemetadata.AbstractFileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;
import java.util.UUID;

@NoRepositoryBean // Diz ao Spring para NÃO tentar instanciar este repositório sozinho
public interface BaseFileRepository<T extends AbstractFileMetadata> extends JpaRepository<T, UUID> {

    // Qualquer repositório que herdar deste já ganha essa busca de graça
    Optional<T> findByS3Key(String s3Key);
}