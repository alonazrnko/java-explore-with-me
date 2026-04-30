package ru.practicum.exception;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiError {
    private String status;
    private String reason;
    private String message;
    private String timestamp;
}
