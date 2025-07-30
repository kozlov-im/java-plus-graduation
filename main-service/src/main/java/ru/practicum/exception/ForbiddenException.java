package ru.practicum.exception;

import lombok.Getter;

@Getter
public class ForbiddenException extends RuntimeException {
    final String reason;

    public ForbiddenException(final String message, final String reason) {
        super(message);
        this.reason = reason;
    }

}