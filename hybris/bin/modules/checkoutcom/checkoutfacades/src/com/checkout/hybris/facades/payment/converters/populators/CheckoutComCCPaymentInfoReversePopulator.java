package com.checkout.hybris.facades.payment.converters.populators;

import com.checkout.hybris.core.merchant.services.CheckoutComMerchantConfigurationService;
import com.checkout.hybris.core.model.CheckoutComCreditCardPaymentInfoModel;
import com.checkout.hybris.core.payment.resolvers.CheckoutComPaymentTypeResolver;
import de.hybris.platform.commercefacades.order.data.CCPaymentInfoData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.springframework.util.Assert;

/**
 * Populates the custom properties of the extended {@link CheckoutComCreditCardPaymentInfoModel}
 */
public class CheckoutComCCPaymentInfoReversePopulator implements Populator<CCPaymentInfoData, CheckoutComCreditCardPaymentInfoModel> {

    protected final CheckoutComPaymentTypeResolver checkoutComPaymentTypeResolver;
    protected final CheckoutComMerchantConfigurationService checkoutComMerchantConfigurationService;

    public CheckoutComCCPaymentInfoReversePopulator(final CheckoutComPaymentTypeResolver checkoutComPaymentTypeResolver,
                                                    final CheckoutComMerchantConfigurationService checkoutComMerchantConfigurationService) {
        this.checkoutComPaymentTypeResolver = checkoutComPaymentTypeResolver;
        this.checkoutComMerchantConfigurationService = checkoutComMerchantConfigurationService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void populate(final CCPaymentInfoData ccPaymentInfoData, final CheckoutComCreditCardPaymentInfoModel checkoutComCreditCardPaymentInfoModel) throws ConversionException {
        Assert.notNull(ccPaymentInfoData, "Parameter ccPaymentInfoData cannot be null.");
        Assert.notNull(checkoutComCreditCardPaymentInfoModel, "Parameter checkoutComCreditCardPaymentInfoModel cannot be null.");

        checkoutComCreditCardPaymentInfoModel.setCardToken(ccPaymentInfoData.getPaymentToken());
        checkoutComCreditCardPaymentInfoModel.setMarkToSave(ccPaymentInfoData.isSaved());
        checkoutComCreditCardPaymentInfoModel.setSaved(false);
        checkoutComCreditCardPaymentInfoModel.setCardBin(ccPaymentInfoData.getCardBin());
        checkoutComCreditCardPaymentInfoModel.setScheme(ccPaymentInfoData.getScheme());
        checkoutComCreditCardPaymentInfoModel.setAutoCapture(checkoutComPaymentTypeResolver.isMadaCard(ccPaymentInfoData.getCardBin()) || checkoutComMerchantConfigurationService.isAutoCapture());
    }
}
