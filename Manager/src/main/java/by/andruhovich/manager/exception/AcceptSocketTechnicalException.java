package by.andruhovich.manager.exception;

public class AcceptSocketTechnicalException extends Exception {
    public AcceptSocketTechnicalException() {
    }

    public AcceptSocketTechnicalException(String message) {
        super(message);
    }

    public AcceptSocketTechnicalException(String message, Throwable cause) {
        super(message, cause);
    }

    public AcceptSocketTechnicalException(Throwable cause) {
        super(cause);
    }

    public AcceptSocketTechnicalException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
