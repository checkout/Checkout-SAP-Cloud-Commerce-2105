/*
 * Copyright (c) 2020 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.checkout.hybris.occ.validators.impl;

import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commerceservices.delivery.DeliveryService;
import de.hybris.platform.core.PK;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.order.CartService;
import de.hybris.platform.servicelayer.model.AbstractItemModel;
import org.springframework.util.Assert;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


public class CheckoutDeliveryAddressValidator implements Validator {

    private static final String FIELD_REQUIRED = "field.required";
    private static final String DELIVERY_ADDRESS_INVALID = "delivery.address.invalid";
    private static final String ADDRESS_ID = "id";

    @Resource(name = "deliveryService")
    private DeliveryService deliveryService;
    @Resource(name = "cartService")
    private CartService cartService;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supports(final Class clazz) {
        return AddressData.class.isAssignableFrom(clazz);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(final Object target, final Errors errors) {
        final AddressData addressData = (AddressData) target;
        Assert.notNull(errors, "Errors object must not be null");

		final String addressId = addressData.getId();

		if (Objects.isNull(addressId) || addressId.trim().isEmpty()) {
            //create ERROR
            errors.rejectValue(ADDRESS_ID, FIELD_REQUIRED);
            return;
        }

        if (cartService.hasSessionCart()) {
            final CartModel sessionCartModel = cartService.getSessionCart();
            final List<AddressModel> addresses = deliveryService.getSupportedDeliveryAddressesForOrder(sessionCartModel, false);
			if (checkIfAddressIdIsPresent(addressId, addresses)) {
				//positive scenario - address with given ID is suitable for delivery. Validation is done here.
				return;
			}
        }
        // delivery is not supported. Create Error
        errors.rejectValue(ADDRESS_ID, DELIVERY_ADDRESS_INVALID);
    }

	/**
	 * Checks if any of the given addresses contains the given AddressId
	 * @param addressId The address Id to find
	 * @param addresses The addresses to check for the given addressId
	 * @return True if present, false otherwise
	 */
	private boolean checkIfAddressIdIsPresent(final String addressId, final List<AddressModel> addresses) {
		return Optional.ofNullable(addresses)
				.stream()
				.flatMap(Collection::stream)
				.map(AbstractItemModel::getPk)
				.map(PK::toString)
				.anyMatch(addressId::equals);
	}
}
