package ro.ubb.models;

public class NotEnoughProductsException extends RuntimeException {

    public NotEnoughProductsException(String message) {
        super(message);
    }

    public NotEnoughProductsException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotEnoughProductsException(Throwable cause) {
        super(cause);
    }


}
