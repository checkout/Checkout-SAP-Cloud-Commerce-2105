package com.checkout.hybris.events.validators;

import javax.servlet.http.HttpServletRequest;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Interface to handle events request validations.
 */
public interface CheckoutComRequestEventValidator {

    /**
     * Validates the event signature/Headers
     *
     * @param eventBody    the event body
     * @return true if valid, false otherwise
     * @throws InvalidKeyException      secret key invalid
     * @throws NoSuchAlgorithmException algorithm not found
     */
    boolean isRequestEventValid(final HttpServletRequest request, final String eventBody) throws NoSuchAlgorithmException, InvalidKeyException;
}
