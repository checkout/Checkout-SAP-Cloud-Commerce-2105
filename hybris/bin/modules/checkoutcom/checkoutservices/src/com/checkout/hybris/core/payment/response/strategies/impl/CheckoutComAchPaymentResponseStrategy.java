package com.checkout.hybris.core.payment.response.strategies.impl;

import com.checkout.hybris.core.authorisation.AuthorizeResponse;
import com.checkout.hybris.core.model.CheckoutComAchPaymentInfoModel;
import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.core.payment.response.mappers.CheckoutComPaymentResponseStrategyMapper;
import com.checkout.hybris.core.payment.response.strategies.CheckoutComPaymentResponseStrategy;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentInfoService;
import com.checkout.payments.PaymentPending;
import com.google.common.base.Preconditions;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

/**
 * Specific {@link CheckoutComPaymentResponseStrategy} implementation for Ach apm payment responses
 */
public class CheckoutComAchPaymentResponseStrategy extends CheckoutComAbstractPaymentResponseStrategy {

    protected final CheckoutComPaymentInfoService paymentInfoService;

    public CheckoutComAchPaymentResponseStrategy(final CheckoutComPaymentResponseStrategyMapper checkoutComPaymentResponseStrategyMapper,
                                                 final CheckoutComPaymentInfoService paymentInfoService) {
        super(checkoutComPaymentResponseStrategyMapper);
        this.paymentInfoService = paymentInfoService;
    }

    @Override
    protected CheckoutComPaymentType getStrategyKey() {
        return CheckoutComPaymentType.ACH;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AuthorizeResponse handlePendingPaymentResponse(final PaymentPending paymentPendingResponse, final PaymentInfoModel paymentInfo) {
        validateParameterNotNull(paymentPendingResponse, "Payment pending response cannot be null");
        Preconditions.checkArgument(paymentInfo instanceof CheckoutComAchPaymentInfoModel, "Payment info null or not valid for Ach.");

        paymentInfoService.addPaymentId(paymentPendingResponse.getId(), paymentInfo);

        return populateAuthorizeResponse();
    }

    protected AuthorizeResponse populateAuthorizeResponse() {
        final AuthorizeResponse response = new AuthorizeResponse();
        response.setIsRedirect(false);
        response.setIsSuccess(true);
        response.setIsDataRequired(true);

        return response;
    }
}