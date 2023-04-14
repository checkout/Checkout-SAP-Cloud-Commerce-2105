package com.checkout.hybris.addon.converters.populators;

import com.checkout.hybris.addon.forms.CreditCardDataForm;
import de.hybris.platform.commercefacades.order.data.CCPaymentInfoData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

public class CheckoutComCreditCardDataFormToCCPaymentInfoDataPopulator implements Populator<
		CreditCardDataForm, CCPaymentInfoData> {

	@Override
	public void populate(final CreditCardDataForm creditCardDataForm, final CCPaymentInfoData ccPaymentInfoData) throws ConversionException {
		ccPaymentInfoData.setAccountHolderName(creditCardDataForm.getAccountHolderName());
		ccPaymentInfoData.setExpiryYear(String.valueOf(creditCardDataForm.getExpiryYear()));
		ccPaymentInfoData.setExpiryMonth(String.valueOf(creditCardDataForm.getExpiryMonth()));
		ccPaymentInfoData.setId(String.valueOf(creditCardDataForm.getCreditCardCode()));
	}
}
