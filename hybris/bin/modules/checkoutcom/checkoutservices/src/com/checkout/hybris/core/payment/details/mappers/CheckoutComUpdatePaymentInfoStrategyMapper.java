package com.checkout.hybris.core.payment.details.mappers;

import com.checkout.hybris.core.payment.details.strategies.CheckoutComUpdatePaymentInfoStrategy;
import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import org.apache.commons.collections4.MapUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Adds the strategies to the map and gets the strategy based on the key
 */
public class CheckoutComUpdatePaymentInfoStrategyMapper {

    protected final CheckoutComUpdatePaymentInfoStrategy defaultCheckoutComPaymentInfoStrategy;
    protected final Map<CheckoutComPaymentType, CheckoutComUpdatePaymentInfoStrategy> strategies = new HashMap<>();

    public CheckoutComUpdatePaymentInfoStrategyMapper(final CheckoutComUpdatePaymentInfoStrategy defaultCheckoutComPaymentInfoStrategy) {
        this.defaultCheckoutComPaymentInfoStrategy = defaultCheckoutComPaymentInfoStrategy;
    }

    /**
     * Adds the strategies with related keys
     *
     * @param key   the strategy key
     * @param value the strategy
     */
    public void addStrategy(final CheckoutComPaymentType key, final CheckoutComUpdatePaymentInfoStrategy value) {
        strategies.put(key, value);
    }

    /**
     * Gets the strategy for the given key, returns the default one if no strategy found for the key
     *
     * @param key the strategy key
     * @return the strategy
     */
    public CheckoutComUpdatePaymentInfoStrategy findStrategy(final CheckoutComPaymentType key) {
        if (MapUtils.isNotEmpty(strategies) && strategies.containsKey(key)) {
            return strategies.get(key);
        }
        return defaultCheckoutComPaymentInfoStrategy;
    }
}
