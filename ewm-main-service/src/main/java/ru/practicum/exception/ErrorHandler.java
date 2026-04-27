package ru.practicum.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(final NotFoundException e) {
        log.error("404 Not Found: {}", e.getMessage(), e);
        return ApiError.builder()
                .status(HttpStatus.NOT_FOUND.name())
                .reason("The required object was not found.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflictException(final ConflictException e) {
        log.error("409 Conflict: {}", e.getMessage(), e);
        return ApiError.builder()
                .status(HttpStatus.CONFLICT.name())
                .reason("Integrity constraint has been violated.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationException(final MethodArgumentNotValidException e) {
        log.error("400 Bad Request: {}", e.getMessage(), e);
        return ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.name())
                .reason("Incorrectly made request.")
                .message(e.getFieldError() != null ? e.getFieldError().getDefaultMessage() : e.getMessage())
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .build();
    }
    
    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleThrowable(final Throwable e) {
        log.error("500 Internal Server Error: {}", e.getMessage(), e);
        return ApiError.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.name())
                .reason("Unexpected error occurred.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleCustomValidationException(final ValidationException e) {
        log.error("400 Bad Request (Validation): {}", e.getMessage(), e);
        return ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.name())
                .reason("Incorrectly made request.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMissingRequestParameterException(final org.springframework.web.bind.MissingServletRequestParameterException e) {
        log.error("400 Bad Request (Missing Parameter): {}", e.getMessage(), e);
        return ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.name())
                .reason("Incorrectly made request.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .build();
    }
}
