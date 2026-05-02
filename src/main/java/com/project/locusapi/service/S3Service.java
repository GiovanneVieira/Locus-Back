package com.project.locusapi.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
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

    public void uploadFile(MultipartFile file) throws IOException {
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(file.getOriginalFilename()).
                        contentType(file.getContentType()).
                        build(), RequestBody.fromBytes(file.getBytes()));
    }

    public byte[] downloadFile(String key) {
        ResponseBytes<GetObjectResponse> objectAsByte = s3Client.getObjectAsBytes(GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build());
        return objectAsByte.asByteArray();
    }

    public String getUrlFromFileName(String fileName) {
        if (!doesFileExist(fileName)) {
            throw new RuntimeException("Arquivo não encontrado no S3: " + fileName);
        }

        return s3Client.utilities()
                .getUrl(b -> b.bucket(bucketName).key(fileName))
                .toExternalForm();
    }

    public byte[] downloadFilesAsZip(List<String> fileNames) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (String fileName : fileNames) {
                byte[] fileContent = downloadFile(fileName);
                ZipEntry entry = new ZipEntry(fileName);
                entry.setSize(fileContent.length);
                zos.putNextEntry(entry);
                zos.write(fileContent);
                zos.closeEntry();
            }
        }
        return baos.toByteArray();
    }
    private boolean doesFileExist(String fileName) {
        try {
            s3Client.headObject(b -> b.bucket(bucketName).key(fileName));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
