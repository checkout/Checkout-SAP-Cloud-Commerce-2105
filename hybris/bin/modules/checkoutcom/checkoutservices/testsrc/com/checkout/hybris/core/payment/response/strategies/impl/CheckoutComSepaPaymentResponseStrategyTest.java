package com.checkout.hybris.core.payment.response.strategies.impl;

import com.checkout.hybris.core.authorisation.AuthorizeResponse;
import com.checkout.hybris.core.model.CheckoutComSepaPaymentInfoModel;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentInfoService;
import com.checkout.payments.PaymentPending;
import de.hybris.bootstrap.annotations.UnitTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.SEPA;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComSepaPaymentResponseStrategyTest {

    private static final String PAYMENT_ID = "paymentId";

    @InjectMocks
    private CheckoutComSepaPaymentResponseStrategy testObj;

    @Mock
    private PaymentPending pendingResponseMock;
    @Mock
    private CheckoutComSepaPaymentInfoModel sepaPaymentInfoMock;
    @Mock
    private CheckoutComPaymentInfoService paymentInfoServiceMock;

    @Before
    public void setUp() {
        when(pendingResponseMock.getId()).thenReturn(PAYMENT_ID);
        doNothing().when(paymentInfoServiceMock).addPaymentId(PAYMENT_ID, sepaPaymentInfoMock);
    }

    @Test
    public void getStrategyKey_ShouldReturnAchPaymentType() {
        assertEquals(SEPA, testObj.getStrategyKey());
    }

    @Test
    public void handlePendingPaymentResponse_WhenSepa_ShouldReturnAuthorizeResponseSuccess() {
        final AuthorizeResponse result = testObj.handlePendingPaymentResponse(pendingResponseMock, sepaPaymentInfoMock);

        verify(paymentInfoServiceMock).addPaymentId(PAYMENT_ID, sepaPaymentInfoMock);
        assertFalse(result.getIsRedirect());
        assertTrue(result.getIsDataRequired());
        assertTrue(result.getIsSuccess());
        assertNull(result.getRedirectUrl());
    }
}