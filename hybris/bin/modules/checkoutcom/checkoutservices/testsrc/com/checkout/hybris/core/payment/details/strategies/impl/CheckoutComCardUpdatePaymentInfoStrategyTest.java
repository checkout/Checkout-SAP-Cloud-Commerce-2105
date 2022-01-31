package com.checkout.hybris.core.payment.details.strategies.impl;

import com.checkout.hybris.core.model.CheckoutComCreditCardPaymentInfoModel;
import com.checkout.hybris.core.payment.details.mappers.CheckoutComUpdatePaymentInfoStrategyMapper;
import com.checkout.hybris.core.payment.details.strategies.CheckoutComUpdatePaymentInfoStrategy;
import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentInfoService;
import com.checkout.payments.GetPaymentResponse;
import com.checkout.payments.ResponseSource;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.order.CartService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.CARD;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComCardUpdatePaymentInfoStrategyTest {

    @InjectMocks
    @Spy
    private CheckoutComCardUpdatePaymentInfoStrategy testObj;

    @Mock
    private CheckoutComUpdatePaymentInfoStrategyMapper checkoutComUpdatePaymentInfoStrategyMapperMock;
    @Mock
    private GetPaymentResponse paymentResponseMock;
    @Mock
    private CartService cartServiceMock;
    @Mock
    private CheckoutComCreditCardPaymentInfoModel checkoutComCreditCardPaymentInfoModelMock;
    @Mock
    private CheckoutComPaymentInfoService paymentInfoServiceMock;
    @Mock
    private CartModel cartMock;
    @Mock
    private ResponseSource paymentSourceMock;

    @Before
    public void setUp() {
        ReflectionTestUtils.setField(testObj, "cartService", cartServiceMock);
        ReflectionTestUtils.setField(testObj, "paymentInfoService", paymentInfoServiceMock);
        doNothing().when(testObj).callSuperProcessPayment(any(GetPaymentResponse.class));
        when(paymentResponseMock.getSource()).thenReturn(paymentSourceMock);
        when(cartServiceMock.getSessionCart()).thenReturn(cartMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void processPaymentResponse_WhenSourceIsNull_ShouldThrowException() {
        when(paymentResponseMock.getSource()).thenReturn(null);

        testObj.processPaymentResponse(paymentResponseMock);

        verifyNoMoreInteractions(cartServiceMock);
        verifyZeroInteractions(paymentInfoServiceMock);
    }

    @Test
    public void processPaymentResponse_WhenSourceIsValid_ShouldUpdatePaymentInfo() {
        when(cartMock.getPaymentInfo()).thenReturn(checkoutComCreditCardPaymentInfoModelMock);

        testObj.processPaymentResponse(paymentResponseMock);

        verify(cartServiceMock).getSessionCart();
        verify(paymentInfoServiceMock).addSubscriptionIdToUserPayment(checkoutComCreditCardPaymentInfoModelMock, paymentSourceMock);
    }

    @Test
    public void getStrategyKey_WhenStandardCard_ShouldReturnCardType() {
        assertEquals(CARD, testObj.getStrategyKey());
    }

    @Test
    public void registerStrategy_ShouldRegisterTheStrategy() {
        testObj.registerStrategy();

        verify(checkoutComUpdatePaymentInfoStrategyMapperMock).addStrategy(any(CheckoutComPaymentType.class), any(CheckoutComUpdatePaymentInfoStrategy.class));
    }
}
