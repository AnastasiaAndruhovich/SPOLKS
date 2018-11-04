package by.andruhovich.server.exception.socket;

public class ReceiveDataTechnicalException extends Exception {
    public ReceiveDataTechnicalException() {
    }

    public ReceiveDataTechnicalException(String message) {
        super(message);
    }

    public ReceiveDataTechnicalException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReceiveDataTechnicalException(Throwable cause) {
        super(cause);
    }

    public ReceiveDataTechnicalException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
