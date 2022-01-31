package com.checkout.hybris.facades.payment.attributes.strategies;

import org.springframework.ui.Model;

/**
 * Adds payment attributes to the model
 */
public interface CheckoutComPaymentAttributeStrategy {

    /**
     * Adds specific payment attribute to the model
     *
     * @param model model to add attributes
     */
    void addPaymentAttributeToModel(Model model);
}
