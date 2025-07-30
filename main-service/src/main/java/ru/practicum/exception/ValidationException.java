package ru.practicum.exception;

import lombok.Getter;

@Getter
public class ValidationException extends RuntimeException {
    final String reason;

    public ValidationException(final String message, final String reason) {
        super(message);
        this.reason = reason;
    }

}