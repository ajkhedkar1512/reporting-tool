package com.fmc.reporting.exception;

import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {

    private static final long serialVersionUID = 1L;


    public BaseException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public BaseException(final String message) {
        super(message);
    }

    public BaseException(final Exception exception) {
        super(exception);
    }

}
