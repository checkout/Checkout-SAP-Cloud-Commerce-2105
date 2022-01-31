package com.checkout.hybris.core.payment.request.mappers;

import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.core.payment.request.strategies.CheckoutComPaymentRequestStrategy;

import java.util.HashMap;
import java.util.Map;

/**
 * Adds the strategies to the map and gets the strategy based on the key
 */
public class CheckoutComPaymentRequestStrategyMapper {

    protected final Map<CheckoutComPaymentType, CheckoutComPaymentRequestStrategy> strategies = new HashMap<>();

    /**
     * Adds the strategies with related keys
     *
     * @param key   the strategy key
     * @param value the strategy bean
     */
    public void addStrategy(final CheckoutComPaymentType key, final CheckoutComPaymentRequestStrategy value) {
        strategies.put(key, value);
    }

    /**
     * Gets the strategy for the given key
     *
     * @param key the strategy key
     * @return the strategy bean
     */
    public CheckoutComPaymentRequestStrategy findStrategy(final CheckoutComPaymentType key) {
        return strategies.get(key);
    }
}
