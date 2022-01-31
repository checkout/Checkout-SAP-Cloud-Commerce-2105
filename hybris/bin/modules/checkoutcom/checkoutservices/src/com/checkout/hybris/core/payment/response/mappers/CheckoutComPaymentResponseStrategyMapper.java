package com.checkout.hybris.core.payment.response.mappers;

import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.core.payment.response.strategies.CheckoutComPaymentResponseStrategy;
import org.apache.commons.collections4.MapUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Adds the strategies to the map and gets the strategy based on the key
 */
public class CheckoutComPaymentResponseStrategyMapper {

    protected final CheckoutComPaymentResponseStrategy defaultCheckoutComPaymentResponseStrategy;
    protected final Map<CheckoutComPaymentType, CheckoutComPaymentResponseStrategy> strategies = new HashMap<>();

    public CheckoutComPaymentResponseStrategyMapper(final CheckoutComPaymentResponseStrategy defaultCheckoutComPaymentResponseStrategy) {
        this.defaultCheckoutComPaymentResponseStrategy = defaultCheckoutComPaymentResponseStrategy;
    }

    /**
     * Adds the strategies with related keys
     *
     * @param key   the strategy key
     * @param value the strategy
     */
    public void addStrategy(final CheckoutComPaymentType key, final CheckoutComPaymentResponseStrategy value) {
        strategies.put(key, value);
    }

    /**
     * Gets the strategy for the given key, returns the default one if no registered strategy found
     *
     * @param key the strategy key
     * @return the strategy
     */
    public CheckoutComPaymentResponseStrategy findStrategy(final CheckoutComPaymentType key) {
        if (MapUtils.isNotEmpty(strategies) && strategies.containsKey(key)) {
            return strategies.get(key);
        }
        return defaultCheckoutComPaymentResponseStrategy;
    }
}