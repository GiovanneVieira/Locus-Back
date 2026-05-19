package com.project.locusapi.exception.file;

import com.project.locusapi.exception.business.BusinessException;
import org.springframework.http.HttpStatus;

public class FileStorageException extends BusinessException {
    public FileStorageException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}