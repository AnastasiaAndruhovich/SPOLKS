package by.andruhovich.broadcastchat.exception;

public class SocketTechnicalException extends Exception {
    public SocketTechnicalException() {
    }

    public SocketTechnicalException(String message) {
        super(message);
    }

    public SocketTechnicalException(String message, Throwable cause) {
        super(message, cause);
    }

    public SocketTechnicalException(Throwable cause) {
        super(cause);
    }

    public SocketTechnicalException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
