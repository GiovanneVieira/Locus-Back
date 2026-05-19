package com.project.locusapi.exception;

import com.auth0.jwt.exceptions.*;
import com.project.locusapi.dto.error.StandardErrorDTO;
import com.project.locusapi.exception.business.BusinessException;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // =========================================================================
    // 1. REGRAS DE NEGÓCIO E EXCEÇÕES CUSTOMIZADAS
    // =========================================================================

    /**
     * Captura todas as exceções customizadas do sistema que herdam de BusinessException.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<StandardErrorDTO> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        StandardErrorDTO errorDto = new StandardErrorDTO(
                LocalDateTime.now(),
                ex.getStatus().value(),
                ex.getStatus().getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(ex.getStatus()).body(errorDto);
    }

    // =========================================================================
    // 2. VALIDAÇÕES DE ENTRADA, DTOs E SINTAXE DE REQUEST (HTTP / JSON)
    // =========================================================================

    /**
     * Captura validações do Beans Validation (@Valid / @NotNull / @Size etc) nos DTOs.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<StandardErrorDTO> handleInvalidCredentials(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .getFirst() // Excelente uso do Java 21!
                .getDefaultMessage();

        StandardErrorDTO errorDto = new StandardErrorDTO(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Invalid Request",
                errorMessage,
                request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDto);
    }

    /**
     * Captura payloads JSON malformados ou incompatibilidade de tipos de dados no corpo da requisição.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<StandardErrorDTO> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        String message = "O corpo da requisição (payload) está malformado ou contém valores com tipos inválidos.";

        if (ex.getMostSpecificCause() != null) {
            String rawMessage = ex.getMostSpecificCause().getMessage();
            if (rawMessage.contains("Cannot deserialize value of type")) {
                message = "Incompatibilidade de tipos detectada no payload. Verifique se enviou textos em campos numéricos ou de data.";
            }
        }

        StandardErrorDTO errorDto = new StandardErrorDTO(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Corpo da Requisição Inválido",
                message,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDto);
    }
    /**
     * Captura falhas de serialização de saída (Java para JSON).
     * Evita estouro de StackOverflow ou vazamento de estruturas circulares/recursivas (infinite loops).
     */
    @ExceptionHandler(HttpMessageNotWritableException.class)
    public ResponseEntity<StandardErrorDTO> handleHttpMessageNotWritable(HttpMessageNotWritableException ex, HttpServletRequest request) {
        String message = "Ocorreu um erro interno ao processar e estruturar a resposta do servidor.";

        // Captura o cenário específico de recursão infinita (Nesting Depth) de forma elegante
        if (ex.getMessage() != null && ex.getMessage().contains("Document nesting depth")) {
            message = "Erro de serialização detectado: Estrutura de dados circular ou cíclica no mapeamento do recurso.";
        }

        StandardErrorDTO errorDto = new StandardErrorDTO(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error (Serialization Failure)",
                message,
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDto);
    }



    /**
     * Captura incompatibilidade de tipos passados diretamente nos parâmetros da URL ou PathVariables.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<StandardErrorDTO> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String name = ex.getName();
        String type = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "tipo desconhecido";
        String message = String.format("O parâmetro '%s' deve ser do tipo %s", name, type);

        StandardErrorDTO errorDto = new StandardErrorDTO(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Parâmetro Inválido",
                message,
                request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDto);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<StandardErrorDTO> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        StandardErrorDTO errorDto = new StandardErrorDTO(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Invalid argument or business rule violation",
                ex.getMessage(),
                request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDto);
    }

    // =========================================================================
    // 3. PERSISTÊNCIA, JPA E RESTRIÇÕES DE BANCO DE DADOS
    // =========================================================================

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<StandardErrorDTO> handleEntityNotFound(EntityNotFoundException ex, HttpServletRequest request) {
        StandardErrorDTO errorDto = new StandardErrorDTO(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDto);
    }

    @ExceptionHandler(EntityExistsException.class)
    public ResponseEntity<StandardErrorDTO> handleEntityExists(EntityExistsException ex, HttpServletRequest request) {
        StandardErrorDTO errorDto = new StandardErrorDTO(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                "Email already exists",
                ex.getMessage(),
                request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorDto);
    }

    /**
     * Trata restrições e chaves duplicadas lançadas pelo banco de dados.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<StandardErrorDTO> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        String message = "Recurso já cadastrado ou violação de integridade.";

        if (ex.getMostSpecificCause().getMessage().contains("email")) {
            message = "Este e-mail já está em uso.";
        }

        StandardErrorDTO errorDto = new StandardErrorDTO(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                "Conflito de Dados",
                message,
                request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorDto);
    }

    // =========================================================================
    // 4. ECOSSISTEMA DE SEGURANÇA (SPRING SECURITY & AUTH0 JWT)
    // =========================================================================

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<StandardErrorDTO> handleDisabled(DisabledException ex, HttpServletRequest request) {
        StandardErrorDTO errorDto = new StandardErrorDTO(
                LocalDateTime.now(),
                HttpStatus.UNAUTHORIZED.value(),
                "Account Disabled",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorDto);
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<StandardErrorDTO> handleAccessDenied(org.springframework.security.access.AccessDeniedException ex, HttpServletRequest request) {
        StandardErrorDTO errorDto = new StandardErrorDTO(
                LocalDateTime.now(),
                HttpStatus.FORBIDDEN.value(),
                "Acesso Negado",
                "Você não tem permissão para acessar ou alterar este recurso.",
                request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorDto);
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

    // =========================================================================
    // 5. INFRAESTRUTURA EXTERNA E PROVEDORES DE NUVEM
    // =========================================================================

    /**
     * Rede de segurança para capturar falhas nativas da AWS (S3) não tratadas na camada Service.
     */
    @ExceptionHandler(S3Exception.class)
    public ResponseEntity<StandardErrorDTO> handleS3Exception(S3Exception ex, HttpServletRequest request) {
        StandardErrorDTO errorDto = new StandardErrorDTO(
                LocalDateTime.now(),
                HttpStatus.BAD_GATEWAY.value(),
                "Bad Gateway (Cloud Storage Error)",
                "Ocorreu uma falha na comunicação com o serviço de armazenamento em nuvem.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(errorDto);
    }
}