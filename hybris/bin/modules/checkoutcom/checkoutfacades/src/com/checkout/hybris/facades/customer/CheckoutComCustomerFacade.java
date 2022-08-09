package com.checkout.hybris.facades.customer;

import de.hybris.platform.commercefacades.customer.CustomerFacade;
import de.hybris.platform.commerceservices.customer.DuplicateUidException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Checkout customer facade to contain any customer related method needed on our checkout controllers
 */
public interface CheckoutComCustomerFacade extends CustomerFacade {

    /**
     * Creates a new Google pay guest customer for checkout
     *
     * @throws DuplicateUidException
     */
    void createGooglePayExpressCheckoutGuestUserForAnonymousCheckout() throws DuplicateUidException;

    /**
     * Creates a new Google pay guest customer for checkout and set the user on the session
     * @throws DuplicateUidException
     */
    void createGooglePayExpressCheckoutGuestUserForAnonymousCheckoutAndSetItOnSession() throws DuplicateUidException;

    /**
     * Creates a new Apple pay guest customer for checkout
     *
     * @throws DuplicateUidException
     */
    void createApplePayExpressCheckoutGuestUserForAnonymousCheckout() throws DuplicateUidException;

    /**
     * Creates a new Apple pay guest customer for checkout and set the user on the session
     * @throws DuplicateUidException
     */
    void createApplePayExpressCheckoutGuestUserForAnonymousCheckoutAndSetItOnSession() throws DuplicateUidException;

    /**
     * It checks if the current user is a guest customer
     * @return a boolean stating if is guest user or not
     */
    boolean isGooglePayExpressGuestCustomer();

    /**
     * It checks if the current user is a guest customer
     * @return a boolean stating if is guest user or not
     */
    boolean isApplePayExpressGuestCustomer();

    /**
     * Recalculate express checkout cart.
     */
    void recalculateExpressCheckoutCart();

    /**
     * Updates email from current Express checkout user
     * @param email
     * @param name
     */
    void updateExpressCheckoutUserEmail(String email, String name);
}
