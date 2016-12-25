package org.kaleta.accountant.service;

/**
 * Created by Stanislav Kaleta on 16.04.2016.
 */
public class ServiceFailureException extends RuntimeException {

    public ServiceFailureException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceFailureException(String message) {
        super(message);
    }

    public ServiceFailureException(Throwable cause) {
        super(cause);
    }

    public ServiceFailureException() {
    }
}
