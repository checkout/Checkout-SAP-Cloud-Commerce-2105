package com.checkout.hybris.core.payment.exception;

/**
 * Exception thrown from the Checkout.com integration errors
 */
public class CheckoutComPaymentIntegrationException extends RuntimeException {

    /**
     * Constructor
     *
     * @param message exception message
     */
    public CheckoutComPaymentIntegrationException(final String message) {
        super(message);
    }

    /**
     * Constructor
     *
     * @param message   exception message
     * @param throwable the throwable to pass
     */
    public CheckoutComPaymentIntegrationException(final String message, final Throwable throwable) {
        super(message, throwable);
    }

}
