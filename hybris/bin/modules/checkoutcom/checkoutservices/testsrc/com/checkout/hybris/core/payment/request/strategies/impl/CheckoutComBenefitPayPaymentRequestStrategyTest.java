package com.checkout.hybris.core.payment.request.strategies.impl;

import com.checkout.hybris.core.model.CheckoutComAPMPaymentInfoModel;
import com.checkout.payments.AlternativePaymentSource;
import com.checkout.payments.PaymentRequest;
import com.checkout.payments.RequestSource;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.CartModel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.BENEFITPAY;
import static com.checkout.hybris.core.payment.request.strategies.impl.CheckoutComBenefitPayPaymentRequestStrategy.INTEGRATION_TYPE_SOURCE_KEY;
import static com.checkout.hybris.core.payment.request.strategies.impl.CheckoutComBenefitPayPaymentRequestStrategy.INTEGRATION_TYPE_SOURCE_VALUE;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComBenefitPayPaymentRequestStrategyTest {

    private static final String CURRENCY_ISO_CODE = "USD";
    private static final Long CHECKOUT_COM_TOTAL_PRICE = 10000L;

    @InjectMocks
    private CheckoutComBenefitPayPaymentRequestStrategy testObj;

    @Mock
    private CartModel cartMock;
    @Mock
    private CheckoutComAPMPaymentInfoModel checkoutComAPMPaymentInfoMock;

    @Test
    public void getRequestSourcePaymentRequest_WhenBenefitPayPayment_ShouldCreateAlternativePaymentRequestWithType() {
        when(cartMock.getPaymentInfo()).thenReturn(checkoutComAPMPaymentInfoMock);
        when(checkoutComAPMPaymentInfoMock.getType()).thenReturn(BENEFITPAY.name());

        final PaymentRequest<RequestSource> result = testObj.getRequestSourcePaymentRequest(cartMock, CURRENCY_ISO_CODE, CHECKOUT_COM_TOTAL_PRICE);

        assertEquals(BENEFITPAY.name().toLowerCase(), result.getSource().getType());
        assertEquals(INTEGRATION_TYPE_SOURCE_VALUE, ((AlternativePaymentSource) result.getSource()).get(INTEGRATION_TYPE_SOURCE_KEY));
    }

    @Test
    public void getStrategyKey_WhenBenefitPay_ShouldReturnBenefitType() {
        assertEquals(BENEFITPAY, testObj.getStrategyKey());
    }
}
