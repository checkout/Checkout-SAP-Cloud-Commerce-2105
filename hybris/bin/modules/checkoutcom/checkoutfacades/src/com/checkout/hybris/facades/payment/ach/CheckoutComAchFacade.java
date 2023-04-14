package com.checkout.hybris.facades.payment.ach;

import com.checkout.hybris.facades.beans.AchBankInfoDetailsData;

import java.io.IOException;

public interface CheckoutComAchFacade {
	void setPaymentInfoAchToCart(final AchBankInfoDetailsData achBankInfoDetailsData) throws IOException;
}
