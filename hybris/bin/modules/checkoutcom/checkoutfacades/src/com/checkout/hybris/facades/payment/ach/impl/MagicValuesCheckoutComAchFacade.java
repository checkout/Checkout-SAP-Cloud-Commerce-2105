package com.checkout.hybris.facades.payment.ach.impl;

import com.checkout.hybris.facades.beans.AchBankInfoDetailsData;
import com.checkout.hybris.facades.payment.CheckoutComPaymentInfoFacade;
import com.checkout.hybris.facades.payment.ach.magicvalues.MagicPostalCodeValuesAchCheckoutStrategyFactory;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.order.CartService;

public class MagicValuesCheckoutComAchFacade extends DefaultCheckoutComAchFacade {
	private final CartService cartService;

	private final MagicPostalCodeValuesAchCheckoutStrategyFactory magicPostalCodeValuesAchCheckoutStrategyFactory;

	public MagicValuesCheckoutComAchFacade(final CheckoutComPaymentInfoFacade checkoutComPaymentInfoFacade,
										   final CartService cartService,
										   final MagicPostalCodeValuesAchCheckoutStrategyFactory magicPostalCodeValuesAchCheckoutStrategyFactory) {
		super(checkoutComPaymentInfoFacade);
		this.cartService = cartService;
		this.magicPostalCodeValuesAchCheckoutStrategyFactory = magicPostalCodeValuesAchCheckoutStrategyFactory;
	}


	@Override
	public void setPaymentInfoAchToCart(final AchBankInfoDetailsData achBankInfoDetailsData) {
		final CartModel sessionCart = cartService.getSessionCart();
		final AddressModel deliveryAddress = sessionCart.getDeliveryAddress();
		final String postalCode = deliveryAddress.getPostalcode();
		magicPostalCodeValuesAchCheckoutStrategyFactory.findStrategy(postalCode).ifPresentOrElse(
				strategy -> callSuperSetPaymentInfoAchToCart(strategy.createAchBankInfoDetailsData()),
				() -> callSuperSetPaymentInfoAchToCart(achBankInfoDetailsData));
	}

	protected void callSuperSetPaymentInfoAchToCart(final AchBankInfoDetailsData achBankInfoDetailsData) {
		super.setPaymentInfoAchToCart(achBankInfoDetailsData);
	}
}
