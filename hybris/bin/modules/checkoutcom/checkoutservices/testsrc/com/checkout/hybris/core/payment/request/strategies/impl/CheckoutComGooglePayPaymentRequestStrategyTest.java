package com.checkout.hybris.core.payment.request.strategies.impl;

import com.checkout.common.Address;
import com.checkout.hybris.core.merchant.services.CheckoutComMerchantConfigurationService;
import com.checkout.hybris.core.model.CheckoutComGooglePayPaymentInfoModel;
import com.checkout.payments.PaymentRequest;
import com.checkout.payments.RequestSource;
import com.checkout.payments.TokenSource;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import de.hybris.platform.core.model.user.AddressModel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.GOOGLEPAY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComGooglePayPaymentRequestStrategyTest {

    private static final String CURRENCY_ISO_CODE = "USD";
    private static final Long CHECKOUT_COM_TOTAL_PRICE = 10000L;
    private static final String PAYMENT_TOKEN_VALUE = "payment_token_value";

    @Spy
    @InjectMocks
    private CheckoutComGooglePayPaymentRequestStrategy testObj;

    @Mock
    private CartModel cartMock;
    @Mock
    private PaymentInfoModel paymentInfoMock;
    @Mock
    private CheckoutComGooglePayPaymentInfoModel googlePayPaymentInfoMock;
    @Mock
    private Address addressMock;
    @Mock
    private AddressModel addressModelMock;
    @Mock
    private CheckoutComMerchantConfigurationService checkoutComMerchantConfigurationServiceMock;

    @Before
    public void setUp() {
        when(cartMock.getPaymentInfo()).thenReturn(googlePayPaymentInfoMock);
        when(googlePayPaymentInfoMock.getToken()).thenReturn(PAYMENT_TOKEN_VALUE);
        when(cartMock.getPaymentAddress()).thenReturn(addressModelMock);
        doReturn(addressMock).when(testObj).createAddress(addressModelMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getRequestSourcePaymentRequest_WhenPaymentInfoIsNotCheckoutComGooglePayPaymentInfo_ShouldThrowException() {
        when(cartMock.getPaymentInfo()).thenReturn(paymentInfoMock);

        testObj.getRequestSourcePaymentRequest(cartMock, CURRENCY_ISO_CODE, CHECKOUT_COM_TOTAL_PRICE);
    }

    @Test
    public void getRequestSourcePaymentRequest_WhenCheckoutComGooglePayPaymentInfo_ShouldReturnPaymentRequest() {
        final PaymentRequest<RequestSource> result = testObj.getRequestSourcePaymentRequest(cartMock, CURRENCY_ISO_CODE, CHECKOUT_COM_TOTAL_PRICE);

        assertEquals(PAYMENT_TOKEN_VALUE, ((TokenSource) result.getSource()).getToken());
        assertEquals(CURRENCY_ISO_CODE, result.getCurrency());
        assertEquals(CHECKOUT_COM_TOTAL_PRICE, result.getAmount());
        assertEquals(addressMock, ((TokenSource) result.getSource()).getBillingAddress());
    }

    @Test
    public void getStrategyKey_WhenGooglePay_ShouldReturnGooglePayType() {
        assertEquals(GOOGLEPAY, testObj.getStrategyKey());
    }

    @Test
    public void isCapture_ShouldReturnConfiguredValue() {
        when(checkoutComMerchantConfigurationServiceMock.isAutoCapture()).thenReturn(Boolean.TRUE);

        final Optional<Boolean> result = testObj.isCapture();

        assertTrue(result.get());
    }
}