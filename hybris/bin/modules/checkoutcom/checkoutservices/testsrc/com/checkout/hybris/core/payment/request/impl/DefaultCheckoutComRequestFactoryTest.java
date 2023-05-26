package com.checkout.hybris.core.payment.request.impl;

import com.checkout.hybris.core.currency.services.CheckoutComCurrencyService;
import com.checkout.hybris.core.model.CheckoutComCreditCardPaymentInfoModel;
import com.checkout.hybris.core.payment.request.mappers.CheckoutComPaymentRequestStrategyMapper;
import com.checkout.hybris.core.payment.request.strategies.impl.CheckoutComCardPaymentRequestStrategy;
import com.checkout.hybris.core.payment.request.strategies.impl.CheckoutComMadaPaymentRequestStrategy;
import com.checkout.hybris.core.payment.resolvers.CheckoutComPaymentTypeResolver;
import com.checkout.payments.*;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import org.apache.commons.configuration.Configuration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.CARD;
import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.MADA;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultCheckoutComRequestFactoryTest {

    private static final String GBP = "GBP";
    private static final double TOTAL_PRICE = 100D;
    private static final Long CHECKOUT_COM_TOTAL_PRICE = 10000L;
    private static final BigDecimal ORDER_TOTAL_PRICE = new BigDecimal(TOTAL_PRICE);
    private static final String CART_REFERENCE = "CART_REFERENCE";
    private static final String BUILD_VERSION_CONFIG = "build.version";
    private static final String CHECKOUTSERVICES_CONNECTOR_VERSION_CONFIG = "checkoutservices.connector.version";
    private static final String CONNECTOR_VERSION = "v1.2.3";
    private static final String HYBRIS_VERSION = "1905.4";
    private static final String DEFAULT_BUILD_VERSION = "develop";
    private static final String UDF5_KEY = "udf5";

    @InjectMocks
    private DefaultCheckoutComRequestFactory testObj;

    @Mock
    private CartModel cartModelMock;
    @Mock
    private CheckoutComCreditCardPaymentInfoModel paymentInfoMock;
    @Mock
    private CheckoutComCurrencyService checkoutComCurrencyServiceMock;
    @Mock
    private ConfigurationService configurationServiceMock;
    @Mock
    private Configuration configurationMock;
    @Mock
    private CheckoutComCardPaymentRequestStrategy checkoutComCardPaymentRequestStrategyMock;
    @Mock
    private CheckoutComMadaPaymentRequestStrategy checkoutComMadaPaymentRequestStrategyMock;
    @Mock
    private CheckoutComPaymentRequestStrategyMapper checkoutComPaymentRequestStrategyMapperMock;
    @Mock
    private CheckoutComPaymentTypeResolver checkoutComPaymentTypeResolverMock;
    @Mock
    private PaymentRequest<RequestSource> paymentRequestMock;
    private Map<String, Object> metadataMap = new HashMap<>();


    @Before
    public void setUp() {
        setUpConfiguration();
        when(cartModelMock.getPaymentInfo()).thenReturn(paymentInfoMock);
        when(checkoutComPaymentRequestStrategyMapperMock.findStrategy(CARD)).thenReturn(checkoutComCardPaymentRequestStrategyMock);
        when(checkoutComCardPaymentRequestStrategyMock.createPaymentRequest(cartModelMock)).thenReturn(paymentRequestMock);
        when(checkoutComMadaPaymentRequestStrategyMock.createPaymentRequest(cartModelMock)).thenReturn(paymentRequestMock);
        when(paymentRequestMock.getMetadata()).thenReturn(metadataMap);
        when(checkoutComPaymentRequestStrategyMapperMock.findStrategy(MADA)).thenReturn(checkoutComMadaPaymentRequestStrategyMock);
    }

    private void setUpConfiguration() {
        when(checkoutComCurrencyServiceMock.convertAmountIntoPennies(GBP, TOTAL_PRICE)).thenReturn(CHECKOUT_COM_TOTAL_PRICE);
        when(configurationMock.getString(BUILD_VERSION_CONFIG)).thenReturn(HYBRIS_VERSION);
        when(configurationServiceMock.getConfiguration()).thenReturn(configurationMock);
        when(configurationMock.getString(CHECKOUTSERVICES_CONNECTOR_VERSION_CONFIG, DEFAULT_BUILD_VERSION)).thenReturn(CONNECTOR_VERSION);
    }


    @Test(expected = IllegalArgumentException.class)
    public void createPaymentRequest_WhenCartIsNull_ShouldThrowException() {
        testObj.createPaymentRequest(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createPaymentRequest_WhenPaymentInfoIsNull_ShouldThrowException() {
        when(cartModelMock.getPaymentInfo()).thenReturn(null);

        testObj.createPaymentRequest(cartModelMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createCapturePaymentRequest_WhenAmountIsNull_ShouldThrowException() {
        testObj.createCapturePaymentRequest(null, CART_REFERENCE, GBP);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createCapturePaymentRequest_WhenReferenceIsNull_ShouldThrowException() {
        testObj.createCapturePaymentRequest(ORDER_TOTAL_PRICE, null, GBP);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createCapturePaymentRequest_WhenCurrencyIsNull_ShouldThrowException() {
        testObj.createCapturePaymentRequest(ORDER_TOTAL_PRICE, CART_REFERENCE, null);
    }

    @Test
    public void createCapturePaymentRequest_ShouldCreateTheRequestProperly() {
        final CaptureRequest result = testObj.createCapturePaymentRequest(ORDER_TOTAL_PRICE, CART_REFERENCE, GBP);

        assertEquals(CHECKOUT_COM_TOTAL_PRICE, result.getAmount());
        assertEquals(CART_REFERENCE, result.getReference());
        assertEquals("hybris " + HYBRIS_VERSION + " extension " + CONNECTOR_VERSION, result.getMetadata().get(UDF5_KEY));
    }

    @Test(expected = IllegalArgumentException.class)
    public void createRefundPaymentRequest_WhenAmountIsNull_ShouldThrowException() {
        testObj.createRefundPaymentRequest(null, CART_REFERENCE, GBP);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createRefundPaymentRequest_WhenReferenceIsNull_ShouldThrowException() {
        testObj.createRefundPaymentRequest(ORDER_TOTAL_PRICE, null, GBP);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createRefundPaymentRequest_WhenCurrencyIsNull_ShouldThrowException() {
        testObj.createRefundPaymentRequest(ORDER_TOTAL_PRICE, CART_REFERENCE, null);
    }

    @Test
    public void createRefundPaymentRequest_ShouldCreateTheRequestProperly() {
        final RefundRequest result = testObj.createRefundPaymentRequest(ORDER_TOTAL_PRICE, CART_REFERENCE, GBP);

        assertEquals(CHECKOUT_COM_TOTAL_PRICE, result.getAmount());
        assertEquals(CART_REFERENCE, result.getReference());
        assertEquals("hybris " + HYBRIS_VERSION + " extension " + CONNECTOR_VERSION, result.getMetadata().get(UDF5_KEY));
    }

    @Test(expected = IllegalArgumentException.class)
    public void createVoidPaymentRequest_WhenReferenceIsNull_ShouldThrowException() {
        testObj.createVoidPaymentRequest(null);
    }

    @Test
    public void createVoidPaymentRequest_ShouldCreateTheRequestProperly() {
        final VoidRequest result = testObj.createVoidPaymentRequest(CART_REFERENCE);

        assertEquals(CART_REFERENCE, result.getReference());
        assertEquals("hybris " + HYBRIS_VERSION + " extension " + CONNECTOR_VERSION, result.getMetadata().get(UDF5_KEY));
    }

    @Test
    public void createPaymentRequest_WhenMada_shouldCallMadaStrategy() {
        when(checkoutComPaymentTypeResolverMock.resolvePaymentType(paymentInfoMock)).thenReturn(MADA);

        testObj.createPaymentRequest(cartModelMock);

        verify(checkoutComPaymentRequestStrategyMapperMock).findStrategy(MADA);
    }

    @Test
    public void createPaymentRequest_WhenNormalCard_shouldCallCardStrategy() {
        when(checkoutComPaymentTypeResolverMock.resolvePaymentType(paymentInfoMock)).thenReturn(CARD);

        testObj.createPaymentRequest(cartModelMock);

        verify(checkoutComPaymentRequestStrategyMapperMock).findStrategy(CARD);
    }
}
