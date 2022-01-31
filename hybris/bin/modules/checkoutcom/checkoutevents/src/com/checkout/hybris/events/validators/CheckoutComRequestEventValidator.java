package com.checkout.hybris.events.validators;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Interface to handle events request validations.
 */
public interface CheckoutComRequestEventValidator {

    /**
     * Validates the ckoSignature
     *
     * @param ckoSignature the received signature
     * @param eventBody    the event body
     * @return true if valid, false otherwise
     * @throws InvalidKeyException      secret key invalid
     * @throws NoSuchAlgorithmException algorithm not found
     */
    boolean isCkoSignatureValid(String ckoSignature, String eventBody) throws InvalidKeyException, NoSuchAlgorithmException;
}
