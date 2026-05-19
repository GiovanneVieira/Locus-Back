package com.project.locusapi.controller;

import com.project.locusapi.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
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
        List<String> fileKeys = new ArrayList<>();

        for (MultipartFile file : files) {
            // Reparo: Devolvemos apenas a Key exata gerada pelo S3
            String s3Key = s3Service.uploadFile(file);
            fileKeys.add(s3Key);
        }

        return ResponseEntity.status(HttpStatus.OK).body(fileKeys);
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