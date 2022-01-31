/*
 * Copyright (c) 2019 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.checkout.hybris.addon.constants;

/**
 * Global class for all Checkoutaddon web constants. You can add global constants for your extension into this class.
 */
@SuppressWarnings({"unused", "cast", "deprecation", "squid:CallToDeprecatedMethod"})
public final class CheckoutaddonWebConstants {

    public static final String PAYMENT_METHOD_MODEL_ATTRIBUTE_KEY = "paymentMethod";
    public static final String CART_DATA_MODEL_ATTRIBUTE_KEY = "cartData";
    public static final String DELIVERY_ADDRESS_MODEL_ATTRIBUTE_KEY = "deliveryAddress";
    public static final String META_ROBOTS_MODEL_ATTRIBUTE_KEY = "metaRobots";
    public static final String REDIRECT_TO_CHOOSE_PAYMENT_METHOD = "redirect:/checkout/multi/checkout-com/choose-payment-method";
    public static final String REDIRECT_TO_CHECKOUT_PAYMENT_METHOD_FORM = "redirect:/checkout/multi/checkout-com/payment/payment-method";

    private CheckoutaddonWebConstants() {
        //empty to avoid instantiating this constant class
    }
}