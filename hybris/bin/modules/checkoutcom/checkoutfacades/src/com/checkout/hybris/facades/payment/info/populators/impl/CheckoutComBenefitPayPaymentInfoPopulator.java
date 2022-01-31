package com.checkout.hybris.facades.payment.info.populators.impl;

import com.checkout.hybris.core.model.CheckoutComAPMPaymentInfoModel;
import com.checkout.hybris.core.model.CheckoutComBenefitPayPaymentInfoModel;
import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.facades.beans.CheckoutComPaymentInfoData;
import com.checkout.hybris.facades.payment.info.mappers.CheckoutComApmPaymentInfoPopulatorMapper;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import javax.annotation.PostConstruct;

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.BENEFITPAY;
import static com.google.common.base.Preconditions.checkArgument;

/**
 * Populates the {@link CheckoutComPaymentInfoData} for BenefitPay adding custom payment attributes
 */
public class CheckoutComBenefitPayPaymentInfoPopulator extends CheckoutComAbstractApmPaymentInfoPopulator {

    protected final CheckoutComApmPaymentInfoPopulatorMapper checkoutComApmPaymentInfoPopulatorMapper;

    public CheckoutComBenefitPayPaymentInfoPopulator(final Converter<AddressModel, AddressData> addressConverter,
                                                     final CheckoutComApmPaymentInfoPopulatorMapper checkoutComApmPaymentInfoPopulatorMapper) {
        super(addressConverter);
        this.checkoutComApmPaymentInfoPopulatorMapper = checkoutComApmPaymentInfoPopulatorMapper;
    }

    /**
     * Adds the populator to the list of populators
     */
    @PostConstruct
    protected void registerPopulator() {
        checkoutComApmPaymentInfoPopulatorMapper.addPopulator(getPopulatorKey(), this);
    }

    /**
     * Returns the populator key
     */
    protected CheckoutComPaymentType getPopulatorKey() {
        return BENEFITPAY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void populate(final CheckoutComAPMPaymentInfoModel source, final CheckoutComPaymentInfoData target) throws ConversionException {
        callSuperPopulate(source, target);

        checkArgument(source instanceof CheckoutComBenefitPayPaymentInfoModel, "The payment info model must be a valid CheckoutComBenefitPayPaymentInfoModel.");
        final CheckoutComBenefitPayPaymentInfoModel benefitPayPaymentInfo = (CheckoutComBenefitPayPaymentInfoModel) source;

        target.setQrCodeData(benefitPayPaymentInfo.getQrCode());
    }

    protected void callSuperPopulate(final CheckoutComAPMPaymentInfoModel source, final CheckoutComPaymentInfoData target) {
        super.populate(source, target);
    }
}