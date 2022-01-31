package com.checkout.hybris.core.payment.response.strategies.impl;

import com.checkout.common.Link;
import com.checkout.hybris.core.authorisation.AuthorizeResponse;
import com.checkout.hybris.core.model.CheckoutComAPMPaymentInfoModel;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentInfoService;
import com.checkout.payments.PaymentPending;
import com.google.common.collect.ImmutableMap;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.MULTIBANCO;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComMultibancoPaymentResponseStrategyTest {

    private static final String MULTIBANCO_REDIRECT_LINK_KEY = "multibanco:static-reference-page";
    private static final String REDIRECT_LINK = "https://test.com";
    private static final String PAYMENT_ID = "paymentId";

    @InjectMocks
    private CheckoutComMultibancoPaymentResponseStrategy testObj;

    @Mock
    private PaymentPending pendingResponseMock;
    @Mock
    private Link linkMock;
    @Mock
    private CheckoutComAPMPaymentInfoModel apmPaymentInfoMock;
    @Mock
    private PaymentInfoModel paymentInfoMock;
    @Mock
    private CheckoutComPaymentInfoService paymentInfoServiceMock;

    @Before
    public void setUp() {
        when(apmPaymentInfoMock.getUserDataRequired()).thenReturn(false);
        when(pendingResponseMock.getLinks()).thenReturn(ImmutableMap.of(MULTIBANCO_REDIRECT_LINK_KEY, linkMock));
        when(linkMock.getHref()).thenReturn(REDIRECT_LINK);
        when(pendingResponseMock.getId()).thenReturn(PAYMENT_ID);
        doNothing().when(paymentInfoServiceMock).addPaymentId(PAYMENT_ID, apmPaymentInfoMock);
    }

    @Test
    public void getStrategyKey_ShouldReturnMultibancoPaymentType() {
        assertEquals(MULTIBANCO, testObj.getStrategyKey());
    }

    @Test
    public void getRedirectUrl_WhenMultibanco_ShouldReturnAuthorizeResponseCorrectlyPopulated() {
        final AuthorizeResponse result = testObj.handlePendingPaymentResponse(pendingResponseMock, apmPaymentInfoMock);

        verify(paymentInfoServiceMock).addPaymentId(PAYMENT_ID, apmPaymentInfoMock);
        assertFalse(result.getIsDataRequired());
        assertTrue(result.getIsRedirect());
        assertTrue(result.getIsSuccess());
        assertEquals(REDIRECT_LINK, result.getRedirectUrl());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getRedirectUrl_WhenResponseIsNull_ShouldThrowException() {
        testObj.handlePendingPaymentResponse(null, apmPaymentInfoMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getRedirectUrl_WhenPaymentInfoNotSupported_ShouldThrowException() {
        testObj.handlePendingPaymentResponse(pendingResponseMock, paymentInfoMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getRedirectUrl_WhenPaymentInfoIsNull_ShouldThrowException() {
        testObj.handlePendingPaymentResponse(pendingResponseMock, null);
    }
}