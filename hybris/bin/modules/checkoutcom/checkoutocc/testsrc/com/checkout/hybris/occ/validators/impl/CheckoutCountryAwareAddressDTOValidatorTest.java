package com.checkout.hybris.occ.validators.impl;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commercewebservicescommons.dto.user.AddressWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.user.CountryWsDTO;
import de.hybris.platform.webservicescommons.errors.exceptions.WebserviceValidationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutCountryAwareAddressDTOValidatorTest {

    private static final String VALID_COUNTRY_ISO_CODE = "DE";
    private static final String INVALID_COUNTRY_ISO_CODE = "DETE";
    private static final String JAPAN_ISO_CODE = "JP";

    @InjectMocks
    private CheckoutCountryAwareAddressDTOValidator testObj;

    @Mock
    private Validator commonAddressWsDTOValidatorMock;
    @Mock
    private Validator japanAddressWsDTOValidatorMock;

    @Mock
    private AddressWsDTO addressWsDtoMock;
    @Mock
    private Errors errorsMock;
    @Mock
    private CountryWsDTO countryWsDTOMock;


    @Before
    public void setUp() {
        final Map<String, Validator> countrySpecificAddressWsDTOValidatorsMock = Map.of(JAPAN_ISO_CODE, japanAddressWsDTOValidatorMock);
        testObj = new CheckoutCountryAwareAddressDTOValidator(commonAddressWsDTOValidatorMock, countrySpecificAddressWsDTOValidatorsMock);
        when(addressWsDtoMock.getCountry()).thenReturn(countryWsDTOMock);
        when(countryWsDTOMock.getIsocode()).thenReturn(VALID_COUNTRY_ISO_CODE);
    }

    @Test
    public void supports_WhenSubjectClassIsValid_ShouldReturnTrue() {
        final boolean result = testObj.supports(AddressWsDTO.class);

        assertThat(result).isTrue();
    }

    @Test
    public void supports_WhenSubjectClassIsNotValid_ShouldReturnFalse() {
        final boolean result = testObj.supports(String.class);

        assertThat(result).isFalse();
    }

    @Test(expected = NullPointerException.class)
    public void supports_WhenSubjectClassIsNull_ShouldThrowNullPointerException() {
        final boolean result = testObj.supports(null);

        assertThat(result).isFalse();
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_WhenErrorsObjectIsNull_ShouldThrowException() {
        testObj.validate(addressWsDtoMock, null);
    }

    @Test(expected = WebserviceValidationException.class)
    public void validate_WhenAddressCountryIsNull_ShouldThrowException() {
        when(addressWsDtoMock.getCountry()).thenReturn(null);

        testObj.validate(addressWsDtoMock, errorsMock);
    }

    @Test(expected = WebserviceValidationException.class)
    public void validate_WhenAddressCountryIsoCodeIsNull_ShouldThrowException() {
        when(countryWsDTOMock.getIsocode()).thenReturn(null);

        testObj.validate(addressWsDtoMock, errorsMock);
    }

    @Test(expected = WebserviceValidationException.class)
    public void validate_WhenAddressCountryIsoCodeIsInvalid_ShouldThrowException() {
        when(countryWsDTOMock.getIsocode()).thenReturn(INVALID_COUNTRY_ISO_CODE);

        testObj.validate(addressWsDtoMock, errorsMock);
    }

    @Test
    public void validate_WhenAddressCountryIsoCodeHasNoCustomValidator_ShouldThrowException() {
        testObj.validate(addressWsDtoMock, errorsMock);

        verify(commonAddressWsDTOValidatorMock).validate(addressWsDtoMock, errorsMock);
    }

    @Test
    public void validate_WhenAddressCountryIsoCodeHasCustomValidator_ShouldThrowException() {
        when(countryWsDTOMock.getIsocode()).thenReturn(JAPAN_ISO_CODE);

        testObj.validate(addressWsDtoMock, errorsMock);

        verify(japanAddressWsDTOValidatorMock).validate(addressWsDtoMock, errorsMock);
    }
}
