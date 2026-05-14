package com.project.locusapi.dto.s3;

import java.util.List;

public record S3ResponseDTO(String message, List<String> images) {
}
