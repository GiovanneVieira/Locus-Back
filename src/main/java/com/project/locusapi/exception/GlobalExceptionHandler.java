package com.project.locusapi.exception;

import com.auth0.jwt.exceptions.*;
import com.project.locusapi.dto.error.StandardErrorDTO;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<StandardErrorDTO> handleEntityNotFound(EntityNotFoundException ex, HttpServletRequest request) {
        StandardErrorDTO errorDto = new StandardErrorDTO(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                "Not found",
                ex.getMessage(),
                request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDto);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<StandardErrorDTO> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        // Extrai o nome do parâmetro e o tipo que ele deveria ser
        String name = ex.getName();
        String type = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "tipo desconhecido";

        // Cria uma mensagem amigável para o seu Toast no React
        String message = String.format("O parâmetro '%s' deve ser do tipo %s", name, type);

        StandardErrorDTO errorDto = new StandardErrorDTO(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Parâmetro Inválido",
                message,
                request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDto);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<StandardErrorDTO> handleInvalidCredentials(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .getFirst()
                .getDefaultMessage();
        StandardErrorDTO errorDto = new StandardErrorDTO(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Invalid Request",
                errorMessage,
                request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDto);
    }

    @ExceptionHandler(JWTCreationException.class)
    public ResponseEntity<StandardErrorDTO> handleJwtCreation(JWTCreationException ex, HttpServletRequest request) {
        StandardErrorDTO errorDto = new StandardErrorDTO(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Error creating JWT",
                ex.getMessage(),
                request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDto);
    }

    @ExceptionHandler(JWTDecodeException.class)
    public ResponseEntity<StandardErrorDTO> handleJwtDecoding(JWTDecodeException ex, HttpServletRequest request) {
        StandardErrorDTO errorDto = new StandardErrorDTO(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Error decoding JWT",
                ex.getMessage(),
                request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDto);
    }

    @ExceptionHandler(JWTVerificationException.class)
    public ResponseEntity<StandardErrorDTO> handleJwtVerification(JWTVerificationException ex, HttpServletRequest request) {
        StandardErrorDTO errorDto = new StandardErrorDTO(
                LocalDateTime.now(),
                HttpStatus.UNAUTHORIZED.value(),
                "Error verifying JWT",
                ex.getMessage(),
                request.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorDto);
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<StandardErrorDTO> handleJwtExpiration(TokenExpiredException ex, HttpServletRequest request) {
        StandardErrorDTO errorDto = new StandardErrorDTO(
                LocalDateTime.now(),
                HttpStatus.UNAUTHORIZED.value(),
                "Token expired",
                ex.getMessage(),
                request.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorDto);
    }

    @ExceptionHandler(SignatureVerificationException.class)
    public ResponseEntity<StandardErrorDTO> handleJwtSignature(SignatureVerificationException ex, HttpServletRequest request) {
        StandardErrorDTO errorDto = new StandardErrorDTO(
                LocalDateTime.now(),
                HttpStatus.UNAUTHORIZED.value(),
                "Invalid JWT signature",
                ex.getMessage(),
                request.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorDto);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<StandardErrorDTO> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        String message = "Recurso já cadastrado";

        // Opcional: Verificar se a mensagem do banco contém "email" para ser mais específico
        if (ex.getMostSpecificCause().getMessage().contains("email")) {
            message = "Este e-mail já está em uso.";
        }

        StandardErrorDTO errorDto = new StandardErrorDTO(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(), // 409 é o status correto para duplicidade
                "Conflito de Dados",
                message,
                request.getRequestURI());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorDto);
    }
}