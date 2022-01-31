package com.checkout.hybris.core.order.services;

import de.hybris.platform.commerceservices.service.data.CommerceCheckoutParameter;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;

import java.math.BigDecimal;

/**
 * Handle the operations in the order
 */
public interface CheckoutComOrderService {

    /**
     * Creates a {@link CommerceCheckoutParameter} based on the passed {@link CartModel} and {@link PaymentInfoModel} given
     *
     * @param abstractOrderModel  The abstractOrderModel to base the commerceCheckoutParameter on
     * @param paymentInfoModel    The paymentInfo to base the commerceCheckoutParameter on
     * @param authorisationAmount The authorised amount by the payment provider
     * @return the created parameters
     */
    CommerceCheckoutParameter createCommerceCheckoutParameter(AbstractOrderModel abstractOrderModel, PaymentInfoModel paymentInfoModel, BigDecimal authorisationAmount);
}
