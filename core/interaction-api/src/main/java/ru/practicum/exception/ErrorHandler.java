package ru.practicum.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(final NotFoundException e) {
        String errorId = UUID.randomUUID().toString();
        log.error("404 ErrorId {} - stackTrace {}", errorId, e.getStackTrace());
        return new ApiError(
                "Internal error occurred. Please provide this ID to support: " + errorId,
                e.getMessage(),
                e.getReason(),
                HttpStatus.NOT_FOUND.name(),
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflictException(final ConflictException e) {
        String errorId = UUID.randomUUID().toString();
        log.error("409 ErrorId {} - stackTrace {}", errorId, e.getStackTrace());
        return new ApiError(
                "Internal error occurred. Please provide this ID to support: " + errorId,
                e.getMessage(),
                e.getReason(),
                HttpStatus.CONFLICT.name(),
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError handleForbiddenException(final ForbiddenException e) {
        String errorId = UUID.randomUUID().toString();
        log.error("403 ErrorId {} - stackTrace {}", errorId, e.getStackTrace());
        return new ApiError(
                "Internal error occurred. Please provide this ID to support: " + errorId,
                e.getMessage(),
                e.getReason(),
                HttpStatus.FORBIDDEN.name(),
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationException(final ValidationException e) {
        String errorId = UUID.randomUUID().toString();
        log.error("400 ErrorId {} - stackTrace {}", errorId, e.getStackTrace());
        return new ApiError(
                "Internal error occurred. Please provide this ID to support: " + errorId,
                e.getMessage(),
                e.getReason(),
                HttpStatus.BAD_REQUEST.name(),
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(GeneralException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleGeneralException(final GeneralException e) {
        String errorId = UUID.randomUUID().toString();
        log.error("500 ErrorId {} - stackTrace {}", errorId, e.getStackTrace());
        return new ApiError(
                "Internal error occurred. Please provide this ID to support: " + errorId,
                e.getMessage(),
                "Unexpected error",
                HttpStatus.INTERNAL_SERVER_ERROR.name(),
                LocalDateTime.now()
        );
    }
}