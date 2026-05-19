package com.project.locusapi.exception.file;

import com.project.locusapi.exception.business.BusinessException;
import org.springframework.http.HttpStatus;

public class StorageFileNotFoundException extends BusinessException {
    public StorageFileNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}