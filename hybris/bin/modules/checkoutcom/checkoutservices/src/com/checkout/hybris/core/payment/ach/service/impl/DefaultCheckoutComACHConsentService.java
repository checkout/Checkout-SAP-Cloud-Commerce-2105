package com.checkout.hybris.core.payment.ach.service.impl;

import com.checkout.hybris.core.model.CheckoutComACHConsentModel;
import com.checkout.hybris.core.payment.ach.service.CheckoutComACHConsentService;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.order.CartService;
import de.hybris.platform.servicelayer.model.ModelService;

import java.util.Optional;

/**
 * Default implementation of {@link CheckoutComACHConsentService}
 */
public class DefaultCheckoutComACHConsentService implements CheckoutComACHConsentService {

    protected final ModelService modelService;

    protected final CartService cartService;

    public DefaultCheckoutComACHConsentService(final ModelService modelService,
                                               final CartService cartService) {
        this.modelService = modelService;
        this.cartService = cartService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveCheckoutComACHConsent(final CheckoutComACHConsentModel checkoutComACHConsent) {
        final CartModel sessionCart = cartService.getSessionCart();
        removeConsent(sessionCart);
        sessionCart.setAchConsent(checkoutComACHConsent);
        checkoutComACHConsent.setOrder(sessionCart);
        modelService.save(checkoutComACHConsent);
        modelService.refresh(checkoutComACHConsent);
        modelService.save(sessionCart);
        modelService.refresh(sessionCart);
    }

    protected void removeConsent(final CartModel sessionCart) {
        final CheckoutComACHConsentModel achConsent = sessionCart.getAchConsent();
        Optional.ofNullable(achConsent).ifPresent(consent -> modelService.remove(achConsent));
    }
}
