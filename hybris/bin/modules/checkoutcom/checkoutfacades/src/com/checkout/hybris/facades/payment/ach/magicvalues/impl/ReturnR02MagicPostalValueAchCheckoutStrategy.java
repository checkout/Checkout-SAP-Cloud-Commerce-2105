package com.checkout.hybris.facades.payment.ach.magicvalues.impl;

import com.checkout.hybris.facades.beans.AchBankInfoDetailsData;
import com.checkout.hybris.facades.payment.ach.magicvalues.MagicPostalCodeValueAchCheckoutStrategy;

public class ReturnR02MagicPostalValueAchCheckoutStrategy implements MagicPostalCodeValueAchCheckoutStrategy {
	private static final String MAGIC_VALUE = "R00002";

	@Override
	public AchBankInfoDetailsData createAchBankInfoDetailsData() {
		final AchBankInfoDetailsData achBankInfoDetailsData = new AchBankInfoDetailsData();
		achBankInfoDetailsData.setAccountHolderName("Tom Black");
		achBankInfoDetailsData.setAccountType("CHECKING");
		achBankInfoDetailsData.setAccountNumber("9999999999");
		achBankInfoDetailsData.setBankRouting("011075150");
		achBankInfoDetailsData.setMask("0000009999");
		achBankInfoDetailsData.setInstitutionName("Bank of america");
		achBankInfoDetailsData.setCompanyName("Widget Inc");

		return achBankInfoDetailsData;
	}

	@Override
	public boolean isApplicable(final String postalCode) {
		return MAGIC_VALUE.equals(postalCode);
	}
}
