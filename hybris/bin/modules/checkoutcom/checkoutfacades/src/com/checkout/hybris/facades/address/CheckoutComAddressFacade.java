package com.checkout.hybris.facades.address;

import de.hybris.platform.commercefacades.user.data.AddressData;

/**
 * Handles the address operations for checkout.com
 */
public interface CheckoutComAddressFacade {

    /**
     * Gets the current cart billing address for the checkout flow
     *
     * @return AddressData the address to expose
     */
    AddressData getCartBillingAddress();

    /**
     * Sets billing details to the session cart (payment address)
     *
     * @param addressData the address data
     */
    void setCartBillingDetails(AddressData addressData);

    /**
     * Sets billing details to the session cart (payment address) given an address Id
     *
     * @param addressId the address id
     */
    void setCartBillingDetailsByAddressId(String addressId);
}
