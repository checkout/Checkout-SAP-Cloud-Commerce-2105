package com.checkout.hybris.facades.hooks;

import com.checkout.hybris.core.model.CheckoutComACHConsentModel;
import com.checkout.hybris.core.model.CheckoutComAchPaymentInfoModel;
import de.hybris.platform.commerceservices.order.hook.CommercePlaceOrderMethodHook;
import de.hybris.platform.commerceservices.service.data.CommerceCheckoutParameter;
import de.hybris.platform.commerceservices.service.data.CommerceOrderResult;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.order.InvalidCartException;
import de.hybris.platform.servicelayer.model.ModelService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class ensures the ACH consent is moved from the cart to the order and also the link between consent and order
 * is done. In case the order is not an ACH payment method and the consent is set, it will be removed.
 */
public class DefaultCheckoutComACHConsentPlaceOrderHook implements CommercePlaceOrderMethodHook {

	private static final Logger LOG = LogManager.getLogger(DefaultCheckoutComACHConsentPlaceOrderHook.class);
	protected final ModelService modelService;

	public DefaultCheckoutComACHConsentPlaceOrderHook(final ModelService modelService) {
		this.modelService = modelService;
	}

	@Override
	public void afterPlaceOrder(final CommerceCheckoutParameter parameter, final CommerceOrderResult orderModel) {
		final CartModel cart = parameter.getCart();
		final CheckoutComACHConsentModel achConsent = cart.getAchConsent();
		if (cart.getPaymentInfo() instanceof CheckoutComAchPaymentInfoModel && achConsent != null) {
			linkConsentFromCartToOrder(achConsent, orderModel);
		}
	}


	@Override
	public void beforePlaceOrder(final CommerceCheckoutParameter parameter) throws InvalidCartException {
		final CartModel cart = parameter.getCart();
		final CheckoutComACHConsentModel achConsent = cart.getAchConsent();
		if (!(cart.getPaymentInfo() instanceof CheckoutComAchPaymentInfoModel) && achConsent != null) {
			LOG.debug("The payment for cart [{}] is not ACH and consent still exists, removing it", cart.getCode());
			modelService.remove(achConsent);
		}
	}

	@Override
	public void beforeSubmitOrder(final CommerceCheckoutParameter parameter, final CommerceOrderResult result) {
		//Does nothing
	}

	protected void linkConsentFromCartToOrder(final CheckoutComACHConsentModel achConsent,
											  final CommerceOrderResult commerceOrderResult) {
		final OrderModel order = commerceOrderResult.getOrder();
		order.setAchConsent(achConsent);
		achConsent.setOrder(order);
		modelService.save(achConsent);
		modelService.refresh(achConsent);
		modelService.save(order);
		modelService.refresh(order);
	}
}
