package com.checkout.hybris.facades.payment.attributes.strategies.impl;

import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.facades.beans.KlarnaClientTokenData;
import com.checkout.hybris.facades.payment.attributes.mapper.CheckoutComPaymentAttributesStrategyMapper;
import com.checkout.hybris.facades.payment.attributes.strategies.CheckoutComPaymentAttributeStrategy;
import com.checkout.hybris.facades.payment.klarna.CheckoutComKlarnaFacade;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.ui.Model;

import java.util.concurrent.ExecutionException;

/**
 * Implementation of {@link CheckoutComPaymentAttributeStrategy} for Klarna payment
 */
public class CheckoutComKlarnaPaymentAttributeStrategy extends CheckoutComAbstractPaymentAttributeStrategy {

    protected static final Logger LOG = LogManager.getLogger(CheckoutComKlarnaPaymentAttributeStrategy.class);

    protected static final String KLARNA_CLIENT_TOKEN_MODEL_ATTRIBUTE = "klarnaClientToken";

    protected final CheckoutComKlarnaFacade checkoutComKlarnaFacade;

    public CheckoutComKlarnaPaymentAttributeStrategy(final CheckoutComPaymentAttributesStrategyMapper checkoutComPaymentAttributesStrategyMapper,
                                                     final CheckoutComKlarnaFacade checkoutComKlarnaFacade) {
        super(checkoutComPaymentAttributesStrategyMapper);
        this.checkoutComKlarnaFacade = checkoutComKlarnaFacade;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addPaymentAttributeToModel(final Model model) {
        KlarnaClientTokenData klarnaClientToken;

        try {
            klarnaClientToken = checkoutComKlarnaFacade.getKlarnaClientToken();
        } catch (final ExecutionException | IllegalArgumentException e) {
            klarnaClientToken = new KlarnaClientTokenData();
            klarnaClientToken.setSuccess(Boolean.FALSE);
            LOG.error("Error getting the Klarna client token from checkout.com", e);
        }
        model.addAttribute(KLARNA_CLIENT_TOKEN_MODEL_ATTRIBUTE, klarnaClientToken);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CheckoutComPaymentType getStrategyKey() {
        return CheckoutComPaymentType.KLARNA;
    }
}
