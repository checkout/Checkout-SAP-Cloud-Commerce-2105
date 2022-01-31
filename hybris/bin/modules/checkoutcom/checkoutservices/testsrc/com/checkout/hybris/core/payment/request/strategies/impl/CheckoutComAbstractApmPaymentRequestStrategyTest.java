package com.checkout.hybris.core.payment.request.strategies.impl;

import com.checkout.hybris.core.model.CheckoutComAPMPaymentInfoModel;
import com.checkout.hybris.core.model.CheckoutComCreditCardPaymentInfoModel;
import com.checkout.payments.PaymentRequest;
import com.checkout.payments.RequestSource;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.CartModel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.SOFORT;
import static java.util.Collections.emptyMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComAbstractApmPaymentRequestStrategyTest {

    private static final String CURRENCY_ISO_CODE = "USD";
    private static final Long CHECKOUT_COM_TOTAL_PRICE = 10000L;

    private CheckoutComAbstractApmPaymentRequestStrategy testObj;

    @Mock
    private CartModel cartMock;
    @Mock
    private CheckoutComCreditCardPaymentInfoModel checkoutComPaymentInfoMock;
    @Mock
    private CheckoutComAPMPaymentInfoModel checkoutComRedirectAPMPaymentInfoMock;
    @Mock
    private PaymentRequest<RequestSource> paymentRequestMock;

    @Before
    public void setUp() {
        testObj = Mockito.mock(
                CheckoutComAbstractApmPaymentRequestStrategy.class,
                Mockito.CALLS_REAL_METHODS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getRequestSourcePaymentRequest_WhenPaymentInfoIsNotApmPaymentInfo_ShouldThrowException() {
        when(cartMock.getPaymentInfo()).thenReturn(checkoutComPaymentInfoMock);

        testObj.getRequestSourcePaymentRequest(cartMock, CURRENCY_ISO_CODE, CHECKOUT_COM_TOTAL_PRICE);
    }

    @Test
    public void getRequestSourcePaymentRequest_WhenGenericAPMPayment_ShouldCreateAlternativePaymentRequestWithType() {
        when(cartMock.getPaymentInfo()).thenReturn(checkoutComRedirectAPMPaymentInfoMock);
        when(checkoutComRedirectAPMPaymentInfoMock.getType()).thenReturn(SOFORT.name());

        final PaymentRequest<RequestSource> result = testObj.getRequestSourcePaymentRequest(cartMock, CURRENCY_ISO_CODE, CHECKOUT_COM_TOTAL_PRICE);

        assertEquals(SOFORT.name().toLowerCase(), result.getSource().getType());
    }

    @Test
    public void isCapture_WhenApmRequest_ThenReturnNull() {
        assertTrue(testObj.isCapture().isEmpty());
    }

    @Test
    public void createThreeDSRequest_WhenApmRequest_ThenResultIsEmpty() {
        assertTrue(testObj.createThreeDSRequest().isEmpty());
    }

    @Test
    public void populateRequestMetadata_WhenApmPayment_ThenDefaultMetadataIsSet() {
        doReturn(emptyMap()).when(testObj).createGenericMetadata();

        testObj.populateRequestMetadata(paymentRequestMock);

        verify(testObj).createGenericMetadata();
        verify(paymentRequestMock).setMetadata(emptyMap());
    }

    @Test(expected = IllegalArgumentException.class)
    public void populateRequestMetadata_WhenPaymentInfoIsNull_ShouldThrowException() {
        when(cartMock.getPaymentInfo()).thenReturn(null);

        testObj.getRequestSourcePaymentRequest(cartMock, CURRENCY_ISO_CODE, CHECKOUT_COM_TOTAL_PRICE);
    }
}
