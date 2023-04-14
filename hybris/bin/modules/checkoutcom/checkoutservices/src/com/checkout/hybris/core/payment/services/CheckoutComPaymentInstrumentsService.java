package com.checkout.hybris.core.payment.services;

import de.hybris.platform.core.model.order.payment.CreditCardPaymentInfoModel;

public interface CheckoutComPaymentInstrumentsService {
	void removeInstrumentByCreditCard(CreditCardPaymentInfoModel creditCardPaymentInfoModel);

	void updateInstrumentByCreditCard(CreditCardPaymentInfoModel creditCardPaymentInfoModel);
}
