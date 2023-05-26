package com.checkout.hybris.facades.payment.ach.magicvalues;

import com.checkout.hybris.facades.beans.AchBankInfoDetailsData;

public interface MagicPostalCodeValueAchCheckoutStrategy {

	AchBankInfoDetailsData createAchBankInfoDetailsData();

	boolean isApplicable(String postalCode);
}
