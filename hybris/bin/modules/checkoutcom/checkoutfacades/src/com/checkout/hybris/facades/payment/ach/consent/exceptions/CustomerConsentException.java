package com.checkout.hybris.facades.payment.ach.consent.exceptions;

/**
 * Thrown when customer consent is not checked
 */
public class CustomerConsentException extends Exception {

    public CustomerConsentException()
    {
        super("The customer consent is mandatory");
    }

    /**
     * @param message
     */
    public CustomerConsentException(final String message) {
        super(message);
    }
}
