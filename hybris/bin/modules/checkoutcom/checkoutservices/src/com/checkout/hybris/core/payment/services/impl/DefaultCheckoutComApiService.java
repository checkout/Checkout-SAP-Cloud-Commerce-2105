package com.checkout.hybris.core.payment.services.impl;

import com.checkout.CheckoutApi;
import com.checkout.CheckoutApiImpl;
import com.checkout.hybris.core.enums.EnvironmentType;
import com.checkout.hybris.core.merchant.services.CheckoutComMerchantConfigurationService;
import com.checkout.hybris.core.payment.services.CheckoutComApiService;

public class DefaultCheckoutComApiService implements CheckoutComApiService {
	private final CheckoutComMerchantConfigurationService checkoutComMerchantConfigurationService;

	public DefaultCheckoutComApiService(final CheckoutComMerchantConfigurationService checkoutComMerchantConfigurationService) {
		this.checkoutComMerchantConfigurationService = checkoutComMerchantConfigurationService;
	}

	@Override
	public CheckoutApi createCheckoutApi() {
		return createCheckoutComApi();

	}

	protected CheckoutApi createCheckoutComApi() {
		final String secretKey = checkoutComMerchantConfigurationService.getSecretKey();
		final String publicKey = checkoutComMerchantConfigurationService.getPublicKey();
		final boolean useSandbox = checkoutComMerchantConfigurationService.getEnvironment()
																		  .equals(EnvironmentType.TEST);
		return createCheckoutComApi(secretKey, publicKey, useSandbox);

	}

	protected CheckoutApi createCheckoutComApi(final String secretKey, final String publicKey,
											   final boolean useSandbox) {
		return CheckoutApiImpl.create(secretKey, useSandbox, publicKey);
	}
}
