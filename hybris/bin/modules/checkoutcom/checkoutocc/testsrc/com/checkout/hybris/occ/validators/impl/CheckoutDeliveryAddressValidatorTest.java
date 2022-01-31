package com.checkout.hybris.occ.validators.impl;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commerceservices.delivery.DeliveryService;
import de.hybris.platform.core.PK;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.order.CartService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutDeliveryAddressValidatorTest {

    private static final int INVALID_ADDRESS_ID_LONG_VALUE = 1;
    private static final int VALID_ADDRESS_ID_LONG_VALUE = 12356;
    private static final String EMPTY_ID = "  ";
    private static final String ADDRESS_ID = "12356";
    private static final String FIELD_REQUIRED = "field.required";
    private static final String DELIVERY_ADDRESS_INVALID = "delivery.address.invalid";

    @InjectMocks
    private CheckoutDeliveryAddressValidator testObj;

    @Mock
    private CartService cartServiceMock;
    @Mock
    private DeliveryService deliveryServiceMock;

    @Mock
    private CartModel cartModelMock;
    @Mock
    private AddressData addressDataMock;
    @Mock
    private AddressModel addressModelMock;

    private Errors errors;

    @Before
    public void setUp() {
        errors = new BeanPropertyBindingResult(addressDataMock, AddressData.class.getSimpleName());
        when(addressDataMock.getId()).thenReturn(ADDRESS_ID);
        when(cartServiceMock.hasSessionCart()).thenReturn(Boolean.TRUE);
        when(cartServiceMock.getSessionCart()).thenReturn(cartModelMock);
        when(deliveryServiceMock.getSupportedDeliveryAddressesForOrder(cartModelMock, false)).thenReturn(List.of(addressModelMock));
    }

    @Test
    public void supports_WhenSubjectClassIsValid_ShouldReturnTrue() {
        final boolean result = testObj.supports(AddressData.class);

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
        testObj.validate(addressDataMock, null);
    }

    @Test
    public void validate_WhenAddressIdIsNull_ShouldReturnWithError() {
        when(addressDataMock.getId()).thenReturn(null);

        testObj.validate(addressDataMock, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(errors.getAllErrors().get(0).getCode(), FIELD_REQUIRED);
    }

    @Test
    public void validate_WhenAddressIdIsEmpty_ShouldReturnWithError() {
        when(addressDataMock.getId()).thenReturn(EMPTY_ID);
        testObj.validate(addressDataMock, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(errors.getAllErrors().get(0).getCode(), FIELD_REQUIRED);
    }

    @Test
    public void validate_WhenSessionHasNoCart_ShouldReturnWithError() {
        when(cartServiceMock.hasSessionCart()).thenReturn(Boolean.FALSE);

        testObj.validate(addressDataMock, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(errors.getAllErrors().get(0).getCode(), DELIVERY_ADDRESS_INVALID);
    }

    @Test
    public void validate_WhenSessionCartHasNoSupportedDeliveryAddresses_ShouldReturnWithError() {
        when(deliveryServiceMock.getSupportedDeliveryAddressesForOrder(cartModelMock, false)).thenReturn(Collections.emptyList());

        testObj.validate(addressDataMock, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(errors.getAllErrors().get(0).getCode(), DELIVERY_ADDRESS_INVALID);
    }

    @Test
    public void validate_WhenSessionCartHasSupportedDeliveryAddressesWithoutTheSameId_ShouldReturnWithError() {
        when(addressModelMock.getPk()).thenReturn(PK.fromLong(INVALID_ADDRESS_ID_LONG_VALUE));

        testObj.validate(addressDataMock, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(errors.getAllErrors().get(0).getCode(), DELIVERY_ADDRESS_INVALID);
    }

    @Test
    public void validate_WhenSessionCartHasSupportedDeliveryAddressesWithTheSameId_ShouldReturnWithError() {
        when(addressModelMock.getPk()).thenReturn(PK.fromLong(VALID_ADDRESS_ID_LONG_VALUE));

        testObj.validate(addressDataMock, errors);

        assertFalse(errors.hasErrors());
    }
}
