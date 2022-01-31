package com.checkout.hybris.core.address.strategies.impl;

import com.checkout.common.Phone;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.user.AddressModel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultCheckoutComPhoneNumberStrategyTest {

    private static final String PHONE_TOO_LONG = " 13213 ffdsfds srwrf;dgdg54 ";
    private static final String COUNTRY_CODE_MAX_LONG_VALID = " +44 222 ";
    private static final String VALID_PHONE_NUMBER_MAX_LONG = " +44 32 32 23 222 23213443 ";
    private static final String COUNTRY_CODE_TOO_LONG = " 212 44fv ";
    private static final String PHONE_TOO_SHORT = " 123 3 ";
    private static final String COUNTRY_CODE_MIN_LONG_VALID = " 1 ";
    private static final String VALID_PHONE_NUMBER_MIN_LONG = " 123 33 ";
    private static final String VALID_COUNTRY_CODE_MAX_LONG_EXPECTED = "+44 222";
    private static final String VALID_PHONE_MAX_LONG_EXPECTED = "+44 32 32 23 222 23213443";
    private static final String VALID_PHONE_MIN_LONG_EXPECTED = "123 33";
    private static final String VALID_COUNTRY_CODE_MIN_LONG_EXPECTED = "1";

    @InjectMocks
    private DefaultCheckoutComPhoneNumberStrategy testObj;

    @Mock
    private AddressModel addressModelMock;

    @Test(expected = IllegalArgumentException.class)
    public void createPhone_WhenNullAddress_ShouldThrowException() {
        testObj.createPhone(null);
    }

    @Test
    public void createPhone_WhenNumberNull_ShouldRetrieveNullObject() {
        when(addressModelMock.getPhone1()).thenReturn(null);

        final Optional<Phone> result = testObj.createPhone(addressModelMock);

        assertFalse(result.isPresent());
    }

    @Test
    public void createPhone_WhenNumberEmpty_ShouldRetrieveNullObject() {
        when(addressModelMock.getPhone1()).thenReturn("               ");

        final Optional<Phone> result = testObj.createPhone(addressModelMock);

        assertFalse(result.isPresent());
    }

    @Test
    public void createPhone_WhenNumberTooShort_ShouldRetrieveNullObject() {
        when(addressModelMock.getPhone1()).thenReturn(PHONE_TOO_SHORT);

        final Optional<Phone> result = testObj.createPhone(addressModelMock);

        assertFalse(result.isPresent());
    }

    @Test
    public void createPhone_WhenNumberTooLong_ShouldRetrieveNullObject() {
        when(addressModelMock.getPhone1()).thenReturn(PHONE_TOO_LONG);

        final Optional<Phone> result = testObj.createPhone(addressModelMock);

        assertFalse(result.isPresent());
    }

    @Test
    public void createPhone_WhenIsCorrectButCountryCodeNull_ShouldReturnThePhone() {
        when(addressModelMock.getPhone1()).thenReturn(VALID_PHONE_NUMBER_MAX_LONG);

        Optional<Phone> result = testObj.createPhone(addressModelMock);

        assertTrue(result.isPresent());
        assertEquals(VALID_PHONE_MAX_LONG_EXPECTED, result.get().getNumber());
        assertNull(result.get().getCountryCode());

        when(addressModelMock.getPhone1()).thenReturn(VALID_PHONE_NUMBER_MIN_LONG);

        result = testObj.createPhone(addressModelMock);

        assertTrue(result.isPresent());
        assertEquals(VALID_PHONE_MIN_LONG_EXPECTED, result.get().getNumber());
        assertNull(result.get().getCountryCode());
    }

    @Test
    public void getPhone_WhenCountryCodeIsEmpty_ShouldRetrievePhoneWithNullCountryCode() {
        final Optional<Phone> result = testObj.getPhone("               ", VALID_PHONE_NUMBER_MAX_LONG);

        assertTrue(result.isPresent());
        assertEquals(VALID_PHONE_MAX_LONG_EXPECTED, result.get().getNumber());
        assertNull(result.get().getCountryCode());
    }

    @Test
    public void getPhone_WhenCountryCodeIsTooLong_ShouldRetrievePhoneWithNullCountryCode() {
        final Optional<Phone> result = testObj.getPhone(COUNTRY_CODE_TOO_LONG, VALID_PHONE_NUMBER_MAX_LONG);

        assertTrue(result.isPresent());
        assertEquals(VALID_PHONE_MAX_LONG_EXPECTED, result.get().getNumber());
        assertNull(result.get().getCountryCode());
    }

    @Test
    public void getPhone_WhenEverythingIsCorrectlyFilled_ShouldReturnThePhoneFullyPopulated() {
        Optional<Phone> result = testObj.getPhone(COUNTRY_CODE_MAX_LONG_VALID, VALID_PHONE_NUMBER_MAX_LONG);

        assertTrue(result.isPresent());
        assertEquals(VALID_PHONE_MAX_LONG_EXPECTED, result.get().getNumber());
        assertEquals(VALID_COUNTRY_CODE_MAX_LONG_EXPECTED, result.get().getCountryCode());

        result = testObj.getPhone(COUNTRY_CODE_MIN_LONG_VALID, VALID_PHONE_NUMBER_MAX_LONG);

        assertTrue(result.isPresent());
        assertEquals(VALID_PHONE_MAX_LONG_EXPECTED, result.get().getNumber());
        assertEquals(VALID_COUNTRY_CODE_MIN_LONG_EXPECTED, result.get().getCountryCode());
    }
}