package com.checkout.hybris.core.payment.request.strategies.impl;

import com.checkout.common.Address;
import com.checkout.hybris.core.merchant.services.CheckoutComMerchantConfigurationService;
import com.checkout.hybris.core.model.CheckoutComCreditCardPaymentInfoModel;
import com.checkout.payments.*;
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

import static com.checkout.hybris.core.enums.PaymentActionType.AUTHORIZE;
import static com.checkout.hybris.core.enums.PaymentActionType.AUTHORIZE_AND_CAPTURE;
import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.CARD;
import static java.util.Collections.emptyMap;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComCardPaymentRequestStrategyTest {

    private static final String SUBSCRIPTION_ID = "subscriptionId";
    private static final String CARD_TOKEN = "CARD_TOKEN";
    private static final String CURRENCY_ISO_CODE = "USD";
    private static final Long CHECKOUT_COM_TOTAL_PRICE = 10000L;

    @Spy
    @InjectMocks
    private CheckoutComCardPaymentRequestStrategy testObj;

    @Mock
    private CheckoutComMerchantConfigurationService checkoutComMerchantConfigurationServiceMock;
    @Mock
    private PaymentRequest<RequestSource> paymentRequestMock;
    @Mock
    private CartModel cartMock;
    @Mock
    private PaymentInfoModel paymentInfoMock;
    @Mock
    private CheckoutComCreditCardPaymentInfoModel checkoutComPaymentInfoMock;
    @Mock
    private Address addressMock;
    @Mock
    private AddressModel addressModelMock;

    @Before
    public void setUp() {
        when(checkoutComMerchantConfigurationServiceMock.getPaymentAction()).thenReturn(AUTHORIZE_AND_CAPTURE);
        when(checkoutComMerchantConfigurationServiceMock.isThreeDSEnabled()).thenReturn(true);
        when(checkoutComMerchantConfigurationServiceMock.isAttemptNoThreeDSecure()).thenReturn(true);
        when(cartMock.getPaymentInfo()).thenReturn(checkoutComPaymentInfoMock);
        when(cartMock.getPaymentAddress()).thenReturn(addressModelMock);
        when(checkoutComPaymentInfoMock.isSaved()).thenReturn(true);
        when(checkoutComPaymentInfoMock.getSubscriptionId()).thenReturn(SUBSCRIPTION_ID);
        when(checkoutComPaymentInfoMock.getCardToken()).thenReturn(CARD_TOKEN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getRequestSourcePaymentRequest_WhenPaymentInfoIsNotCheckoutComCreditCard_ShouldThrowException() {
        when(cartMock.getPaymentInfo()).thenReturn(paymentInfoMock);

        testObj.getRequestSourcePaymentRequest(cartMock, CURRENCY_ISO_CODE, CHECKOUT_COM_TOTAL_PRICE);
    }

    @Test
    public void getRequestSourcePaymentRequest_WhenCardSavedAndSubscriptionIdPopulated_ShouldCreateIdSourcePaymentRequest() {
        final PaymentRequest<RequestSource> result = testObj.getRequestSourcePaymentRequest(cartMock, CURRENCY_ISO_CODE, CHECKOUT_COM_TOTAL_PRICE);

        assertEquals(SUBSCRIPTION_ID, ((IdSource) result.getSource()).getId());
        assertEquals(CURRENCY_ISO_CODE, result.getCurrency());
        assertEquals(CHECKOUT_COM_TOTAL_PRICE, result.getAmount());
    }

    @Test
    public void getRequestSourcePaymentRequest_WhenNoSavedCard_ShouldCreateTokenSourcePaymentRequest() {
        when(checkoutComPaymentInfoMock.isSaved()).thenReturn(false);
        doReturn(addressMock).when(testObj).createAddress(addressModelMock);

        final PaymentRequest<RequestSource> result = testObj.getRequestSourcePaymentRequest(cartMock, CURRENCY_ISO_CODE, CHECKOUT_COM_TOTAL_PRICE);

        assertEquals(CARD_TOKEN, ((TokenSource) result.getSource()).getToken());
        assertEquals(CURRENCY_ISO_CODE, result.getCurrency());
        assertEquals(CHECKOUT_COM_TOTAL_PRICE, result.getAmount());
        assertEquals(addressMock, ((TokenSource) result.getSource()).getBillingAddress());
    }

    @Test
    public void populateRequestMetadata_WhenCardPayment_ThenDefaultMetadataIsSet() {
        doReturn(emptyMap()).when(testObj).createGenericMetadata();

        testObj.populateRequestMetadata(paymentRequestMock);

        verify(testObj).createGenericMetadata();
        verify(paymentRequestMock).setMetadata(emptyMap());
    }

    @Test
    public void isCapture_WhenAuthAndCapture_ShouldReturnTrue() {
        assertTrue(testObj.isCapture().get());
    }

    @Test
    public void isCapture_WhenNotAuthAndCapture_ShouldReturnFalse() {
        when(checkoutComMerchantConfigurationServiceMock.getPaymentAction()).thenReturn(AUTHORIZE);

        assertFalse(testObj.isCapture().get());
    }

    @Test
    public void createThreeDSRequest_WhenStandard_ShouldCreateThreeDSFromConfiguration() {
        final Optional<ThreeDSRequest> result = testObj.createThreeDSRequest();

        assertTrue(result.isPresent());
        assertTrue(result.get().isEnabled());
        assertTrue(result.get().getAttemptN3D());
    }

    @Test
    public void getStrategyKey_WhenStandardCard_ShouldReturnCardType() {
        assertEquals(CARD, testObj.getStrategyKey());
    }
}
