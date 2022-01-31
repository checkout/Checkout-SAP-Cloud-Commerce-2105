package com.checkout.hybris.facades.payment.converters.populators;

import com.checkout.hybris.facades.beans.*;
import de.hybris.platform.commercefacades.order.CartFacade;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

/**
 * Populates attributes from {@link GooglePaySettingsData}.
 */
public class CheckoutComGooglePayPaymentRequestPopulator implements Populator<GooglePaySettingsData, GooglePayMerchantConfigurationData> {

    protected static final String FORMAT = "FULL";

    protected final Converter<CartData, GooglePayTransactionInfoData> checkoutComGooglePayTransactionInfoConverter;
    protected final CartFacade cartFacade;

    public CheckoutComGooglePayPaymentRequestPopulator(final Converter<CartData, GooglePayTransactionInfoData> checkoutComGooglePayTransactionInfoConverter,
                                                       final CartFacade cartFacade) {
        this.checkoutComGooglePayTransactionInfoConverter = checkoutComGooglePayTransactionInfoConverter;
        this.cartFacade = cartFacade;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public void populate(final GooglePaySettingsData source, final GooglePayMerchantConfigurationData target) throws ConversionException {
        validateParameterNotNull(source, "GooglePaySettingsData cannot be null.");
        validateParameterNotNull(target, "GooglePayPaymentRequest cannot be null.");

        populateBaseCardPaymentMethod(source, target);
        populateClientSettings(source, target);
        target.setGateway(source.getGateway());
        target.setGatewayMerchantId(source.getGatewayMerchantId());
        target.setMerchantName(source.getMerchantName());
        target.setMerchantId(source.getMerchantId());
        target.setTransactionInfo(checkoutComGooglePayTransactionInfoConverter.convert(cartFacade.getSessionCart()));
    }

    /**
     * Populates the base card payment method from Google Pay settings.
     *
     * @param source Google Pay settings.
     * @param target Google Pay merchant configuration.
     */
    protected void populateBaseCardPaymentMethod(final GooglePaySettingsData source, final GooglePayMerchantConfigurationData target) {
        final BaseCardPaymentMethodData baseCardPaymentMethod = new BaseCardPaymentMethodData();

        baseCardPaymentMethod.setType(source.getType());
        populateParameters(source, baseCardPaymentMethod);

        target.setBaseCardPaymentMethod(baseCardPaymentMethod);
    }

    /**
     * Populates base card payment method parameters from Google Pay settings.
     *
     * @param source                Google Pay settings.
     * @param baseCardPaymentMethod base card payment method.
     */
    protected void populateParameters(final GooglePaySettingsData source, final BaseCardPaymentMethodData baseCardPaymentMethod) {
        final BaseCardPaymentMethodParametersData baseCardPaymentMethodParameters = new BaseCardPaymentMethodParametersData();

        baseCardPaymentMethodParameters.setAllowedAuthMethods(source.getAllowedAuthMethods());
        baseCardPaymentMethodParameters.setAllowedCardNetworks(source.getAllowedCardNetworks());
        baseCardPaymentMethodParameters.setBillingAddressRequired(Boolean.TRUE);

        baseCardPaymentMethod.setParameters(baseCardPaymentMethodParameters);
        populateBillingAddressParameters(baseCardPaymentMethodParameters);
    }

    /**
     * Populates Google Pay billing address format.
     *
     * @param baseCardPaymentMethodParameters base card parameters.
     */
    protected void populateBillingAddressParameters(final BaseCardPaymentMethodParametersData baseCardPaymentMethodParameters) {
        final GooglePayBillingAddressParametersFormatData googlePayBillingAddressParameters = new GooglePayBillingAddressParametersFormatData();

        googlePayBillingAddressParameters.setFormat(FORMAT);

        baseCardPaymentMethodParameters.setBillingAddressParameters(googlePayBillingAddressParameters);
    }

    /**
     * Populates Google Pay client settings environments.
     * @param source Google Pay settings.
     * @param target Google Pay merchant configuration.
     */
    protected void populateClientSettings(final GooglePaySettingsData source, final GooglePayMerchantConfigurationData target) {
        final GooglePayClientSettingsData googlePayClientSettings = new GooglePayClientSettingsData();

        googlePayClientSettings.setEnvironment(source.getEnvironment());

        target.setClientSettings(googlePayClientSettings);
    }
}
