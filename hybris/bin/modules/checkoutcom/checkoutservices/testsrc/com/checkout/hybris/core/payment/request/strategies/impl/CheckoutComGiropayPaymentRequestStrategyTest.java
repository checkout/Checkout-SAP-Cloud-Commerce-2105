package com.checkout.hybris.core.payment.request.strategies.impl;

import com.checkout.hybris.core.model.CheckoutComAPMPaymentInfoModel;
import com.checkout.payments.AlternativePaymentSource;
import com.checkout.payments.PaymentRequest;
import com.checkout.payments.RequestSource;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.CartModel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.GIROPAY;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComGiropayPaymentRequestStrategyTest {

    private static final String CURRENCY_ISO_CODE = "USD";
    private static final Long CHECKOUT_COM_TOTAL_PRICE = 10000L;
    private static final String PURPOSE_KEY = "purpose";
    private static final String PAYMENT_REFERENCE_VALUE = "payment-reference";

    @InjectMocks
    private CheckoutComGiropayPaymentRequestStrategy testObj;

    @Mock
    private CartModel cartMock;
    @Mock
    private CheckoutComAPMPaymentInfoModel checkoutComAPMPaymentInfoMock;

    @Before
    public void setUp() {
        when(cartMock.getPaymentInfo()).thenReturn(checkoutComAPMPaymentInfoMock);
        when(cartMock.getCheckoutComPaymentReference()).thenReturn(PAYMENT_REFERENCE_VALUE);
        when(checkoutComAPMPaymentInfoMock.getType()).thenReturn(GIROPAY.name());
    }

    @Test
    public void getStrategyKey_WhenGiropay_ShouldReturnGiropayType() {
        assertEquals(GIROPAY, testObj.getStrategyKey());
    }

    @Test
    public void getRequestSourcePaymentRequest_WhenGiropayPayment_ShouldCreateAlternativePaymentRequestWithTypeAndPurpose() {
        final PaymentRequest<RequestSource> result = testObj.getRequestSourcePaymentRequest(cartMock, CURRENCY_ISO_CODE, CHECKOUT_COM_TOTAL_PRICE);

        assertEquals(GIROPAY.name().toLowerCase(), result.getSource().getType());
        assertEquals(PAYMENT_REFERENCE_VALUE, ((AlternativePaymentSource) result.getSource()).get(PURPOSE_KEY));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getRequestSourcePaymentRequest_WhenGiropayPaymentButPaymentReferenceIsBlank_ShouldThrowException() {
        when(cartMock.getCheckoutComPaymentReference()).thenReturn("");

        testObj.getRequestSourcePaymentRequest(cartMock, CURRENCY_ISO_CODE, CHECKOUT_COM_TOTAL_PRICE);
    }
}
