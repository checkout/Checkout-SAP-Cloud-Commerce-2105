package com.checkout.hybris.occ.validators.impl;

import com.checkout.hybris.facades.accelerator.CheckoutComCheckoutFlowFacade;
import com.checkout.hybris.facades.payment.CheckoutComPaymentInfoFacade;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.acceleratorservices.enums.CheckoutPciOptionEnum;
import de.hybris.platform.commercefacades.order.data.CartData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComPlaceOrderCartValidatorTest {

    @InjectMocks
    private CheckoutComPlaceOrderCartValidator testObj;

    @Mock
    private CheckoutComCheckoutFlowFacade checkoutFlowFacadeMock;
    @Mock
    private CheckoutComPaymentInfoFacade checkoutComPaymentInfoFacadeMock;
    @Mock
    private CartData cartDataMock;

    private Errors errors;

    @Before
    public void setUp() {
        errors = new BeanPropertyBindingResult(cartDataMock, CartData.class.getSimpleName());

        when(checkoutFlowFacadeMock.hasCheckoutCart()).thenReturn(true);
        when(checkoutFlowFacadeMock.hasNoPaymentInfo()).thenReturn(false);
        when(checkoutFlowFacadeMock.hasNoDeliveryAddress()).thenReturn(false);
        when(checkoutFlowFacadeMock.hasNoDeliveryMode()).thenReturn(false);
        when(checkoutComPaymentInfoFacadeMock.isTokenMissingOnCardPaymentInfo(cartDataMock)).thenReturn(false);
        when(checkoutFlowFacadeMock.getSubscriptionPciOption()).thenReturn(CheckoutPciOptionEnum.DEFAULT);
        when(checkoutFlowFacadeMock.containsTaxValues()).thenReturn(true);
        when(checkoutFlowFacadeMock.getCheckoutCart()).thenReturn(cartDataMock);
        when(cartDataMock.isCalculated()).thenReturn(true);
    }

    @Test
    public void validateCheckoutPlaceOrderStep_WhenCartDoesNotHaveDeliveryAddress_ShouldReturnTrue() {
        when(checkoutFlowFacadeMock.hasNoDeliveryAddress()).thenReturn(true);

        testObj.validate(cartDataMock, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(errors.getAllErrors().get(0).getCode(), "checkoutcom.occ.deliveryAddress.notSelected");
    }

    @Test
    public void validateCheckoutPlaceOrderStep_WhenCartDoesNotHaveDeliveryMode_ShouldReturnTrue() {
        when(checkoutFlowFacadeMock.hasNoDeliveryMode()).thenReturn(true);

        testObj.validate(cartDataMock, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(errors.getAllErrors().get(0).getCode(), "checkoutcom.occ.deliveryMethod.notSelected");
    }

    @Test
    public void validateCheckoutPlaceOrderStep_WhenCartDoesNotHavePaymentInfo_ShouldReturnTrue() {
        when(checkoutFlowFacadeMock.hasNoPaymentInfo()).thenReturn(true);

        testObj.validate(cartDataMock, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(errors.getAllErrors().get(0).getCode(), "checkoutcom.occ.paymentMethod.notSelected");
    }

    @Test
    public void validateCheckoutPlaceOrderStep_WhenCartPaymentInfoDoesNotHavePaymentToken_ShouldReturnTrue() {
        when(checkoutComPaymentInfoFacadeMock.isTokenMissingOnCardPaymentInfo(cartDataMock)).thenReturn(true);

        testObj.validate(cartDataMock, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(errors.getAllErrors().get(0).getCode(), "checkoutcom.occ.paymentMethod.notSelected");
    }

    @Test
    public void validateCheckoutPlaceOrderStep_WhenCartDoesNotHaveCorrectTaxes_ShouldReturnTrue() {
        when(checkoutFlowFacadeMock.containsTaxValues()).thenReturn(false);

        testObj.validate(cartDataMock, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(errors.getAllErrors().get(0).getCode(), "checkoutcom.occ.error.tax.missing");
    }

    @Test
    public void validateCheckoutPlaceOrderStep_WhenCartIsNotCalculated_ShouldReturnTrue() {
        when(cartDataMock.isCalculated()).thenReturn(false);

        testObj.validate(cartDataMock, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(errors.getAllErrors().get(0).getCode(), "checkoutcom.occ.error.cart.notcalculated");
    }

    @Test
    public void validateCheckoutPlaceOrderStep_WhenPassAllValidations_ShouldReturnFalse() {
        testObj.validate(cartDataMock, errors);

        assertFalse(errors.hasErrors());
    }
}
