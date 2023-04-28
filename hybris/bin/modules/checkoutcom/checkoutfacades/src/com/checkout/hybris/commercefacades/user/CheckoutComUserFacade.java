package com.checkout.hybris.commercefacades.user;

import de.hybris.platform.commercefacades.order.data.CCPaymentInfoData;
import de.hybris.platform.commercefacades.user.UserFacade;

public interface CheckoutComUserFacade extends UserFacade {

	void updateCreditCardDetails(String code, CCPaymentInfoData ccPaymentInfoData);
}
