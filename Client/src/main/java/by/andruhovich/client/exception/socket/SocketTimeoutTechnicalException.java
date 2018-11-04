package by.andruhovich.client.exception.socket;

public class SocketTimeoutTechnicalException extends Exception {
    public SocketTimeoutTechnicalException() {
    }

    public SocketTimeoutTechnicalException(String message) {
        super(message);
    }

    public SocketTimeoutTechnicalException(String message, Throwable cause) {
        super(message, cause);
    }

    public SocketTimeoutTechnicalException(Throwable cause) {
        super(cause);
    }

    public SocketTimeoutTechnicalException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
