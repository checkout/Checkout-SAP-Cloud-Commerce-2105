package com.checkout.hybris.core.payment.response.strategies.impl;

import com.checkout.hybris.core.authorisation.AuthorizeResponse;
import com.checkout.hybris.core.model.CheckoutComAPMPaymentInfoModel;
import com.checkout.hybris.core.model.CheckoutComBenefitPayPaymentInfoModel;
import com.checkout.hybris.core.payment.exception.CheckoutComPaymentIntegrationException;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentInfoService;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentIntegrationService;
import com.checkout.payments.AlternativePaymentSourceResponse;
import com.checkout.payments.CardSourceResponse;
import com.checkout.payments.GetPaymentResponse;
import com.checkout.payments.PaymentPending;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.BENEFITPAY;
import static org.junit.Assert.*;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComBenefitPayPaymentResponseStrategyTest {

    private static final String PAYMENT_ID = "paymentId";
    private static final String QR_CODE_VALUE = "dhjgdhnuxugydfbhdzdgfus";

    @InjectMocks
    private CheckoutComBenefitPayPaymentResponseStrategy testObj;

    @Mock
    private PaymentPending pendingResponseMock;
    @Mock
    private CheckoutComPaymentIntegrationService checkoutComPaymentIntegrationServiceMock;
    @Mock
    private GetPaymentResponse getPaymentResponseMock;
    @Mock
    private CardSourceResponse cardSourceResponseMock;
    @Mock
    private PaymentInfoModel paymentInfoMock;
    @Mock
    private CheckoutComAPMPaymentInfoModel apmPaymentInfoMock;
    @Mock
    private CheckoutComBenefitPayPaymentInfoModel benefitPayPaymentInfoMock;
    @Mock
    private CheckoutComPaymentInfoService paymentInfoServiceMock;

    private AlternativePaymentSourceResponse apmPaymentSource = new AlternativePaymentSourceResponse();

    @Before
    public void setUp() {
        when(checkoutComPaymentIntegrationServiceMock.getPaymentDetails(PAYMENT_ID)).thenReturn(getPaymentResponseMock);
        when(getPaymentResponseMock.getId()).thenReturn(PAYMENT_ID);
        when(pendingResponseMock.getId()).thenReturn(PAYMENT_ID);
        when(getPaymentResponseMock.getSource()).thenReturn(apmPaymentSource);
        apmPaymentSource.put("qr_data", QR_CODE_VALUE);
    }

    @Test
    public void getStrategyKey_ShouldReturnBenefitPayPaymentType() {
        assertEquals(BENEFITPAY, testObj.getStrategyKey());
    }

    @Test
    public void handlePendingPaymentResponse_WhenBenefitPay_ShouldReturnAuthorizeResponseSuccess() {
        final AuthorizeResponse result = testObj.handlePendingPaymentResponse(pendingResponseMock, benefitPayPaymentInfoMock);

        assertFalse(result.getIsRedirect());
        assertFalse(result.getIsDataRequired());
        assertTrue(result.getIsSuccess());
        assertNull(result.getRedirectUrl());

        final InOrder inOrder = inOrder(benefitPayPaymentInfoMock, paymentInfoServiceMock);
        inOrder.verify(benefitPayPaymentInfoMock).setPaymentId(PAYMENT_ID);
        inOrder.verify(paymentInfoServiceMock).addQRCodeDataToBenefitPaymentInfo(benefitPayPaymentInfoMock, QR_CODE_VALUE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void handlePendingPaymentResponse_WhenPaymentResponseIdIsNull_ShouldThrowException() {
        when(getPaymentResponseMock.getId()).thenReturn(null);

        testObj.handlePendingPaymentResponse(pendingResponseMock, apmPaymentInfoMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void handlePendingPaymentResponse_WhenResponseIsNull_ShouldThrowException() {
        testObj.handlePendingPaymentResponse(null, apmPaymentInfoMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void handlePendingPaymentResponse_WhenResponseIsNotSupported_ShouldThrowException() {
        when(getPaymentResponseMock.getSource()).thenReturn(cardSourceResponseMock);

        testObj.handlePendingPaymentResponse(pendingResponseMock, apmPaymentInfoMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void handlePendingPaymentResponse_WhenPaymentInfoIsNull_ShouldThrowException() {
        testObj.handlePendingPaymentResponse(pendingResponseMock, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void handlePendingPaymentResponse_WhenPaymentInfoIsNotSupported_ShouldThrowException() {
        testObj.handlePendingPaymentResponse(pendingResponseMock, paymentInfoMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void handlePendingPaymentResponse_WhenPaymentResponseMissingId_ShouldThrowException() {
        when(pendingResponseMock.getId()).thenReturn(null);

        testObj.handlePendingPaymentResponse(pendingResponseMock, apmPaymentInfoMock);
    }

    @Test
    public void handlePendingPaymentResponse_WhenIntegrationErrorShouldReturnAuthorizeResponseNotSuccess() {
        when(pendingResponseMock.getId()).thenReturn(PAYMENT_ID);
        when(checkoutComPaymentIntegrationServiceMock.getPaymentDetails(PAYMENT_ID)).thenThrow(new CheckoutComPaymentIntegrationException("Exception"));

        final AuthorizeResponse result = testObj.handlePendingPaymentResponse(pendingResponseMock, benefitPayPaymentInfoMock);

        assertFalse(result.getIsRedirect());
        assertFalse(result.getIsDataRequired());
        assertFalse(result.getIsSuccess());
        assertNull(result.getRedirectUrl());
    }
}