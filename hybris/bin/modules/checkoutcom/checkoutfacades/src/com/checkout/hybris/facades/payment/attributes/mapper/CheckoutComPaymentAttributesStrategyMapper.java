package com.checkout.hybris.facades.payment.attributes.mapper;

import com.checkout.hybris.facades.payment.attributes.strategies.CheckoutComPaymentAttributeStrategy;
import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import org.apache.commons.collections4.MapUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Adds the strategies to the map and gets the strategy based on the key
 */
public class CheckoutComPaymentAttributesStrategyMapper {

    protected final Map<CheckoutComPaymentType, CheckoutComPaymentAttributeStrategy> strategies = new HashMap<>();

    /**
     * Adds the strategies with related keys
     *
     * @param key   the strategy key
     * @param value the strategy bean
     */
    public void addStrategy(final CheckoutComPaymentType key, final CheckoutComPaymentAttributeStrategy value) {
        strategies.put(key, value);
    }

    /**
     * Gets the strategy for the given key
     *
     * @param key the strategy key
     * @return the strategy bean, optional empty if no strategy found
     */
    public Optional<CheckoutComPaymentAttributeStrategy> findStrategy(final CheckoutComPaymentType key) {
        if (MapUtils.isNotEmpty(strategies) && strategies.containsKey(key)) {
            return Optional.of(strategies.get(key));
        }
        return Optional.empty();
    }
}
