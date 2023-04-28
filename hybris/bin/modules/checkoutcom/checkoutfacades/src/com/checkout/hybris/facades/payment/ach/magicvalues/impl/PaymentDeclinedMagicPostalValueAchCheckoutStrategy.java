package com.checkout.hybris.facades.payment.ach.magicvalues.impl;

import com.checkout.hybris.facades.beans.AchBankInfoDetailsData;
import com.checkout.hybris.facades.payment.ach.magicvalues.MagicPostalCodeValueAchCheckoutStrategy;

public class PaymentDeclinedMagicPostalValueAchCheckoutStrategy implements MagicPostalCodeValueAchCheckoutStrategy {
	private static final String MAGIC_VALUE = "PD00001";

	@Override
	public AchBankInfoDetailsData createAchBankInfoDetailsData() {
		final AchBankInfoDetailsData achBankInfoDetailsData = new AchBankInfoDetailsData();
		achBankInfoDetailsData.setAccountHolderName("Mike Hammer");
		achBankInfoDetailsData.setAccountType("CHECKING");
		achBankInfoDetailsData.setAccountNumber("10@BC99999");
		achBankInfoDetailsData.setBankRouting("091000022");
		achBankInfoDetailsData.setMask("****9999");
		achBankInfoDetailsData.setInstitutionName("Bank of america");

		return achBankInfoDetailsData;
	}

	@Override
	public boolean isApplicable(final String postalCode) {
		return MAGIC_VALUE.equals(postalCode);
	}
}
