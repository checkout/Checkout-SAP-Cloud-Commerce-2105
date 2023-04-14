package com.checkout.hybris.core.merchant.services.impl;

import com.checkout.hybris.core.enums.EnvironmentType;
import com.checkout.hybris.core.enums.PaymentActionType;
import com.checkout.hybris.core.merchant.services.CheckoutComMerchantConfigurationService;
import com.checkout.hybris.core.merchantconfiguration.BillingDescriptor;
import com.checkout.hybris.core.model.CheckoutComACHConfigurationModel;
import com.checkout.hybris.core.model.CheckoutComApplePayConfigurationModel;
import com.checkout.hybris.core.model.CheckoutComGooglePayConfigurationModel;
import com.checkout.hybris.core.model.CheckoutComKlarnaConfigurationModel;
import com.checkout.hybris.core.model.CheckoutComMerchantConfigurationModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.site.BaseSiteService;
import org.apache.commons.lang3.StringUtils;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Default implementation of the {@link CheckoutComMerchantConfigurationService}
 */
public class DefaultCheckoutComMerchantConfigurationService implements CheckoutComMerchantConfigurationService {

    protected final BaseSiteService baseSiteService;

    public DefaultCheckoutComMerchantConfigurationService(final BaseSiteService baseSiteService) {
        this.baseSiteService = baseSiteService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSecretKey() {
        final CheckoutComMerchantConfigurationModel currentConfiguration = getCurrentConfiguration();
        return currentConfiguration.getUseNas() ? currentConfiguration.getNasSecretKey() : currentConfiguration.getSecretKey();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSignatureKey() {
        return getCurrentConfiguration().getNasSignatureKey();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAuthorizationKey() {
        return getCurrentConfiguration().getNasAuthorisationHeaderKey();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isNasUsed() {
        return getCurrentConfiguration().getUseNas();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isNasAuthorisationHeaderUsedOnNotificationValidation() {
        return getCurrentConfiguration().getUseNasAuthorisationKeyOnNotifications();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isNasSignatureKeyUsedOnNotificationValidation() {
        return getCurrentConfiguration().getUseNasSignatureKeyOnNotifications();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAbcSignatureKeyUsedOnNotificationValidation() {
        return getCurrentConfiguration().getUseAbcSignatureKeyOnNotifications();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPublicKey() {
        final CheckoutComMerchantConfigurationModel currentConfiguration = getCurrentConfiguration();
        return currentConfiguration.getUseNas() ? currentConfiguration.getNasPublicKey() : currentConfiguration.getPublicKey();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPublicKeyForSite(final String siteId) {
        final CheckoutComMerchantConfigurationModel currentConfiguration = getConfigurationForSiteId(siteId);
        return currentConfiguration.getUseNas() ? currentConfiguration.getNasPublicKey() : currentConfiguration.getPublicKey();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPrivateSharedKey() {
        final CheckoutComMerchantConfigurationModel currentConfiguration = getCurrentConfiguration();
        return currentConfiguration.getUseNas() ? currentConfiguration.getNasSignatureKey() : currentConfiguration.getPrivateSharedKey();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EnvironmentType getEnvironment() {
        return getCurrentConfiguration().getEnvironment();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EnvironmentType getEnvironmentForSite(final String siteId) {
        return getConfigurationForSiteId(siteId).getEnvironment();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PaymentActionType getPaymentAction() {
        return getCurrentConfiguration().getPaymentAction();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isReviewTransactionsAtRisk(final String siteId) {
        return getConfigurationForSiteId(siteId).getReviewTransactionsAtRisk();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isThreeDSEnabled() {
        return getCurrentConfiguration().getThreeDSEnabled();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAttemptNoThreeDSecure() {
        return getCurrentConfiguration().getNoThreeDSAttempt();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAutoCapture() {
        return PaymentActionType.AUTHORIZE_AND_CAPTURE.equals(getCurrentConfiguration().getPaymentAction());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BillingDescriptor getBillingDescriptor() {
        final CheckoutComMerchantConfigurationModel configuration = getCurrentConfiguration();
        final BillingDescriptor billingDescriptor = new BillingDescriptor();
        billingDescriptor.setIncludeBillingDescriptor(configuration.getIncludeBillingDescriptor());
        billingDescriptor.setBillingDescriptorName(configuration.getBillingDescriptorName());
        billingDescriptor.setBillingDescriptorCity(configuration.getBillingDescriptorCity());
        return billingDescriptor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CheckoutComApplePayConfigurationModel getApplePayConfiguration() {
        final CheckoutComMerchantConfigurationModel configuration = getCurrentConfiguration();
        return configuration.getApplePayConfiguration();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CheckoutComGooglePayConfigurationModel getGooglePayConfiguration() {
        final CheckoutComMerchantConfigurationModel configuration = getCurrentConfiguration();
        return configuration.getGooglePayConfiguration();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CheckoutComACHConfigurationModel getACHConfiguration() {
        final CheckoutComMerchantConfigurationModel configuration = getCurrentConfiguration();
        return configuration.getAchConfiguration();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CheckoutComKlarnaConfigurationModel getKlarnaConfiguration() {
        final CheckoutComMerchantConfigurationModel configuration = getCurrentConfiguration();
        return configuration.getKlarnaConfiguration();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Double getAuthorisationAmountValidationThreshold(final String siteId) {
        final Double authorisationAmountValidationTolerance = getConfigurationForSiteId(siteId).getAuthorisationAmountValidationThreshold();
        return authorisationAmountValidationTolerance != null ? authorisationAmountValidationTolerance : Double.valueOf(0.0d);
    }

    protected CheckoutComMerchantConfigurationModel getCurrentConfiguration() {
        final BaseSiteModel currentBaseSite = baseSiteService.getCurrentBaseSite();
        checkArgument(currentBaseSite != null, "Current base site cannot be null");

        return currentBaseSite.getCheckoutComMerchantConfiguration();
    }

    protected CheckoutComMerchantConfigurationModel getConfigurationForSiteId(final String siteId) {
        checkArgument(StringUtils.isNotBlank(siteId), "Site id is null.");
        final BaseSiteModel baseSite = baseSiteService.getBaseSiteForUID(siteId);
        checkArgument(baseSite != null, "Base site is null for id " + siteId);

        return baseSite.getCheckoutComMerchantConfiguration();
    }

}
