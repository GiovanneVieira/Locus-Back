package com.project.locusapi.service;

import com.project.locusapi.exception.file.FileStorageException;
import com.project.locusapi.exception.file.StorageFileNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class S3Service {

    private final S3Client s3Client;

    @Value("${cloud.aws.bucket.name}")
    private String bucketName;

    public S3Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    /**
     * Realiza o upload físico do arquivo para o bucket do S3 utilizando streams.
     */
    public String uploadFile(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
        String s3Key = UUID.randomUUID().toString() + extension;

        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(s3Key)
                            .contentType(file.getContentType())
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );
            return s3Key;
        } catch (S3Exception e) {
            throw new FileStorageException("Erro ao fazer upload para o S3: " + e.awsErrorDetails().errorMessage());
        }
    }

    /**
     * Retorna o InputStream direto do S3 para evitar estouro de memória RAM (Heap).
     */
    public InputStream downloadFileStream(String key) {
        try {
            return s3Client.getObject(GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build());
        } catch (NoSuchKeyException e) {
            throw new StorageFileNotFoundException("Arquivo não encontrado no S3: " + key);
        } catch (S3Exception e) {
            throw new FileStorageException("Erro ao baixar arquivo do S3: " + e.awsErrorDetails().errorMessage());
        }
    }

    /**
     * Remove fisicamente o objeto de dentro do Bucket do S3.
     */
    public void deleteFile(String key) {
        try {
            s3Client.deleteObject(b -> b.bucket(bucketName).key(key));
        } catch (S3Exception e) {
            throw new FileStorageException("Erro ao deletar arquivo do S3: " + e.awsErrorDetails().errorMessage());
        }
    }

    /**
     * Gera a URL externa estática do arquivo se ele existir no S3.
     */
    public String getUrlFromFileName(String fileName) {
        if (!doesFileExist(fileName)) {
            throw new StorageFileNotFoundException("Arquivo não encontrado no S3: " + fileName);
        }

        return s3Client.utilities()
                .getUrl(b -> b.bucket(bucketName).key(fileName))
                .toExternalForm();
    }

    /**
     * Agrupa múltiplos arquivos em um único pacote ZIP consumindo os streams sob demanda.
     */
    public byte[] downloadFilesAsZip(List<String> fileNames) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (String fileName : fileNames) {
                if (doesFileExist(fileName)) {
                    try (InputStream is = downloadFileStream(fileName)) {
                        ZipEntry entry = new ZipEntry(fileName);
                        zos.putNextEntry(entry);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            zos.write(buffer, 0, bytesRead);
                        }
                        zos.closeEntry();
                    }
                }
            }
        }
        return baos.toByteArray();
    }

    /**
     * Verifica a existência de um objeto no bucket usando metadados leve (HEAD request).
     */
    private boolean doesFileExist(String fileName) {
        try {
            s3Client.headObject(b -> b.bucket(bucketName).key(fileName));
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            throw new FileStorageException("Erro ao verificar existência do arquivo no S3: " + e.awsErrorDetails().errorMessage());
        }
    }
}