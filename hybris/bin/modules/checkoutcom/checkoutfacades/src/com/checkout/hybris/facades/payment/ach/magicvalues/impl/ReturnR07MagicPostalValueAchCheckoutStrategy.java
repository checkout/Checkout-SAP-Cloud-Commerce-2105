package com.checkout.hybris.facades.payment.ach.magicvalues.impl;

import com.checkout.hybris.facades.beans.AchBankInfoDetailsData;
import com.checkout.hybris.facades.payment.ach.magicvalues.MagicPostalCodeValueAchCheckoutStrategy;

public class ReturnR07MagicPostalValueAchCheckoutStrategy implements MagicPostalCodeValueAchCheckoutStrategy {
	private static final String MAGIC_VALUE = "R00007";

	@Override
	public AchBankInfoDetailsData createAchBankInfoDetailsData() {
		final AchBankInfoDetailsData achBankInfoDetailsData = new AchBankInfoDetailsData();
		achBankInfoDetailsData.setAccountHolderName("Tom Black");
		achBankInfoDetailsData.setAccountType("CHECKING");
		achBankInfoDetailsData.setAccountNumber("082000549");
		achBankInfoDetailsData.setBankRouting("121122676");
		achBankInfoDetailsData.setMask("0000000549");
		achBankInfoDetailsData.setInstitutionName("Bank of america");
		achBankInfoDetailsData.setCompanyName("Widget Inc");

		return achBankInfoDetailsData;
	}

	@Override
	public boolean isApplicable(final String postalCode) {
		return MAGIC_VALUE.equals(postalCode);
	}
}
