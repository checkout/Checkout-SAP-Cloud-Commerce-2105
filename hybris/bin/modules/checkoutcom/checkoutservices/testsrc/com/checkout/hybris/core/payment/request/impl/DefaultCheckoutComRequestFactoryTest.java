package com.checkout.hybris.core.payment.request.impl;

import com.checkout.common.Phone;
import com.checkout.hybris.core.address.strategies.CheckoutComPhoneNumberStrategy;
import com.checkout.hybris.core.currency.services.CheckoutComCurrencyService;
import com.checkout.hybris.core.enums.MadaBin;
import com.checkout.hybris.core.enums.PaymentActionType;
import com.checkout.hybris.core.merchant.services.CheckoutComMerchantConfigurationService;
import com.checkout.hybris.core.model.CheckoutComCreditCardPaymentInfoModel;
import com.checkout.hybris.core.payment.request.mappers.CheckoutComPaymentRequestStrategyMapper;
import com.checkout.hybris.core.payment.request.strategies.impl.CheckoutComCardPaymentRequestStrategy;
import com.checkout.hybris.core.payment.request.strategies.impl.CheckoutComMadaPaymentRequestStrategy;
import com.checkout.hybris.core.payment.resolvers.CheckoutComPaymentTypeResolver;
import com.checkout.hybris.core.url.services.CheckoutComUrlService;
import com.checkout.payments.*;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.cms2.servicelayer.services.CMSSiteService;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.enumeration.EnumerationService;
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
import static java.util.Collections.emptyList;
import static java.util.Optional.of;
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
    private static final String TOKEN = "TOKEN";
    private static final String CUSTOMER_EMAIL = "email@email.com";
    private static final String CART_REFERENCE = "CART_REFERENCE";
    private static final String PHONE_NUMBER = "213423423";
    private static final String CHECKOUT_COM_PAYMENT_REDIRECT_PAYMENT_SUCCESS = "/checkout/payment/checkout-com/redirect-response/success";
    private static final String CHECKOUT_COM_PAYMENT_REDIRECT_PAYMENT_FAILURE = "/checkout/payment/checkout-com/redirect-response/failure";
    private static final String SITE_ID = "SITE_ID";
    private static final String TOWN = "Town";
    private static final String LINE_1 = "Line 1";
    private static final String BUILD_VERSION_CONFIG = "build.version";
    private static final String CHECKOUTSERVICES_CONNECTOR_VERSION_CONFIG = "checkoutservices.connector.version";
    private static final String CONNECTOR_VERSION = "v1.2.3";
    private static final String HYBRIS_VERSION = "1905.4";
    private static final String DEFAULT_BUILD_VERSION = "develop";
    private static final String UDF5_KEY = "udf5";
    private static final String CARD_BIN = "123456";

    @InjectMocks
    private DefaultCheckoutComRequestFactory testObj;

    @Mock
    private CartModel cartModelMock;
    @Mock
    private CheckoutComCreditCardPaymentInfoModel paymentInfoMock;
    @Mock
    private EnumerationService enumerationServiceMock;
    @Mock
    private MadaBin madaBin1Mock, madaBin2Mock;
    @Mock
    private CMSSiteService cmsSiteServiceMock;
    @Mock
    private CheckoutComMerchantConfigurationService checkoutComMerchantConfigurationServiceMock;
    @Mock
    private CheckoutComCurrencyService checkoutComCurrencyServiceMock;
    @Mock
    private CustomerModel customerModelMock;
    @Mock
    private AddressModel cartAddressModelMock;
    @Mock
    private CurrencyModel currencyModelMock;
    @Mock
    private CheckoutComPhoneNumberStrategy checkoutComPhoneNumberStrategyMock;
    @Mock
    private Phone phoneMock;
    @Mock
    private CheckoutComUrlService checkoutComUrlServiceMock;
    @Mock
    private CMSSiteModel currentSiteMock;
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
        setUpCart();
        setUpAddress();
        setUpConfiguration();
        when(checkoutComPaymentRequestStrategyMapperMock.findStrategy(CARD)).thenReturn(checkoutComCardPaymentRequestStrategyMock);
        when(checkoutComCardPaymentRequestStrategyMock.createPaymentRequest(cartModelMock)).thenReturn(paymentRequestMock);
        when(checkoutComMadaPaymentRequestStrategyMock.createPaymentRequest(cartModelMock)).thenReturn(paymentRequestMock);
        when(paymentRequestMock.getMetadata()).thenReturn(metadataMap);
        when(checkoutComPaymentRequestStrategyMapperMock.findStrategy(MADA)).thenReturn(checkoutComMadaPaymentRequestStrategyMock);
        when(enumerationServiceMock.getEnumerationValues(MadaBin.class)).thenReturn(emptyList());
        when(madaBin1Mock.getCode()).thenReturn(CARD_BIN);
        when(madaBin2Mock.getCode()).thenReturn("7891011");
        when(paymentInfoMock.getCardBin()).thenReturn(CARD_BIN);
        when(paymentInfoMock.getCardToken()).thenReturn(TOKEN);
        when(currencyModelMock.getIsocode()).thenReturn(GBP);
        when(customerModelMock.getContactEmail()).thenReturn(CUSTOMER_EMAIL);
    }

    private void setUpConfiguration() {
        when(checkoutComMerchantConfigurationServiceMock.getPaymentAction()).thenReturn(PaymentActionType.AUTHORIZE);
        when(checkoutComCurrencyServiceMock.convertAmountIntoPennies(GBP, TOTAL_PRICE)).thenReturn(CHECKOUT_COM_TOTAL_PRICE);
        when(checkoutComMerchantConfigurationServiceMock.isAttemptNoThreeDSecure()).thenReturn(true);
        when(checkoutComMerchantConfigurationServiceMock.isThreeDSEnabled()).thenReturn(true);
        when(configurationMock.getString(BUILD_VERSION_CONFIG)).thenReturn(HYBRIS_VERSION);
        when(configurationServiceMock.getConfiguration()).thenReturn(configurationMock);
        when(checkoutComUrlServiceMock.getFullUrl(CHECKOUT_COM_PAYMENT_REDIRECT_PAYMENT_SUCCESS, true)).thenReturn(CHECKOUT_COM_PAYMENT_REDIRECT_PAYMENT_SUCCESS);
        when(configurationMock.getString(CHECKOUTSERVICES_CONNECTOR_VERSION_CONFIG, DEFAULT_BUILD_VERSION)).thenReturn(CONNECTOR_VERSION);
        when(checkoutComUrlServiceMock.getFullUrl(CHECKOUT_COM_PAYMENT_REDIRECT_PAYMENT_FAILURE, true)).thenReturn(CHECKOUT_COM_PAYMENT_REDIRECT_PAYMENT_FAILURE);
        when(cmsSiteServiceMock.getCurrentSite()).thenReturn(currentSiteMock);
        when(currentSiteMock.getUid()).thenReturn(SITE_ID);
    }

    private void setUpAddress() {
        when(checkoutComPhoneNumberStrategyMock.createPhone(cartAddressModelMock)).thenReturn(of(phoneMock));
        when(cartAddressModelMock.getLine1()).thenReturn(LINE_1);
        when(cartAddressModelMock.getTown()).thenReturn(TOWN);
        when(phoneMock.getNumber()).thenReturn(PHONE_NUMBER);
    }

    private void setUpCart() {
        when(cartModelMock.getTotalPrice()).thenReturn(TOTAL_PRICE);
        when(cartModelMock.getUser()).thenReturn(customerModelMock);
        when(cartModelMock.getCheckoutComPaymentReference()).thenReturn(CART_REFERENCE);
        when(cartModelMock.getDeliveryAddress()).thenReturn(cartAddressModelMock);
        when(cartModelMock.getPaymentAddress()).thenReturn(cartAddressModelMock);
        when(cartModelMock.getCurrency()).thenReturn(currencyModelMock);
        when(cartModelMock.getPaymentInfo()).thenReturn(paymentInfoMock);
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
