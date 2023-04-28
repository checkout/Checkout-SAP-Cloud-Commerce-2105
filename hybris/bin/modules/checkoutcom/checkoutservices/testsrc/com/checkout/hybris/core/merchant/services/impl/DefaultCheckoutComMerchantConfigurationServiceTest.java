package com.checkout.hybris.core.merchant.services.impl;

import com.checkout.hybris.core.enums.EnvironmentType;
import com.checkout.hybris.core.enums.PaymentActionType;
import com.checkout.hybris.core.merchantconfiguration.BillingDescriptor;
import com.checkout.hybris.core.model.*;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.site.BaseSiteService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultCheckoutComMerchantConfigurationServiceTest {

    private static final String SECRET_KEY = "secretKey";
    private static final String PUBLIC_KEY = "publicKey";
    private static final String PRIVATE_SHARED_KEY = "privateSharedKey";
    private static final String NAS_SECRET_KEY = "nasSecretKey";
    private static final String NAS_PUBLIC_KEY = "nasPublicKey";
    private static final String NAS_AUTH_HEADER_KEY = "nasAuthHeaderKey";
    private static final String NAS_SIGNATURE_KEY = "signatureKey";
    private static final String TEST = "test";
    private static final String AUTHORIZE = "authorize";
    private static final String BASE_SITE_ID = "base-site-id";
    private static final String BILLING_DESCRIPTOR_NAME = "billingDescriptorName";
    private static final String BILLING_DESCRIPTOR_CITY = "billingDescriptorCity";

    @InjectMocks
    private DefaultCheckoutComMerchantConfigurationService testObj;

    @Mock
    private BaseSiteService baseSiteServiceMock;

    @Mock
    private BaseSiteModel baseSiteMock;
    @Mock
    private CheckoutComKlarnaConfigurationModel klarnaConfigMock;
    @Mock
    private CheckoutComApplePayConfigurationModel applePayConfigMock;
    @Mock
    private CheckoutComGooglePayConfigurationModel googlePayConfigMock;
    @Mock
    private CheckoutComMerchantConfigurationModel merchantConfigurationMock;
    @Mock
    private CheckoutComACHConfigurationModel achConfigurationMock;

    @Before
    public void setUp() {
        testObj = new DefaultCheckoutComMerchantConfigurationService(baseSiteServiceMock);

        when(baseSiteServiceMock.getCurrentBaseSite()).thenReturn(baseSiteMock);
        when(baseSiteServiceMock.getBaseSiteForUID(BASE_SITE_ID)).thenReturn(baseSiteMock);
        when(baseSiteMock.getCheckoutComMerchantConfiguration()).thenReturn(merchantConfigurationMock);
    }

    @Test
    public void getSecretKey_ShouldReturnTheValueAsExpected() {
        when(merchantConfigurationMock.getSecretKey()).thenReturn(SECRET_KEY);

        final String result = testObj.getSecretKey();

        assertEquals(SECRET_KEY, result);
    }

    @Test
    public void getPublicKey_ShouldReturnTheValueAsExpected() {
        when(merchantConfigurationMock.getPublicKey()).thenReturn(PUBLIC_KEY);

        final String result = testObj.getPublicKey();

        assertEquals(PUBLIC_KEY, result);
    }

    @Test
    public void getPublicKeyForSite_ShouldReturnTheValueAsExpected() {
        when(merchantConfigurationMock.getPublicKey()).thenReturn(PUBLIC_KEY);

        final String result = testObj.getPublicKeyForSite(BASE_SITE_ID);

        assertEquals(PUBLIC_KEY, result);
    }

    @Test
    public void getPrivateSharedKey_ShouldReturnTheValueAsExpected() {
        when(merchantConfigurationMock.getPrivateSharedKey()).thenReturn(PRIVATE_SHARED_KEY);

        final String result = testObj.getPrivateSharedKey();

        assertEquals(PRIVATE_SHARED_KEY, result);
    }

    @Test
    public void getSecretKey_WhenNASEnabled_ShouldReturnTheValueAsExpected() {
        when(merchantConfigurationMock.getUseNas()).thenReturn(Boolean.TRUE);
        when(merchantConfigurationMock.getNasSecretKey()).thenReturn(NAS_SECRET_KEY);

        final String result = testObj.getSecretKey();

        assertEquals(NAS_SECRET_KEY, result);
    }

    @Test
    public void getPublicKey_WhenNASEnabled_ShouldReturnTheValueAsExpected() {
        when(merchantConfigurationMock.getUseNas()).thenReturn(Boolean.TRUE);
        when(merchantConfigurationMock.getNasPublicKey()).thenReturn(NAS_PUBLIC_KEY);

        final String result = testObj.getPublicKey();

        assertEquals(NAS_PUBLIC_KEY, result);
    }

    @Test
    public void getPublicKeyForSite_WhenNASEnabled_ShouldReturnTheValueAsExpected() {
        when(merchantConfigurationMock.getUseNas()).thenReturn(Boolean.TRUE);
        when(merchantConfigurationMock.getNasPublicKey()).thenReturn(NAS_PUBLIC_KEY);

        final String result = testObj.getPublicKeyForSite(BASE_SITE_ID);

        assertEquals(NAS_PUBLIC_KEY, result);
    }

    @Test
    public void getPrivateSharedKey_WhenNASEnabled_ShouldReturnTheValueAsExpected() {
        when(merchantConfigurationMock.getUseNas()).thenReturn(Boolean.TRUE);
        when(merchantConfigurationMock.getNasSignatureKey()).thenReturn(NAS_SIGNATURE_KEY);

        final String result = testObj.getPrivateSharedKey();

        assertEquals(NAS_SIGNATURE_KEY, result);
    }

    @Test
    public void getCurrentMerchantConfig_ShouldReturnTheValueAsExpected() {
        final CheckoutComMerchantConfigurationModel result = testObj.getCurrentConfiguration();

        assertSame(merchantConfigurationMock, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getCurrentMerchantConfig_WhenSiteNull_ShouldThrowException() {
        when(baseSiteServiceMock.getCurrentBaseSite()).thenReturn(null);

        testObj.getCurrentConfiguration();
    }

    @Test(expected = IllegalArgumentException.class)
    public void getConfigurationForSiteId_WhenSiteIdNull_ShouldThrowException() {
        testObj.getConfigurationForSiteId(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getConfigurationForSiteId_WhenSiteNull_ShouldThrowException() {
        when(baseSiteServiceMock.getBaseSiteForUID(BASE_SITE_ID)).thenReturn(null);

        testObj.getConfigurationForSiteId(BASE_SITE_ID);
    }

    @Test
    public void getConfigurationForSiteId_ShouldReturnTheValueAsExpected() {
        final CheckoutComMerchantConfigurationModel result = testObj.getConfigurationForSiteId(BASE_SITE_ID);

        assertSame(merchantConfigurationMock, result);
    }

    @Test
    public void getEnvironment_ShouldReturnTheValueAsExpected() {
        when(merchantConfigurationMock.getEnvironment()).thenReturn(EnvironmentType.TEST);

        final EnvironmentType result = testObj.getEnvironment();

        assertEquals(TEST, result.getCode());
    }

    @Test
    public void getEnvironmentForSite_ShouldReturnTheValueAsExpected() {
        when(merchantConfigurationMock.getEnvironment()).thenReturn(EnvironmentType.TEST);

        final EnvironmentType result = testObj.getEnvironmentForSite(BASE_SITE_ID);

        assertEquals(TEST, result.getCode());
    }

    @Test
    public void getPaymentAction_ShouldReturnTheValueAsExpected() {
        when(merchantConfigurationMock.getPaymentAction()).thenReturn(PaymentActionType.AUTHORIZE);

        final PaymentActionType result = testObj.getPaymentAction();

        assertEquals(AUTHORIZE, result.getCode());
    }

    @Test
    public void isThreeDSEnabled_ShouldReturnTheValueAsExpected() {
        when(merchantConfigurationMock.getThreeDSEnabled()).thenReturn(true);

        final boolean result = testObj.isThreeDSEnabled();

        assertTrue(result);
    }

    @Test
    public void isAttemptNoThreeDSecure_ShouldReturnTheValueAsExpected() {
        when(merchantConfigurationMock.getNoThreeDSAttempt()).thenReturn(false);

        final boolean result = testObj.isAttemptNoThreeDSecure();

        assertFalse(result);
    }

    @Test
    public void isAutoCapture_WhenAuthorize_ShouldReturnFalse() {
        when(merchantConfigurationMock.getPaymentAction()).thenReturn(PaymentActionType.AUTHORIZE);

        final boolean result = testObj.isAutoCapture();

        assertFalse(result);
    }

    @Test
    public void isAutoCapture_WhenAuthorizeAndCapture_ShouldReturnTrue() {
        when(merchantConfigurationMock.getPaymentAction()).thenReturn(PaymentActionType.AUTHORIZE_AND_CAPTURE);

        final boolean result = testObj.isAutoCapture();

        assertTrue(result);
    }

    @Test
    public void isReviewTransactionsAtRisk_ShouldReturnTheValueAsExpected() {
        when(merchantConfigurationMock.getReviewTransactionsAtRisk()).thenReturn(true);

        assertTrue(testObj.isReviewTransactionsAtRisk(BASE_SITE_ID));
    }

    @Test
    public void getBillingDescriptor_ShouldReturnConfiguredBillingDescriptor() {
        when(merchantConfigurationMock.getIncludeBillingDescriptor()).thenReturn(Boolean.FALSE);
        when(merchantConfigurationMock.getBillingDescriptorName()).thenReturn(BILLING_DESCRIPTOR_NAME);
        when(merchantConfigurationMock.getBillingDescriptorCity()).thenReturn(BILLING_DESCRIPTOR_CITY);

        final BillingDescriptor result = testObj.getBillingDescriptor();

        assertEquals(Boolean.FALSE, result.getIncludeBillingDescriptor());
        assertEquals(BILLING_DESCRIPTOR_NAME, result.getBillingDescriptorName());
        assertEquals(BILLING_DESCRIPTOR_CITY, result.getBillingDescriptorCity());
    }

    @Test
    public void getAuthorisationAmountValidationThreshold_WhenSiteIdGivenAndValueExists_ShouldReturnTheValueAsExpected() {
        when(merchantConfigurationMock.getAuthorisationAmountValidationThreshold()).thenReturn(10.0d);

        final Double result = testObj.getAuthorisationAmountValidationThreshold(BASE_SITE_ID);

        assertEquals(Double.valueOf(10.0d), result);
    }

    @Test
    public void getAuthorisationAmountValidationThreshold_WhenSiteIdGivenAndValueDoesNotExists_ShouldReturnZero() {
        when(merchantConfigurationMock.getAuthorisationAmountValidationThreshold()).thenReturn(null);

        final Double result = testObj.getAuthorisationAmountValidationThreshold(BASE_SITE_ID);

        assertEquals(Double.valueOf(0.0d), result);
    }

    @Test
    public void getApplePayConfiguration_ShouldReturnTheApplePayConfiguration() {
        when(merchantConfigurationMock.getApplePayConfiguration()).thenReturn(applePayConfigMock);

        final CheckoutComApplePayConfigurationModel result = testObj.getApplePayConfiguration();

        assertEquals(applePayConfigMock, result);
    }

    @Test
    public void getGooglePayConfiguration_ShouldReturnTheGooglePayConfiguration() {
        when(merchantConfigurationMock.getGooglePayConfiguration()).thenReturn(googlePayConfigMock);

        final CheckoutComGooglePayConfigurationModel result = testObj.getGooglePayConfiguration();

        assertEquals(googlePayConfigMock, result);
    }

    @Test
    public void getKlarnaConfiguration_ShouldReturnTheKlarnaConfiguration() {
        when(merchantConfigurationMock.getKlarnaConfiguration()).thenReturn(klarnaConfigMock);

        final CheckoutComKlarnaConfigurationModel result = testObj.getKlarnaConfiguration();

        assertEquals(klarnaConfigMock, result);
    }

    @Test
    public void getSignatureKey_shouldReturnNASSignatureKey() {
        when(merchantConfigurationMock.getNasSignatureKey()).thenReturn(NAS_SIGNATURE_KEY);

        final String result = testObj.getSignatureKey();

        assertEquals(NAS_SIGNATURE_KEY, result);
    }

    @Test
    public void getNasAuthorizationKey_shouldReturnNASAuthorizationKey() {
        when(merchantConfigurationMock.getNasAuthorisationHeaderKey()).thenReturn(NAS_AUTH_HEADER_KEY);

        final String result = testObj.getAuthorizationKey();

        assertEquals(NAS_AUTH_HEADER_KEY, result);
    }

    @Test
    public void isNasUsed_shouldReturnConfigurationNASFlag() {
        when(merchantConfigurationMock.getUseNas()).thenReturn(true);

        assertTrue(testObj.isNasUsed());
    }

    @Test
    public void isNasAuthorisationHeaderUsedOnNotificationValidation_shouldReturnConfigurationFlag() {
        when(merchantConfigurationMock.getUseNasAuthorisationKeyOnNotifications()).thenReturn(true);

        assertTrue(testObj.isNasAuthorisationHeaderUsedOnNotificationValidation());
    }

    @Test
    public void isNasSignatureKeyUsedOnNotificationValidation_shouldReturnConfigurationFlag() {
        when(merchantConfigurationMock.getUseNasSignatureKeyOnNotifications()).thenReturn(true);

        assertTrue(testObj.isNasSignatureKeyUsedOnNotificationValidation());
    }

    @Test
    public void isAbcSignatureKeyUsedOnNotificationValidation_shouldReturnConfigurationFlag() {
        when(merchantConfigurationMock.getUseAbcSignatureKeyOnNotifications()).thenReturn(true);

        assertTrue(testObj.isAbcSignatureKeyUsedOnNotificationValidation());
    }

    @Test
    public void getAchConfiguration_shouldReturnAchConfiguration() {
        when(merchantConfigurationMock.getAchConfiguration()).thenReturn(achConfigurationMock);

        final CheckoutComACHConfigurationModel result = testObj.getACHConfiguration();

        assertThat(result).isSameAs(achConfigurationMock);
    }
}
