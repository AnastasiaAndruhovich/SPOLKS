package by.andruhovich.broadcastchat.exception;

public class SendDataTechnicalException extends Exception {
    public SendDataTechnicalException() {
    }

    public SendDataTechnicalException(String message) {
        super(message);
    }

    public SendDataTechnicalException(String message, Throwable cause) {
        super(message, cause);
    }

    public SendDataTechnicalException(Throwable cause) {
        super(cause);
    }

    public SendDataTechnicalException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
