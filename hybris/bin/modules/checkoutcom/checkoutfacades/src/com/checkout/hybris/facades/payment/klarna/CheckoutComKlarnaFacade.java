package com.checkout.hybris.facades.payment.klarna;

import com.checkout.hybris.facades.beans.KlarnaClientTokenData;

import java.util.concurrent.ExecutionException;

/**
 * Facade for Klarna functionalities
 */
public interface CheckoutComKlarnaFacade {

    /**
     * Gets the klarna client token for the session cart
     */
    KlarnaClientTokenData getKlarnaClientToken() throws IllegalArgumentException, ExecutionException;
}
