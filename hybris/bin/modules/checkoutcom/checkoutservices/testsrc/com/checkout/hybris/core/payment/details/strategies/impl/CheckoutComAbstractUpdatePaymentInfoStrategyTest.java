package com.checkout.hybris.core.payment.details.strategies.impl;

import com.checkout.payments.GetPaymentResponse;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import de.hybris.platform.order.CartService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComAbstractUpdatePaymentInfoStrategyTest {

    private static final String PAYMENT_ID = "paymentId";

    @Spy
    private CheckoutComAbstractUpdatePaymentInfoStrategy testObj;

    @Mock
    private CartService cartServiceMock;
    @Mock
    private GetPaymentResponse paymentResponseMock;
    @Mock
    private CartModel cartMock;
    @Mock
    private PaymentInfoModel paymentInfoMock;

    @Before
    public void setUp() {
        testObj = Mockito.mock(
                CheckoutComAbstractUpdatePaymentInfoStrategy.class,
                Mockito.CALLS_REAL_METHODS);

        ReflectionTestUtils.setField(testObj, "cartService", cartServiceMock);
        when(cartServiceMock.getSessionCart()).thenReturn(cartMock);
        when(cartServiceMock.hasSessionCart()).thenReturn(true);
        when(cartMock.getPaymentInfo()).thenReturn(paymentInfoMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void processPaymentResponse_WhenSessionDoesNotHaveCart_ShouldThrowException() {
        when(cartServiceMock.hasSessionCart()).thenReturn(false);

        testObj.processPaymentResponse(paymentResponseMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void processPaymentResponse_WhenPaymentInfoNull_ShouldThrowException() {
        when(paymentResponseMock.getId()).thenReturn(PAYMENT_ID);
        when(cartMock.getPaymentInfo()).thenReturn(null);

        testObj.processPaymentResponse(paymentResponseMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void processPaymentResponse_WhenPaymentResponseIsNull_ShouldThrowException() {
        testObj.processPaymentResponse(null);
    }

}
