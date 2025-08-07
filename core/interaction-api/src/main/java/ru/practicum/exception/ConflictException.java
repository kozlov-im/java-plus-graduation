package ru.practicum.exception;

import lombok.Getter;

@Getter
public class ConflictException extends RuntimeException{
    final String reason;

    public ConflictException(final String message, final String reason) {
        super(message);
        this.reason = reason;
    }
}
