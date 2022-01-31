package com.checkout.hybris.core.certificate.exceptions;

/**
 * Exception thrown from the Checkout.com certificate errors
 */
public class CheckoutComCertificateException extends RuntimeException {

    /**
     * Constructor
     *
     * @param message   exception message
     * @param throwable the throwable to pass
     */
    public CheckoutComCertificateException(final String message, final Throwable throwable) {
        super(message, throwable);
    }

}
