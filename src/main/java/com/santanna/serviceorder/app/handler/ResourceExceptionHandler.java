package com.santanna.serviceorder.app.handler;

import com.santanna.serviceorder.app.handler.model.BadRequestException;
import com.santanna.serviceorder.app.handler.model.NotFoundException;
import com.santanna.serviceorder.app.handler.model.StandardError;
import com.santanna.serviceorder.app.handler.model.InternalServerErrorException;
import com.santanna.serviceorder.utils.LoggerUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@ControllerAdvice
public class ResourceExceptionHandler {
    private final LoggerUtils loggerUtils;

    public ResourceExceptionHandler(LoggerUtils loggerUtils) {
        this.loggerUtils = loggerUtils;
    }
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<StandardError> handleBadRequestException(BadRequestException ex, HttpServletRequest request) {
        loggerUtils.logWarn(ResourceExceptionHandler.class, "Bad request error: {} - Path: {}", ex.getMessage(), request.getRequestURI());

        StandardError error = new StandardError(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage() != null ? ex.getMessage() : "Requisição inválida",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<StandardError> handleNotFoundException(NotFoundException ex, HttpServletRequest request) {
        loggerUtils.logWarn(ResourceExceptionHandler.class, "Resource not found: {} - Path: {}", ex.getMessage(), request.getRequestURI());

        StandardError error = new StandardError(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage() != null ? ex.getMessage() : "Recurso não encontrado",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(InternalServerErrorException.class)
    public ResponseEntity<StandardError> handleInternalServerErrorException(InternalServerErrorException ex, HttpServletRequest request) {
        loggerUtils.logError(ResourceExceptionHandler.class, "Internal server error: {} - Path: {}", ex, request.getRequestURI());

        StandardError error = new StandardError(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ex.getMessage() != null ? ex.getMessage() : "Erro interno no servidor",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandardError> handleAllExceptions(Exception ex, HttpServletRequest request) {
        loggerUtils.logError(ResourceExceptionHandler.class, "Unexpected error: {} - Path: {}", ex, request.getRequestURI());

        StandardError error = new StandardError(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ex.getMessage() != null ? ex.getMessage() : "Erro inesperado",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<StandardError> handleConstraintViolationException(ConstraintViolationException ex, HttpServletRequest request) {
        loggerUtils.logWarn(ResourceExceptionHandler.class, "Validation error: {} - Path: {}", ex.getMessage(), request.getRequestURI());

        StandardError error = new StandardError(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed: " + ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<StandardError> handleDataIntegrityViolationException(DataIntegrityViolationException ex, HttpServletRequest request) {
        loggerUtils.logWarn(ResourceExceptionHandler.class, "Data integrity violation: {} - Path: {}", ex.getMessage(), request.getRequestURI());

        StandardError error = new StandardError(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Data integrity violation: " + ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        loggerUtils.logWarn(ResourceExceptionHandler.class, "Method argument validation failed - Path: {}", request.getRequestURI());

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("message", "Validation failed");


        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage())
        );

        response.put("errors", fieldErrors);
        response.put("path", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

}
