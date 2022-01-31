package com.checkout.hybris.facades.payment.impl;

import com.checkout.hybris.core.certificate.services.CheckoutComCertificateService;
import com.checkout.hybris.core.merchant.services.CheckoutComMerchantConfigurationService;
import com.checkout.hybris.core.model.CheckoutComCreditCardPaymentInfoModel;
import com.checkout.hybris.core.payment.exception.CheckoutComPaymentIntegrationException;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentIntegrationService;
import com.checkout.hybris.facades.beans.WalletPaymentAdditionalAuthInfo;
import com.checkout.hybris.facades.beans.WalletPaymentInfoData;
import com.checkout.hybris.facades.payment.token.request.converters.mappers.CheckoutComMappedPaymentTokenRequestConverter;
import com.checkout.payments.GetPaymentResponse;
import com.checkout.payments.ResponseSource;
import com.checkout.tokens.TokenResponse;
import com.checkout.tokens.WalletTokenRequest;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.order.CartService;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import org.apache.commons.configuration.Configuration;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.Optional;

import static com.checkout.hybris.facades.enums.WalletPaymentType.APPLEPAY;
import static com.checkout.hybris.facades.payment.impl.DefaultCheckoutComPaymentFacade.APPLE_PAY_KEY_STORE_ALIAS_PROPERTY_KEY;
import static com.checkout.hybris.facades.payment.impl.DefaultCheckoutComPaymentFacade.APPLE_PAY_KEY_STORE_PWD_PROPERTY_KEY;
import static org.junit.Assert.*;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultCheckoutComPaymentFacadeTest {

    private static final String CKO_SESSION_ID = "cko-session-id";
    private static final String CART_REFERENCE = "CART_REFERENCE";
    private static final String OTHER_REFERENCE = "OTHER_REFERENCE";
    private static final String APPLE_PAY_PRIVATE_KEY = "applePayPrivateKey";
    private static final String APPLE_PAY_CERTIFICATE = "applePayCertificate";
    private static final String TOKEN_VALUE = "token_value";

    @InjectMocks
    private DefaultCheckoutComPaymentFacade testObj;

    @Mock
    private CartService cartServiceMock;
    @Mock
    private CheckoutComPaymentIntegrationService checkoutComPaymentIntegrationServiceMock;
    @Mock
    private CartModel cartModelMock;
    @Mock
    private CheckoutComCreditCardPaymentInfoModel checkoutComCreditCardPaymentInfoModelMock;
    @Mock
    private GetPaymentResponse getPaymentResponse;
    @Mock
    private ResponseSource paymentSourceMock;
    @Mock
    private CheckoutComCertificateService checkoutComCertificateServiceMock;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private CheckoutComMerchantConfigurationService checkoutComMerchantConfigurationServiceMock;
    @Mock
    private X509Certificate applePayCertificateMock;
    @Mock
    private RSAPrivateKey applePayPrivateKeyMock;
    @Mock
    private WalletPaymentAdditionalAuthInfo walletPaymentAdditionalAuthInfoMock;
    @Mock
    private WalletTokenRequest walletTokenRequestMock;
    @Mock
    private CheckoutComMappedPaymentTokenRequestConverter checkoutComMappedPaymentTokenRequestConverterMock;
    @Mock
    private TokenResponse tokenResponseMock;
    @Mock
    private ConfigurationService configurationServiceMock;
    @Mock
    private Configuration configurationMock;

    @Before
    public void setUp() {
        when(configurationServiceMock.getConfiguration()).thenReturn(configurationMock);
        when(configurationMock.getString(APPLE_PAY_KEY_STORE_ALIAS_PROPERTY_KEY)).thenReturn("applePay");
        when(configurationMock.getString(APPLE_PAY_KEY_STORE_PWD_PROPERTY_KEY)).thenReturn("changeit");
        when(cartServiceMock.getSessionCart()).thenReturn(cartModelMock);
        when(cartServiceMock.hasSessionCart()).thenReturn(true);
        when(cartModelMock.getCheckoutComPaymentReference()).thenReturn(CART_REFERENCE);
        when(checkoutComPaymentIntegrationServiceMock.getPaymentDetails(CKO_SESSION_ID)).thenReturn(getPaymentResponse);
        when(getPaymentResponse.getReference()).thenReturn(CART_REFERENCE);
        when(getPaymentResponse.isApproved()).thenReturn(true);
        when(checkoutComPaymentIntegrationServiceMock.generateWalletPaymentToken(walletTokenRequestMock)).thenReturn(tokenResponseMock);
        when(tokenResponseMock.getToken()).thenReturn(TOKEN_VALUE);
        when(tokenResponseMock.getType()).thenReturn(APPLEPAY.name());
        when(checkoutComMappedPaymentTokenRequestConverterMock.convertWalletTokenRequest(walletPaymentAdditionalAuthInfoMock, APPLEPAY)).thenReturn(walletTokenRequestMock);
    }

    @Test
    public void doesSessionCartMatchAuthorizedCart_WhenPaymentDetailsIsNull_ShouldReturnFalse() {
        final boolean result = testObj.doesSessionCartMatchAuthorizedCart(null);

        assertFalse(result);
    }

    @Test
    public void doesSessionCartMatchAuthorizedCart_WhenNoCartInSession_ShouldReturnFalse() {
        when(cartServiceMock.hasSessionCart()).thenReturn(false);

        final boolean result = testObj.doesSessionCartMatchAuthorizedCart(getPaymentResponse);

        assertFalse(result);
    }

    @Test
    public void doesSessionCartMatchAuthorizedCart_WhenTheCartDoesNotMatch_ShouldReturnFalse() {
        when(getPaymentResponse.getReference()).thenReturn(OTHER_REFERENCE);

        final boolean result = testObj.doesSessionCartMatchAuthorizedCart(getPaymentResponse);

        assertFalse(result);
    }

    @Test
    public void doesSessionCartMatchAuthorizedCart_WhenTheCartMatches_ShouldReturnTrue() {
        when(getPaymentResponse.getSource()).thenReturn(paymentSourceMock);
        when(cartModelMock.getPaymentInfo()).thenReturn(checkoutComCreditCardPaymentInfoModelMock);

        final boolean result = testObj.doesSessionCartMatchAuthorizedCart(getPaymentResponse);

        final InOrder inOrder = inOrder(cartServiceMock);
        inOrder.verify(cartServiceMock).hasSessionCart();
        inOrder.verify(cartServiceMock).getSessionCart();
        assertTrue(result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getPaymentDetailsByCkoSessionId_WhenCkoSessionIdIsNull_ShouldThrowException() {
        testObj.getPaymentDetailsByCkoSessionId(null);
    }

    @Test(expected = CheckoutComPaymentIntegrationException.class)
    public void getPaymentDetailsByCkoSessionId_WhenThereIsPaymentIntegrationError_ShouldThrowCheckoutComException() {
        when(checkoutComPaymentIntegrationServiceMock.getPaymentDetails(CKO_SESSION_ID)).thenThrow(new CheckoutComPaymentIntegrationException("Error"));

        testObj.getPaymentDetailsByCkoSessionId(CKO_SESSION_ID);
    }

    @Test
    public void getPaymentDetailsByCkoSessionId_WhenPaymentNotFound_ShouldReturnAnEmptyOptional() {
        when(checkoutComPaymentIntegrationServiceMock.getPaymentDetails(CKO_SESSION_ID)).thenReturn(null);

        final Optional<GetPaymentResponse> result = testObj.getPaymentDetailsByCkoSessionId(CKO_SESSION_ID);

        assertTrue(result.isEmpty());
    }

    @Test
    public void getPaymentDetailsByCkoSessionId_WhenPaymentFound_ShouldReturnAnThePaymentDetails() {
        final Optional<GetPaymentResponse> result = testObj.getPaymentDetailsByCkoSessionId(CKO_SESSION_ID);

        assertTrue(result.isPresent());
        assertEquals(getPaymentResponse, result.get());
        assertEquals(CART_REFERENCE, result.get().getReference());
        assertTrue(result.get().isApproved());
    }

    @Test
    public void createApplePayConnectionFactory_ShouldCreateAnSSLConnectionFactoryForApplePay() throws Exception {
        when(checkoutComMerchantConfigurationServiceMock.getApplePayConfiguration().getPrivateKey()).thenReturn(APPLE_PAY_PRIVATE_KEY);
        when(checkoutComMerchantConfigurationServiceMock.getApplePayConfiguration().getCertificate()).thenReturn(APPLE_PAY_CERTIFICATE);
        when(checkoutComCertificateServiceMock.cleanupCertificate(APPLE_PAY_CERTIFICATE)).thenReturn(APPLE_PAY_CERTIFICATE);
        when(checkoutComCertificateServiceMock.cleanupPrivateKey(APPLE_PAY_PRIVATE_KEY)).thenReturn(APPLE_PAY_PRIVATE_KEY);
        when(checkoutComCertificateServiceMock.generateX509Certificate(APPLE_PAY_CERTIFICATE)).thenReturn(applePayCertificateMock);
        when(checkoutComCertificateServiceMock.generatePrivateKey(APPLE_PAY_PRIVATE_KEY)).thenReturn(applePayPrivateKeyMock);

        final KeyStore applePayKeyStore = KeyStore.getInstance("JKS");
        applePayKeyStore.load(null, null);

        when(checkoutComCertificateServiceMock.generateKeyStore("applePay", "changeit", applePayPrivateKeyMock, applePayCertificateMock)).thenReturn(applePayKeyStore);

        final SSLConnectionSocketFactory result = testObj.createApplePayConnectionFactory();

        assertNotNull(result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createApplePayConnectionFactory_WhenApplePayConfigurationNull_ShouldThrowException() {
        when(checkoutComMerchantConfigurationServiceMock.getApplePayConfiguration()).thenReturn(null);

        testObj.createApplePayConnectionFactory();
    }

    @Test(expected = IllegalArgumentException.class)
    public void createCheckoutComWalletPaymentToken_WhenWalletPaymentAdditionalAuthInfoIsNull_ShouldThrowException() {
        testObj.createCheckoutComWalletPaymentToken(null, APPLEPAY);

    }

    @Test(expected = IllegalArgumentException.class)
    public void createCheckoutComWalletPaymentToken_WhenWalletPaymentTypeIsNull_ShouldThrowException() {
        testObj.createCheckoutComWalletPaymentToken(walletPaymentAdditionalAuthInfoMock, null);
    }

    @Test(expected = CheckoutComPaymentIntegrationException.class)
    public void createCheckoutComWalletPaymentToken_WhenIntegrationError_ShouldThrowException() {
        when(checkoutComPaymentIntegrationServiceMock.generateWalletPaymentToken(walletTokenRequestMock)).thenThrow(new CheckoutComPaymentIntegrationException("Exception"));

        testObj.createCheckoutComWalletPaymentToken(walletPaymentAdditionalAuthInfoMock, APPLEPAY);
    }

    @Test(expected = CheckoutComPaymentIntegrationException.class)
    public void createCheckoutComWalletPaymentToken_WhenResponseTokenNull_ShouldThrowException() {
        when(checkoutComPaymentIntegrationServiceMock.generateWalletPaymentToken(walletTokenRequestMock)).thenReturn(null);

        testObj.createCheckoutComWalletPaymentToken(walletPaymentAdditionalAuthInfoMock, APPLEPAY);
    }

    @Test
    public void createCheckoutComWalletPaymentToken_WhenEverythingIsCorrect_ShouldReturnTheWalletPaymentInfoData() {
        final WalletPaymentInfoData result = testObj.createCheckoutComWalletPaymentToken(walletPaymentAdditionalAuthInfoMock, APPLEPAY);

        assertEquals(TOKEN_VALUE, result.getToken());
        assertEquals(APPLEPAY.name(), result.getType());
    }
}