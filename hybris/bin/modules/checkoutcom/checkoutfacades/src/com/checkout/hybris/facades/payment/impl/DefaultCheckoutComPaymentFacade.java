package com.checkout.hybris.facades.payment.impl;

import com.checkout.hybris.core.certificate.exceptions.CheckoutComCertificateException;
import com.checkout.hybris.core.certificate.services.CheckoutComCertificateService;
import com.checkout.hybris.core.merchant.services.CheckoutComMerchantConfigurationService;
import com.checkout.hybris.core.model.CheckoutComApplePayConfigurationModel;
import com.checkout.hybris.core.payment.exception.CheckoutComPaymentIntegrationException;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentIntegrationService;
import com.checkout.hybris.facades.beans.WalletPaymentAdditionalAuthInfo;
import com.checkout.hybris.facades.beans.WalletPaymentInfoData;
import com.checkout.hybris.facades.enums.WalletPaymentType;
import com.checkout.hybris.facades.payment.CheckoutComPaymentFacade;
import com.checkout.hybris.facades.payment.token.request.converters.mappers.CheckoutComMappedPaymentTokenRequestConverter;
import com.checkout.payments.GetPaymentResponse;
import com.checkout.tokens.TokenResponse;
import com.checkout.tokens.WalletTokenRequest;
import com.google.common.base.Preconditions;
import de.hybris.platform.acceleratorfacades.payment.impl.DefaultPaymentFacade;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.order.CartService;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import org.apache.commons.lang.StringUtils;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.Assert;

import javax.net.ssl.SSLContext;
import java.security.*;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.Optional;

/**
 * Default implementation of the {@link CheckoutComPaymentFacade}
 */
public class DefaultCheckoutComPaymentFacade extends DefaultPaymentFacade implements CheckoutComPaymentFacade {

    protected static final Logger LOG = LogManager.getLogger(DefaultCheckoutComPaymentFacade.class);

    protected static final String APPLE_PAY_KEY_STORE_ALIAS_PROPERTY_KEY = "checkoutcom.applePay.keystore.alias";
    protected static final String APPLE_PAY_KEY_STORE_PWD_PROPERTY_KEY = "checkoutcom.applePay.keystore.password";

    protected final CartService cartService;
    protected final CheckoutComPaymentIntegrationService checkoutComPaymentIntegrationService;
    protected final CheckoutComMerchantConfigurationService checkoutComMerchantConfigurationService;
    protected final CheckoutComCertificateService checkoutComCertificateService;
    protected final CheckoutComMappedPaymentTokenRequestConverter checkoutComMappedPaymentTokenRequestConverter;
    protected final ConfigurationService configurationService;


