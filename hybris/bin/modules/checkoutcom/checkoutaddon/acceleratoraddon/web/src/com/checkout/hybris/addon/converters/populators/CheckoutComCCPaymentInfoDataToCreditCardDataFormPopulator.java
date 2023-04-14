package com.checkout.hybris.addon.converters.populators;

import com.checkout.hybris.addon.forms.CreditCardDataForm;
import de.hybris.platform.commercefacades.order.data.CCPaymentInfoData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

public class CheckoutComCCPaymentInfoDataToCreditCardDataFormPopulator implements Populator<CCPaymentInfoData,
		CreditCardDataForm> {
	@Override
	public void populate(final CCPaymentInfoData ccPaymentInfoData, final CreditCardDataForm creditCardDataForm) throws ConversionException {
		creditCardDataForm.setAccountHolderName(ccPaymentInfoData.getAccountHolderName());
		creditCardDataForm.setExpiryYear(Integer.parseInt(ccPaymentInfoData.getExpiryYear()));
		creditCardDataForm.setExpiryMonth(Integer.parseInt(ccPaymentInfoData.getExpiryMonth()));
	}
}
