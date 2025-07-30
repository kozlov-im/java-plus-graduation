package ru.practicum.exception;

import lombok.Getter;

@Getter
public class NotFoundException extends RuntimeException {
    final String reason;

    public NotFoundException(final String message, final String reason) {
        super(message);
        this.reason = reason;
    }

}