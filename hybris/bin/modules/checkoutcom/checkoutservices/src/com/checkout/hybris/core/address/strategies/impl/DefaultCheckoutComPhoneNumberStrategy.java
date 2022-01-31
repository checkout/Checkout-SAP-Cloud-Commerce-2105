package com.checkout.hybris.core.address.strategies.impl;

import com.checkout.common.Phone;
import com.checkout.hybris.core.address.strategies.CheckoutComPhoneNumberStrategy;
import de.hybris.platform.core.model.user.AddressModel;
import org.apache.commons.lang.StringUtils;

import java.util.Optional;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

/**
 * Default implementation of {@link CheckoutComPhoneNumberStrategy}
 */
public class DefaultCheckoutComPhoneNumberStrategy implements CheckoutComPhoneNumberStrategy {

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Phone> createPhone(final AddressModel addressModel) {
        validateParameterNotNull(addressModel, "Address model cannot be null");

        return getPhone(null, addressModel.getPhone1());
    }

    /**
     * Gets the valid phone request object
     *
     * @param countryCode the country code number
     * @param number      the phone number
     * @return an Option with the request Phone
     */
    protected Optional<Phone> getPhone(final String countryCode, final String number) {
        return createValidPhone(countryCode, number);
    }

    /**
     * Apply the validation for countryCode and number and if the country code is invalid
     * but the phone number is valid, then the country code will be empty
     *
     * @param countryCode the country code number
     * @param number      the phone number
     * @return an Option with the request Phone
     */
    private Optional<Phone> createValidPhone(final String countryCode, final String number) {
        if (StringUtils.isNotBlank(number) && isFieldValid(number.trim(), 6, 25)) {
            Phone phone = new Phone();
            phone.setNumber(number.trim());

            if (StringUtils.isNotBlank(countryCode) && isFieldValid(countryCode.trim(), 1, 7)) {
                phone.setCountryCode(countryCode.trim());
            }
            return Optional.of(phone);
        }
        return Optional.empty();
    }

    private boolean isFieldValid(final String field, final int minLength, final int maxLength) {
        return field.length() >= minLength && field.length() <= maxLength;
    }
}
