package com.checkout.hybris.core.payment.response.strategies.impl;

import com.checkout.common.Link;
import com.checkout.hybris.core.authorisation.AuthorizeResponse;
import com.checkout.hybris.core.model.CheckoutComAPMPaymentInfoModel;
import com.checkout.hybris.core.model.CheckoutComCreditCardPaymentInfoModel;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentInfoService;
import com.checkout.payments.PaymentPending;
import de.hybris.bootstrap.annotations.UnitTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultCheckoutComPaymentResponseStrategyTest {

    private static final String REDIRECT_LINK = "https://test.com";
    private static final String PAYMENT_ID = "paymentId";

    @InjectMocks
    private DefaultCheckoutComPaymentResponseStrategy testObj;

    @Mock
    private PaymentPending pendingResponseMock;
    @Mock
    private Link linkMock;
    @Mock
    private CheckoutComAPMPaymentInfoModel apmPaymentInfoMock;
    @Mock
    private CheckoutComCreditCardPaymentInfoModel paymentInfoMock;
    @Mock
    private CheckoutComPaymentInfoService paymentInfoServiceMock;

    @Before
    public void setUp() {
        when(paymentInfoMock.getItemtype()).thenReturn(CheckoutComCreditCardPaymentInfoModel._TYPECODE);
        when(apmPaymentInfoMock.getUserDataRequired()).thenReturn(false);
        when(apmPaymentInfoMock.getItemtype()).thenReturn(CheckoutComAPMPaymentInfoModel._TYPECODE);
        when(pendingResponseMock.getRedirectLink()).thenReturn(linkMock);
        when(linkMock.getHref()).thenReturn(REDIRECT_LINK);
        when(pendingResponseMock.getId()).thenReturn(PAYMENT_ID);
    }

    @Test
    public void getRedirectUrl_WheGenericApm_ShouldReturnAuthorizeResponseCorrectlyPopulated() {
        final AuthorizeResponse result = testObj.handlePendingPaymentResponse(pendingResponseMock, apmPaymentInfoMock);

        verify(paymentInfoServiceMock).addPaymentId(PAYMENT_ID, apmPaymentInfoMock);
        assertFalse(result.getIsDataRequired());
        assertTrue(result.getIsRedirect());
        assertTrue(result.getIsSuccess());
        assertEquals(REDIRECT_LINK, result.getRedirectUrl());
    }

    @Test
    public void getRedirectUrl_WheCardPayment_ShouldReturnAuthorizeResponseCorrectlyPopulated() {
        final AuthorizeResponse result = testObj.handlePendingPaymentResponse(pendingResponseMock, paymentInfoMock);

        verify(paymentInfoServiceMock).addPaymentId(PAYMENT_ID, paymentInfoMock);
        assertTrue(result.getIsDataRequired());
        assertTrue(result.getIsRedirect());
        assertTrue(result.getIsSuccess());
        assertEquals(REDIRECT_LINK, result.getRedirectUrl());
    }


    @Test(expected = IllegalArgumentException.class)
    public void getRedirectUrl_WhenPendingResponseNull_ShouldThrowException() {
        testObj.handlePendingPaymentResponse(null, apmPaymentInfoMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getRedirectUrl_WhenPaymentInfoIsNull_ShouldThrowException() {
        testObj.handlePendingPaymentResponse(pendingResponseMock, null);
    }
}
