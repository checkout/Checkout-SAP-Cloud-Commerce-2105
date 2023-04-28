package com.checkout.hybris.core.merchant.services;


import com.checkout.hybris.core.enums.EnvironmentType;
import com.checkout.hybris.core.enums.PaymentActionType;
import com.checkout.hybris.core.merchantconfiguration.BillingDescriptor;
import com.checkout.hybris.core.model.CheckoutComACHConfigurationModel;
import com.checkout.hybris.core.model.CheckoutComApplePayConfigurationModel;
import com.checkout.hybris.core.model.CheckoutComGooglePayConfigurationModel;
import com.checkout.hybris.core.model.CheckoutComKlarnaConfigurationModel;

/**
 * Manages the merchant configurations
 */
public interface CheckoutComMerchantConfigurationService {

    /**
     * Returns the secret key from the merchant configuration of the current site
     *
     * @return the secret key
     */
    String getSecretKey();

    /**
     * Returns the secret key from the merchant configuration of the current site
     *
     * @return the secret key
     */
    String getSignatureKey();

    /**
     * Returns the authorization key from the merchant configuration of the current site. This value
     * is validated on incoming Notification/Events
     *
     * @return the authorization key
     */
    String getAuthorizationKey();

    /**
     * Returns whether Nas is used on the merchant configuration of the current site
     *
     * @return the activated value
     */
    boolean isNasUsed();

    /**
     * Returns whether Nas authorisation header is used on the merchant configuration of the current site to validate
     * incoming notifications
     *
     * @return the activated value
     */
    boolean isNasAuthorisationHeaderUsedOnNotificationValidation();

    /**
     * Returns whether the NAS signature key is used on the merchant configuration of the current site to validate
     * incoming notifications
     *
     * @return {@code True} if enabled, {@code False} otherwise
     */
    boolean isNasSignatureKeyUsedOnNotificationValidation();

    /**
     * Returns whether the ABC signature key is used on the merchant configuration of the current site to validate
     * incoming notifications
     *
     * @return {@code True} if enabled, {@code False} otherwise
     */
    boolean isAbcSignatureKeyUsedOnNotificationValidation();

    /**
     * Returns the secret key from the merchant configuration of the current site
     *
     * @return the public key
     */
    String getPublicKey();

    /**
     * Returns the public key from the merchant configuration of the given site
     *
     * @param siteId the site id for which to get the secret
     * @return the secret key
     */
    String getPublicKeyForSite(String siteId);

    /**
     * Returns the secret key from the merchant configuration of the current site
     *
     * @return the private shared key
     */
    String getPrivateSharedKey();

    /**
     * Returns the environment from the merchant configuration of the current site
     *
     * @return the environment
     */
    EnvironmentType getEnvironment();

    /**
     * Returns the environment from the merchant configuration for a site
     *
     * @param siteId the site id for which get the environment
     * @return the environment
     */
    EnvironmentType getEnvironmentForSite(String siteId);

    /**
     * Returns the payment action from the merchant configuration of the current site
     *
     * @return the payment action
     */
    PaymentActionType getPaymentAction();

    /**
     * Returns whether the transactions at risk should be reviewed or not
     * based on the site configuration
     *
     * @param siteId the site id
     * @return the payment action
     */
    boolean isReviewTransactionsAtRisk(String siteId);

    /**
     * Returns the boolean value set for the merchant configuration of the current site
     *
     * @return true if 3D secure is enabled, false instead
     */
    boolean isThreeDSEnabled();

    /**
     * Returns the boolean value set for the merchant configuration of the current site
     *
     * @return true if attempt no 3D secure is enabled, false instead
     */
    boolean isAttemptNoThreeDSecure();

    /**
     * Returns a flag indicating whether the payment is expected to be automatically captured or not
     *
     * @return true if the configuration is authorize_and_capture, false otherwise
     */
    boolean isAutoCapture();

    /**
     * Returns the Billing Descriptor configuration for the current site
     *
     * @return a {@link BillingDescriptor}
     */
    BillingDescriptor getBillingDescriptor();

    /**
     * Returns the CheckoutComApplePayConfigurationModel configuration for the current site
     *
     * @return a {@link CheckoutComApplePayConfigurationModel}
     */
    CheckoutComApplePayConfigurationModel getApplePayConfiguration();

    /**
     * Returns the CheckoutComGooglePayConfigurationModel configuration for the current site
     *
     * @return a {@link CheckoutComGooglePayConfigurationModel}
     */
    CheckoutComGooglePayConfigurationModel getGooglePayConfiguration();

    /**
     * Returns the CheckoutComACHConfigurationModel configuration for the current site
     *
     * @return a {@link CheckoutComACHConfigurationModel}
     */
    CheckoutComACHConfigurationModel getACHConfiguration();

    /**
     * Returns the threshold used to decide whether an authorisation amount is valid or not
     *
     * @return the tolerance
     */
    Double getAuthorisationAmountValidationThreshold(String siteId);

    /**
     * Returns the CheckoutComKlarnaConfigurationModel configuration for the current site
     *
     * @return a {@link CheckoutComKlarnaConfigurationModel}
     */
    CheckoutComKlarnaConfigurationModel getKlarnaConfiguration();
}
