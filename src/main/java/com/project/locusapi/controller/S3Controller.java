package com.project.locusapi.controller;

import com.project.locusapi.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLConnection;
import java.util.List;

@RestController
@RequestMapping("/s3")
@RequiredArgsConstructor
public class S3Controller {

    private final S3Service s3Service;

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) throws IOException {
        s3Service.uploadFile(file);
        return ResponseEntity.ok("File uploaded successfully");
    }

    @PostMapping("/upload/multiple")
    public ResponseEntity<?> uploadMultiple(@RequestParam("files") MultipartFile[] files) throws IOException {
        for (MultipartFile file : files) {
            s3Service.uploadFile(file);
        }
        return ResponseEntity.ok("Files uploaded successfully");
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> download(@RequestParam("fileName") String fileName) throws IOException {
        byte[] data = s3Service.downloadFile(fileName);

        // Tenta determinar o tipo de mídia (ou use um padrão)
        String contentType = URLConnection.guessContentTypeFromName(fileName);
        if (contentType == null) contentType = "application/octet-stream";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(data);
    }

    @GetMapping("/download/multiple")
    public ResponseEntity<byte[]> downloadMultiple(@RequestParam("fileNames") List<String> fileNames) throws IOException {
        byte[] zipData = s3Service.downloadFilesAsZip(fileNames);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/zip"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"locus_files.zip\"")
                .body(zipData);
    }

    @GetMapping("/get/file-url")
    public ResponseEntity<?> url(@RequestParam("fileName") String fileName) {
        try {
            String fileUrl = s3Service.getUrlFromFileName(fileName);
            return ResponseEntity.ok(java.util.Map.of("url", fileUrl));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }
}
