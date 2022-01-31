package com.checkout.hybris.core.payment.response.strategies.impl;

import com.checkout.hybris.core.authorisation.AuthorizeResponse;
import com.checkout.hybris.core.model.CheckoutComFawryPaymentInfoModel;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentInfoService;
import com.checkout.payments.PaymentPending;
import de.hybris.bootstrap.annotations.UnitTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.FAWRY;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComFawryPaymentResponseStrategyTest {

    private static final String PAYMENT_ID = "paymentId";

    @InjectMocks
    private CheckoutComFawryPaymentResponseStrategy testObj;

    @Mock
    private PaymentPending pendingResponseMock;
    @Mock
    private CheckoutComFawryPaymentInfoModel fawryPaymentInfoMock;
    @Mock
    private CheckoutComPaymentInfoService paymentInfoServiceMock;

    @Before
    public void setUp() {
        when(pendingResponseMock.getId()).thenReturn(PAYMENT_ID);
        doNothing().when(paymentInfoServiceMock).addPaymentId(PAYMENT_ID, fawryPaymentInfoMock);
    }

    @Test
    public void getStrategyKey_ShouldReturnFawryPaymentType() {
        assertEquals(FAWRY, testObj.getStrategyKey());
    }

    @Test
    public void handlePendingPaymentResponse_WhenFawry_ShouldReturnAuthorizeResponseSuccess() {
        final AuthorizeResponse result = testObj.handlePendingPaymentResponse(pendingResponseMock, fawryPaymentInfoMock);

        verify(paymentInfoServiceMock).addPaymentId(PAYMENT_ID, fawryPaymentInfoMock);
        assertFalse(result.getIsRedirect());
        assertTrue(result.getIsDataRequired());
        assertTrue(result.getIsSuccess());
        assertNull(result.getRedirectUrl());
    }
}