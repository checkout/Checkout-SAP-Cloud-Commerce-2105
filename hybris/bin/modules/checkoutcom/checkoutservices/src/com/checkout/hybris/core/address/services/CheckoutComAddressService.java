package com.checkout.hybris.core.address.services;

import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.user.AddressService;

/**
 * Checkout.com AddressService to handle operations on Addresses
 */
public interface CheckoutComAddressService extends AddressService {

    /**
     * Sets the billing address in the given cart model
     *
     * @param cartModel    the given cart model
     * @param addressModel the address to save
     */
    void setCartPaymentAddress(CartModel cartModel, AddressModel addressModel);

    /**
     * Gets customer name with related title
     *
     * @param addressModel the address
     * @return the customer name
     */
    String getCustomerFullNameFromAddress(AddressModel addressModel);
}
