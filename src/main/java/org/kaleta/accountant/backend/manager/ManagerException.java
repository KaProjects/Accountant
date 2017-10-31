package org.kaleta.accountant.backend.manager;

public class ManagerException extends Exception {

    public ManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public ManagerException(String message) {
        super(message);
    }

    public ManagerException(Throwable cause) {
        super(cause);
    }

    public ManagerException() {
    }
}
