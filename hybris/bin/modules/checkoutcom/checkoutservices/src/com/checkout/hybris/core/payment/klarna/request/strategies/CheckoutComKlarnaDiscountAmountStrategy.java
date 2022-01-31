package com.checkout.hybris.core.payment.klarna.request.strategies;

import com.checkout.hybris.core.klarna.session.request.KlarnaProductRequestDto;
import de.hybris.platform.core.model.order.CartModel;

import java.util.List;

/**
 * Strategy to calculate total discount amounts for each order line
 */
public interface CheckoutComKlarnaDiscountAmountStrategy {

    /**
     * Calculates the correct discount for each product line and applies the value
     *
     * @param cart         the cart model
     * @param productLines the populated product lines
     */
    void applyDiscountsToKlarnaOrderLines(CartModel cart, List<KlarnaProductRequestDto> productLines);
}
