package com.checkout.hybris.core.payment.response.strategies.impl;

import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.core.payment.response.mappers.CheckoutComPaymentResponseStrategyMapper;
import com.checkout.hybris.core.payment.response.strategies.CheckoutComPaymentResponseStrategy;

import javax.annotation.PostConstruct;

/**
 * Abstract strategy to implement the common response handle logic
 */
public abstract class CheckoutComAbstractPaymentResponseStrategy implements CheckoutComPaymentResponseStrategy {

    protected CheckoutComPaymentResponseStrategyMapper checkoutComPaymentResponseStrategyMapper;

    protected CheckoutComAbstractPaymentResponseStrategy() {
        // default empty constructor
    }

    public CheckoutComAbstractPaymentResponseStrategy(final CheckoutComPaymentResponseStrategyMapper checkoutComPaymentResponseStrategyMapper) {
        this.checkoutComPaymentResponseStrategyMapper = checkoutComPaymentResponseStrategyMapper;
    }

    /**
     * Add the strategy to the factory map of strategies
     */
    @PostConstruct
    protected void registerStrategy() {
        checkoutComPaymentResponseStrategyMapper.addStrategy(getStrategyKey(), this);
    }

    /**
     * Returns the key of the strategy used to register the strategy
     *
     * @return the key the strategy will be registered as
     */
    protected abstract CheckoutComPaymentType getStrategyKey();

}
