package br.com.teste.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import br.com.teste.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, WebRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .findFirst()
            .orElse("Validation failed");

        logger.warn("Validation error: {}", message);

        ErrorResponse errorResponse = new ErrorResponse(
            message,
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(GarageConfigException.class)
    public ResponseEntity<ErrorResponse> handleGarageConfigException(
            GarageConfigException ex, WebRequest request) {
        logger.warn("Garage config error: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
            ex.getMessage(),
            LocalDateTime.now(),
            HttpStatus.SERVICE_UNAVAILABLE.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(SectorFullException.class)
    public ResponseEntity<ErrorResponse> handleSectorFullException(
            SectorFullException ex, WebRequest request) {
        logger.warn("Sector full: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
            ex.getMessage(),
            LocalDateTime.now(),
            HttpStatus.CONFLICT.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(VehicleNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleVehicleNotFoundException(
            VehicleNotFoundException ex, WebRequest request) {
        logger.warn("Vehicle not found: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
            ex.getMessage(),
            LocalDateTime.now(),
            HttpStatus.NOT_FOUND.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidEventException.class)
    public ResponseEntity<ErrorResponse> handleInvalidEventException(
            InvalidEventException ex, WebRequest request) {
        logger.warn("Invalid event: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
            ex.getMessage(),
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex, WebRequest request) {
        String message = String.format("Invalid argument: %s should be of type %s",
            ex.getName(), ex.getRequiredType().getSimpleName());
        logger.warn("Argument type mismatch: {}", message);
        ErrorResponse errorResponse = new ErrorResponse(
            message,
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(
            Exception ex, WebRequest request) {
        logger.error("Unexpected exception occurred", ex);

        ErrorResponse errorResponse = new ErrorResponse(
            "An unexpected error occurred. Please contact support.",
            LocalDateTime.now(),
            HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
