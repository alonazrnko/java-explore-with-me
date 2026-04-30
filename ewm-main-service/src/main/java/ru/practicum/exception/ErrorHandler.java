package ru.practicum.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // --- 404 NOT FOUND ---
    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(final NotFoundException e) {
        log.error("404 Not Found: {}", e.getMessage());
        return createApiError(HttpStatus.NOT_FOUND, "The required object was not found.", e.getMessage());
    }

    // --- 409 CONFLICT ---
    @ExceptionHandler({ConflictException.class, DataIntegrityViolationException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflictException(final Exception e) {
        log.error("409 Conflict: {}", e.getMessage());
        return createApiError(HttpStatus.CONFLICT, "Integrity constraint has been violated.", e.getMessage());
    }

    // --- 400 BAD REQUEST ---
    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            ValidationException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            ConstraintViolationException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBadRequestException(final Exception e) {
        log.error("400 Bad Request: {}", e.getMessage());
        String message = e instanceof MethodArgumentNotValidException ?
                ((MethodArgumentNotValidException) e).getFieldError().getDefaultMessage() : e.getMessage();

        return createApiError(HttpStatus.BAD_REQUEST, "Incorrectly made request.", message);
    }

    // --- 500 INTERNAL SERVER ERROR ---
    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleThrowable(final Throwable e) {
        log.error("500 Internal Server Error: {}", e.getMessage(), e);
        return createApiError(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error occurred.", e.getMessage());
    }

    private ApiError createApiError(HttpStatus status, String reason, String message) {
        return ApiError.builder()
                .status(status.name())
                .reason(reason)
                .message(message)
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .build();
    }
}