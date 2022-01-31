package com.checkout.hybris.addon.validators.impl;

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
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultCheckoutComStepsValidatorTest {

    private static final String SECURITY_CODE = "SECURITY_CODE";

    @InjectMocks
    private DefaultCheckoutComStepsValidator testObj;

    @Mock
    private CheckoutComCheckoutFlowFacade checkoutFlowFacadeMock;
    @Mock
    private CheckoutComPaymentInfoFacade checkoutComPaymentInfoFacadeMock;
    @Mock
    private CartData cartDataMock;
    @Mock
    private RedirectAttributes redirectAttributesMock;

    private Model modelMock = new ExtendedModelMap();

    @Before
    public void setUp() {
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
    public void hasNoSessionCart_WhenSessionDoesNotHaveCart_ShouldReturnTrue() {
        when(checkoutFlowFacadeMock.hasCheckoutCart()).thenReturn(false);

        final boolean result = testObj.hasNoSessionCart(modelMock);

        assertTrue(result);
    }

    @Test
    public void hasNoSessionCart_WhenSessionHasCart_ShouldReturnFalse() {
        final boolean result = testObj.hasNoSessionCart(modelMock);

        assertFalse(result);
    }

    @Test
    public void validateCheckoutPlaceOrderStep_WhenCartDoesNotHaveDeliveryAddress_ShouldReturnTrue() {
        when(checkoutFlowFacadeMock.hasNoDeliveryAddress()).thenReturn(true);

        final boolean result = testObj.validateCheckoutPlaceOrderStep(redirectAttributesMock, SECURITY_CODE);

        assertTrue(result);
    }

    @Test
    public void validateCheckoutPlaceOrderStep_WhenCartDoesNotHaveDeliveryMode_ShouldReturnTrue() {
        when(checkoutFlowFacadeMock.hasNoDeliveryMode()).thenReturn(true);

        final boolean result = testObj.validateCheckoutPlaceOrderStep(redirectAttributesMock, SECURITY_CODE);

        assertTrue(result);
    }

    @Test
    public void validateCheckoutPlaceOrderStep_WhenCartDoesNotHavePaymentInfo_ShouldReturnTrue() {
        when(checkoutFlowFacadeMock.hasNoPaymentInfo()).thenReturn(true);

        final boolean result = testObj.validateCheckoutPlaceOrderStep(redirectAttributesMock, SECURITY_CODE);

        assertTrue(result);
    }

    @Test
    public void validateCheckoutPlaceOrderStep_WhenCartPaymentInfoDoesNotHavePaymentToken_ShouldReturnTrue() {
        when(checkoutComPaymentInfoFacadeMock.isTokenMissingOnCardPaymentInfo(cartDataMock)).thenReturn(true);

        final boolean result = testObj.validateCheckoutPlaceOrderStep(redirectAttributesMock, SECURITY_CODE);

        assertTrue(result);
    }

    @Test
    public void validateCheckoutPlaceOrderStep_WhenSecurityCodeNull_ShouldReturnTrue() {
        final boolean result = testObj.validateCheckoutPlaceOrderStep(redirectAttributesMock, null);

        assertTrue(result);
    }

    @Test
    public void validateCheckoutPlaceOrderStep_WhenCartDoesNotHaveCorrectTaxes_ShouldReturnTrue() {
        when(checkoutFlowFacadeMock.containsTaxValues()).thenReturn(false);

        final boolean result = testObj.validateCheckoutPlaceOrderStep(redirectAttributesMock, SECURITY_CODE);

        assertTrue(result);
    }

    @Test
    public void validateCheckoutPlaceOrderStep_WhenCartIsNotCalculated_ShouldReturnTrue() {
        when(cartDataMock.isCalculated()).thenReturn(false);

        final boolean result = testObj.validateCheckoutPlaceOrderStep(redirectAttributesMock, SECURITY_CODE);

        assertTrue(result);
    }

    @Test
    public void validateCheckoutPlaceOrderStep_WhenPassAllValidations_ShouldReturnFalse() {
        final boolean result = testObj.validateCheckoutPlaceOrderStep(redirectAttributesMock, SECURITY_CODE);

        assertFalse(result);
    }

    @Test
    public void isTermsAndConditionsAccepted_WhenTAndCIsNotChecked_ShouldReturnFalse() {
        final boolean result = testObj.isTermsAndConditionsAccepted(modelMock, false);

        assertFalse(result);
    }

    @Test
    public void isTermsAndConditionsAccepted_WhenTAndCIsNotChecked_ShouldReturnTrue() {
        final boolean result = testObj.isTermsAndConditionsAccepted(modelMock, true);

        assertTrue(result);
    }
}
