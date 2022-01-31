package com.checkout.hybris.facades.payment.attributes.strategies.impl;

import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.facades.payment.attributes.mapper.CheckoutComPaymentAttributesStrategyMapper;
import com.checkout.hybris.facades.payment.attributes.strategies.CheckoutComPaymentAttributeStrategy;

import javax.annotation.PostConstruct;

/**
 * Abstract strategy to implement the common payment attributes
 */
public abstract class CheckoutComAbstractPaymentAttributeStrategy implements CheckoutComPaymentAttributeStrategy {

    protected CheckoutComPaymentAttributesStrategyMapper checkoutComPaymentAttributesStrategyMapper;

    protected CheckoutComAbstractPaymentAttributeStrategy(final CheckoutComPaymentAttributesStrategyMapper checkoutComPaymentAttributesStrategyMapper) {
        this.checkoutComPaymentAttributesStrategyMapper = checkoutComPaymentAttributesStrategyMapper;
    }

    protected CheckoutComAbstractPaymentAttributeStrategy() {
        // default empty constructor
    }

    /**
     * Add the strategy to the factory map of strategies
     */
    @PostConstruct
    protected void registerStrategy() {
        checkoutComPaymentAttributesStrategyMapper.addStrategy(getStrategyKey(), this);
    }

    /**
     * Returns the key of the strategy used to register the strategy
     *
     * @return the key the strategy will be registered as
     */
    protected abstract CheckoutComPaymentType getStrategyKey();
}
