package com.checkout.hybris.commerceservices.customer;

import de.hybris.platform.commerceservices.customer.CustomerAccountService;
import de.hybris.platform.core.model.order.payment.CreditCardPaymentInfoModel;
import de.hybris.platform.core.model.user.CustomerModel;

public interface CheckoutComCustomerAccountServiceAdapter extends CustomerAccountService {
	void updateCreditCardDetails(CustomerModel customerModel, CreditCardPaymentInfoModel creditCardPaymentInfoModel);
}
