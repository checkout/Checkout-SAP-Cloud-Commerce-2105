package com.checkout.hybris.occ.exceptions;

/**
 * Thrown when failed to place the order
 */
public class PlaceOrderException extends Exception {

    public PlaceOrderException()
    {
        super("Failed to place the order");
    }

    /**
     * @param message
     */
    public PlaceOrderException(final String message) {
        super(message);
    }
}
