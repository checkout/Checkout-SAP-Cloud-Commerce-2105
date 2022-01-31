package com.checkout.hybris.core.address.strategies;

import com.checkout.common.Phone;
import de.hybris.platform.core.model.user.AddressModel;

import java.util.Optional;

/**
 * Strategy to create handle Phone object for checkout.com requests based on valid fields
 */
public interface CheckoutComPhoneNumberStrategy {

    /**
     * Creates a valid Phone object for checkout.com request based on the given address model phone fields.
     * Override this method to send the separate Country Code
     *
     * @param addressModel the source address model
     * @return Phone the request object
     */
    Optional<Phone> createPhone(AddressModel addressModel);
}
