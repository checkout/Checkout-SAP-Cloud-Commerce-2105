package com.checkout.hybris.facades.payment.ach.magicvalues.impl;

import com.checkout.hybris.facades.beans.AchBankInfoDetailsData;
import com.checkout.hybris.facades.payment.ach.magicvalues.MagicPostalCodeValueAchCheckoutStrategy;

public class ReturnR01MagicPostalValueAchCheckoutStrategy implements MagicPostalCodeValueAchCheckoutStrategy {
	private static final String MAGIC_VALUE = "R00001";

	@Override
	public AchBankInfoDetailsData createAchBankInfoDetailsData() {
		final AchBankInfoDetailsData achBankInfoDetailsData = new AchBankInfoDetailsData();
		achBankInfoDetailsData.setAccountHolderName("Mike Hammer");
		achBankInfoDetailsData.setAccountType("CHECKING");
		achBankInfoDetailsData.setAccountNumber("4099999992");
		achBankInfoDetailsData.setBankRouting("011075150");
		achBankInfoDetailsData.setMask("0000009992");
		achBankInfoDetailsData.setInstitutionName("Bank of america");

		return achBankInfoDetailsData;
	}

	@Override
	public boolean isApplicable(final String postalCode) {
		return MAGIC_VALUE.equals(postalCode);
	}
}
