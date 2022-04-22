package com.checkout.hybris.core.payment.request.strategies.impl;

import com.checkout.common.Address;
import com.checkout.hybris.core.merchant.services.CheckoutComMerchantConfigurationService;
import com.checkout.hybris.core.model.CheckoutComGooglePayPaymentInfoModel;
import com.checkout.payments.PaymentRequest;
import com.checkout.payments.RequestSource;
import com.checkout.payments.ThreeDSRequest;
import com.checkout.payments.TokenSource;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import de.hybris.platform.core.model.user.AddressModel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.GOOGLEPAY;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

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
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private CheckoutComMerchantConfigurationService checkoutComMerchantConfigurationServiceMock;

    @Before
    public void setUp() {
        when(cartMock.getPaymentInfo()).thenReturn(googlePayPaymentInfoMock);
        when(googlePayPaymentInfoMock.getToken()).thenReturn(PAYMENT_TOKEN_VALUE);
        when(cartMock.getPaymentAddress()).thenReturn(addressModelMock);
        doReturn(addressMock).when(testObj).createAddress(addressModelMock);
        when(checkoutComMerchantConfigurationServiceMock.getGooglePayConfiguration().getThreeDSEnabled()).thenReturn(Boolean.TRUE);
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

    @Test
    public void createThreeDSRequest_WhenThreeDSEnabled_ShouldCreateThreeDSFromConfiguration() {
        final Optional<ThreeDSRequest> result = testObj.createThreeDSRequest();

        assertTrue(result.isPresent());
        assertTrue(result.get().isEnabled());
    }
}
