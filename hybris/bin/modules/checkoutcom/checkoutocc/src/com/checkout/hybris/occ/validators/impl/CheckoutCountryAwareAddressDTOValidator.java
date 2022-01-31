/*
 * Copyright (c) 2020 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.checkout.hybris.occ.validators.impl;

import de.hybris.platform.commercewebservicescommons.dto.user.AddressWsDTO;
import de.hybris.platform.webservicescommons.errors.exceptions.WebserviceValidationException;
import de.hybris.platform.webservicescommons.validators.CompositeValidator;
import org.springframework.util.Assert;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Map;
import java.util.Objects;

/**
 * Implementation of {@link Validator} that validate instances of {@link AddressWsDTO}.
 * <p>
 * The {@code CountryAwareAddressValidator} does not validate all fields itself, but delegates to other Validators
 * {@link #countrySpecificAddressWsDTOValidators}. {@code AddressValidator} uses the country.isocode field to select a
 * suitable validator for a specific country. If a matching validator cannot be found,
 * {@link #commonAddressWsDTOValidator} is used.
 */
public class CheckoutCountryAwareAddressDTOValidator implements Validator {

    private static final int MAX_ISOCODE_LENGTH = 2;
    private static final String COUNTRY_ISO = "country.isocode";
    private static final String FIELD_REQUIRED_AND_NOT_TOO_LONG_MESSAGE_ID = "field.requiredAndNotTooLong";

    private final Validator commonAddressWsDTOValidator;
    private final Map<String, Validator> countrySpecificAddressWsDTOValidators;

    /**
     * Default constructor for {@link CheckoutCountryAwareAddressDTOValidator}
     *
     * @param commonAddressWsDTOValidator           injected
     * @param countrySpecificAddressWsDTOValidators injected
     */
    public CheckoutCountryAwareAddressDTOValidator(final Validator commonAddressWsDTOValidator,
                                                   final Map<String, Validator> countrySpecificAddressWsDTOValidators) {
        this.commonAddressWsDTOValidator = commonAddressWsDTOValidator;
        this.countrySpecificAddressWsDTOValidators = countrySpecificAddressWsDTOValidators;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supports(final Class clazz) {
        return AddressWsDTO.class.isAssignableFrom(clazz);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(final Object target, final Errors errors) {
        final AddressWsDTO address = (AddressWsDTO) target;
        Assert.notNull(errors, "Errors object must not be null");

        if (Objects.isNull(address.getCountry()) || Objects.isNull(address.getCountry().getIsocode()) || address.getCountry().getIsocode().length() > MAX_ISOCODE_LENGTH) {
            errors.rejectValue(COUNTRY_ISO, FIELD_REQUIRED_AND_NOT_TOO_LONG_MESSAGE_ID,
                    new String[]{String.valueOf(MAX_ISOCODE_LENGTH)}, null);
            throw new WebserviceValidationException(errors);
        }

        Validator addressValidator = countrySpecificAddressWsDTOValidators.get(address.getCountry().getIsocode());

        if (Objects.isNull(addressValidator )) {
            addressValidator = commonAddressWsDTOValidator;
        }
        addressValidator.validate(target, errors);
    }
}
