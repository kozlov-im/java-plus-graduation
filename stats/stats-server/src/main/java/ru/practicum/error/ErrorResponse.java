package ru.practicum.error;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class ErrorResponse {
    HttpStatus httpStatus;
    String message;
    String stackTrace;

    public ErrorResponse(HttpStatus httpStatus, String message, String stackTrace) {
        this.httpStatus = httpStatus;
        this.message = message;
        this.stackTrace = stackTrace;
    }
}