    public DefaultCheckoutComPaymentFacade(final CartService cartService,
                                           final CheckoutComPaymentIntegrationService checkoutComPaymentIntegrationService,
                                           final CheckoutComMerchantConfigurationService checkoutComMerchantConfigurationService,
                                           final CheckoutComCertificateService checkoutComCertificateService,
                                           final CheckoutComMappedPaymentTokenRequestConverter checkoutComMappedPaymentTokenRequestConverter,
                                           final ConfigurationService configurationService) {
        this.cartService = cartService;
        this.checkoutComPaymentIntegrationService = checkoutComPaymentIntegrationService;
        this.checkoutComMerchantConfigurationService = checkoutComMerchantConfigurationService;
        this.checkoutComCertificateService = checkoutComCertificateService;
        this.checkoutComMappedPaymentTokenRequestConverter = checkoutComMappedPaymentTokenRequestConverter;
        this.configurationService = configurationService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean doesSessionCartMatchAuthorizedCart(final GetPaymentResponse paymentDetails) {
        if (cartService.hasSessionCart()) {
            final CartModel sessionCart = cartService.getSessionCart();
            return paymentDetails != null && paymentDetails.getReference().equalsIgnoreCase(sessionCart.getCheckoutComPaymentReference());
        }

        LOG.error("The current session doesn't have a cart.");
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<GetPaymentResponse> getPaymentDetailsByCkoSessionId(final String ckoSessionId) {
        Preconditions.checkArgument(StringUtils.isNotBlank(ckoSessionId), "cko-session-id session id null.");

        final GetPaymentResponse paymentDetails = checkoutComPaymentIntegrationService.getPaymentDetails(ckoSessionId);
        return paymentDetails != null ? Optional.of(paymentDetails) : Optional.empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SSLConnectionSocketFactory createApplePayConnectionFactory() {
        final CheckoutComApplePayConfigurationModel applePayConfiguration = checkoutComMerchantConfigurationService.getApplePayConfiguration();
        Assert.notNull(applePayConfiguration, "CheckoutComApplePayConfigurationModel cannot be null.");

        final String privateKey = applePayConfiguration.getPrivateKey();
        final String certificate = applePayConfiguration.getCertificate();

        final String cleanedCertificate = checkoutComCertificateService.cleanupCertificate(certificate);
        final String cleanedPrivateKey = checkoutComCertificateService.cleanupPrivateKey(privateKey);

        final X509Certificate generatedCertificate = checkoutComCertificateService.generateX509Certificate(cleanedCertificate);
        final RSAPrivateKey generatedPrivateKey = checkoutComCertificateService.generatePrivateKey(cleanedPrivateKey);

        final String applePayKeyStoreAlias = configurationService.getConfiguration().getString(APPLE_PAY_KEY_STORE_ALIAS_PROPERTY_KEY);
        final String applePayKeyStorePassword = configurationService.getConfiguration().getString(APPLE_PAY_KEY_STORE_PWD_PROPERTY_KEY);
        final KeyStore pkcs12Store = checkoutComCertificateService.generateKeyStore(applePayKeyStoreAlias,
                applePayKeyStorePassword, generatedPrivateKey, generatedCertificate);

        try {
            final SSLContext sslContext = SSLContextBuilder.create()
                    .loadKeyMaterial(pkcs12Store, applePayKeyStorePassword.toCharArray())
                    .loadTrustMaterial(null, new TrustSelfSignedStrategy()).build();
            return new SSLConnectionSocketFactory(sslContext);
        } catch (final NoSuchAlgorithmException | KeyStoreException | UnrecoverableKeyException | KeyManagementException e) {
            throw new CheckoutComCertificateException("Exception while building SSL context", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WalletPaymentInfoData createCheckoutComWalletPaymentToken(final WalletPaymentAdditionalAuthInfo walletPaymentAdditionalAuthInfo,
                                                                     final WalletPaymentType walletPaymentType) {
        Assert.notNull(walletPaymentAdditionalAuthInfo, "WalletPaymentAdditionalAuthInfo cannot be null.");
        Assert.notNull(walletPaymentType, "WalletPaymentType cannot be null.");

        final WalletTokenRequest walletTokenRequest = checkoutComMappedPaymentTokenRequestConverter.convertWalletTokenRequest(walletPaymentAdditionalAuthInfo, walletPaymentType);
        final TokenResponse tokenResponse = checkoutComPaymentIntegrationService.generateWalletPaymentToken(walletTokenRequest);

        if (tokenResponse == null) {
            throw new CheckoutComPaymentIntegrationException(String.format("Error while generating the payment token with Checkout.com for [%s] payment.", walletPaymentType));
        }

        return createWalletPaymentInfoDataResponse(tokenResponse);
    }

    protected WalletPaymentInfoData createWalletPaymentInfoDataResponse(final TokenResponse tokenResponse) {
        final WalletPaymentInfoData walletPaymentTokenData = new WalletPaymentInfoData();
        walletPaymentTokenData.setToken(tokenResponse.getToken());
        walletPaymentTokenData.setType(tokenResponse.getType().toUpperCase());
        return walletPaymentTokenData;
    }
}
