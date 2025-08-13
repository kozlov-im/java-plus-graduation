package ru.practicum.exception;

import lombok.Getter;

@Getter
public class GeneralException extends Throwable {
    final String reason;

    public GeneralException(final String message, final String reason) {
        super(message);
        this.reason = reason;
    }
}
