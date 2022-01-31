package com.checkout.hybris.core.payment.details.strategies.impl;

import com.checkout.hybris.core.model.CheckoutComCreditCardPaymentInfoModel;
import com.checkout.hybris.core.payment.details.mappers.CheckoutComUpdatePaymentInfoStrategyMapper;
import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentInfoService;
import com.checkout.payments.GetPaymentResponse;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.order.CartService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.CARD;

/**
 * Strategy to processes the checkout.com payment response for Card payment
 */
public class CheckoutComCardUpdatePaymentInfoStrategy extends CheckoutComAbstractUpdatePaymentInfoStrategy {

    protected static final Logger LOG = LogManager.getLogger(CheckoutComCardUpdatePaymentInfoStrategy.class);

    protected CheckoutComPaymentInfoService paymentInfoService;
    protected CheckoutComUpdatePaymentInfoStrategyMapper checkoutComUpdatePaymentInfoStrategyMapper;

    public CheckoutComCardUpdatePaymentInfoStrategy(final CartService cartService,
                                                    final CheckoutComPaymentInfoService paymentInfoService,
                                                    final CheckoutComUpdatePaymentInfoStrategyMapper checkoutComUpdatePaymentInfoStrategyMapper) {
        super(cartService);
        this.paymentInfoService = paymentInfoService;
        this.checkoutComUpdatePaymentInfoStrategyMapper = checkoutComUpdatePaymentInfoStrategyMapper;
    }

    /**
     * Add the strategy to the factory map of strategies
     */
    @PostConstruct
    protected void registerStrategy() {
        checkoutComUpdatePaymentInfoStrategyMapper.addStrategy(getStrategyKey(), this);
    }

    /**
     * {@inheritDoc}
     */
    protected CheckoutComPaymentType getStrategyKey() {
        return CARD;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processPaymentResponse(final GetPaymentResponse paymentResponse) {
        callSuperProcessPayment(paymentResponse);
        if (paymentResponse.getSource() != null) {
            final CartModel sessionCart = cartService.getSessionCart();
            paymentInfoService.addSubscriptionIdToUserPayment((CheckoutComCreditCardPaymentInfoModel) sessionCart.getPaymentInfo(), paymentResponse.getSource());
        } else {
            throw new IllegalArgumentException("The current payment details source is null. The current payment method cannot be mark as saved.");
        }
    }

    protected void callSuperProcessPayment(final GetPaymentResponse paymentResponse) {
        super.processPaymentResponse(paymentResponse);
    }
}

