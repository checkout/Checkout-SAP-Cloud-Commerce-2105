package com.checkout.hybris.core.payment.request.strategies.impl;

import com.checkout.hybris.core.model.CheckoutComAPMPaymentInfoModel;
import com.checkout.hybris.core.model.CheckoutComMerchantConfigurationModel;
import com.checkout.payments.AlternativePaymentSource;
import com.checkout.payments.PaymentRequest;
import com.checkout.payments.RequestSource;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.order.CartModel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.QPAY;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComQPayPaymentRequestStrategyTest {

    private static final String CURRENCY_ISO_CODE = "USD";
    private static final Long CHECKOUT_COM_TOTAL_PRICE = 10000L;
    private static final String DESCRIPTION_KEY = "description";
    private static final String ELETRONICS_MERCHANT_CODE = "eletronics";

    @InjectMocks
    private CheckoutComQPayPaymentRequestStrategy testObj;

    @Mock
    private CartModel cartMock;
    @Mock
    private CheckoutComAPMPaymentInfoModel checkoutComAPMPaymentInfoMock;
    @Mock
    private BaseSiteModel siteMock;
    @Mock
    private CheckoutComMerchantConfigurationModel merchantConfigurationMock;

    @Before
    public void setUp() {
        when(cartMock.getPaymentInfo()).thenReturn(checkoutComAPMPaymentInfoMock);
        when(cartMock.getSite()).thenReturn(siteMock);
        when(siteMock.getCheckoutComMerchantConfiguration()).thenReturn(merchantConfigurationMock);
        when(merchantConfigurationMock.getCode()).thenReturn(ELETRONICS_MERCHANT_CODE);
        when(checkoutComAPMPaymentInfoMock.getType()).thenReturn(QPAY.name());
    }

    @Test
    public void getStrategyKey_WhenQPay_ShouldReturnQPayType() {
        assertEquals(QPAY, testObj.getStrategyKey());
    }

    @Test
    public void getRequestSourcePaymentRequest_WhenQPayPayment_ShouldCreateAlternativePaymentRequestWithTypeAndDescription() {
        final PaymentRequest<RequestSource> result = testObj.getRequestSourcePaymentRequest(cartMock, CURRENCY_ISO_CODE, CHECKOUT_COM_TOTAL_PRICE);

        assertEquals(QPAY.name().toLowerCase(), result.getSource().getType());
        assertEquals(ELETRONICS_MERCHANT_CODE, ((AlternativePaymentSource) result.getSource()).get(DESCRIPTION_KEY));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getRequestSourcePaymentRequest_WhenQPayPaymentButSiteIsNull_ShouldThrowException() {
        when(cartMock.getSite()).thenReturn(null);

        testObj.getRequestSourcePaymentRequest(cartMock, CURRENCY_ISO_CODE, CHECKOUT_COM_TOTAL_PRICE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getRequestSourcePaymentRequest_WhenQPayPaymentButMerchantConfigIsNull_ShouldThrowException() {
        when(siteMock.getCheckoutComMerchantConfiguration()).thenReturn(null);

        testObj.getRequestSourcePaymentRequest(cartMock, CURRENCY_ISO_CODE, CHECKOUT_COM_TOTAL_PRICE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getRequestSourcePaymentRequest_WhenQPayPaymentButMerchantConfigCodeIsEmpty_ShouldThrowException() {
        when(merchantConfigurationMock.getCode()).thenReturn("");

        testObj.getRequestSourcePaymentRequest(cartMock, CURRENCY_ISO_CODE, CHECKOUT_COM_TOTAL_PRICE);
    }
}
