package dev.vaidik.orders.common;

/** A request that is valid but conflicts with current state (duplicate email, not enough stock...). */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
