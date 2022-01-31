package com.checkout.hybris.facades.payment.converters.populators;

import com.checkout.hybris.core.merchant.services.CheckoutComMerchantConfigurationService;
import com.checkout.hybris.core.model.CheckoutComApplePayPaymentInfoModel;
import com.checkout.hybris.facades.beans.WalletPaymentInfoData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.springframework.util.Assert;

/**
 * Populates the custom properties of the extended {@link CheckoutComApplePayPaymentInfoModel}
 */
public class CheckoutComApplePayPaymentInfoReversePopulator implements Populator<WalletPaymentInfoData, CheckoutComApplePayPaymentInfoModel> {

    protected final CheckoutComMerchantConfigurationService checkoutComMerchantConfigurationService;

    public CheckoutComApplePayPaymentInfoReversePopulator(final CheckoutComMerchantConfigurationService checkoutComMerchantConfigurationService) {
        this.checkoutComMerchantConfigurationService = checkoutComMerchantConfigurationService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void populate(final WalletPaymentInfoData source, final CheckoutComApplePayPaymentInfoModel target) throws ConversionException {
        Assert.notNull(source, "Parameter WalletPaymentInfoData  cannot be null.");
        Assert.notNull(target, "Parameter CheckoutComApplePayPaymentInfoModel cannot be null.");

        target.setType(source.getType());
        target.setToken(source.getToken());
        target.setUserDataRequired(false);
        target.setDeferred(checkoutComMerchantConfigurationService.isAutoCapture());
    }
}