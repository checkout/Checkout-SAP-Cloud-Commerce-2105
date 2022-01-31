package com.checkout.hybris.facades.accelerator;

import com.checkout.hybris.facades.beans.AuthorizeResponseData;
import de.hybris.platform.acceleratorfacades.flow.CheckoutFlowFacade;
import de.hybris.platform.core.model.user.AddressModel;

/**
 * Handles checkout flow for Checkout.com
 */
public interface CheckoutComCheckoutFlowFacade extends CheckoutFlowFacade {

    /**
     * Authorizes the order using the payment token got from checkout.com or using the stored payment
     * source id and returns the response
     *
     * @return AuthorizeResponseData an object that contains the property success that says if the authorize has been
     * done correctly, the property redirect if we need to do a redirect to the property redirect url
     */
    AuthorizeResponseData authorizePayment();

    /**
     * Removes the payment info model if present from the session cart
     */
    void removePaymentInfoFromSessionCart();

    /**
     * Sets the payment info billing address as session cart payment address
     */
    void setPaymentInfoBillingAddressOnSessionCart();

    /**
     * Checks if the payment is an APM with user data required.
     *
     * @return true if apm with data form, false otherwise
     */
    boolean isUserDataRequiredApmPaymentMethod();

    /**
     * Checks the session cart payment info and gets the current payment method type
     *
     * @return the payment method type
     */
    String getCurrentPaymentMethodType();

    /**
     * Gets the delivery address for the given id
     *
     * @param addressId the address identifier
     * @return {@link AddressModel} the delivery address for the id
     */
    AddressModel getDeliveryAddressModelForCode(String addressId);
}