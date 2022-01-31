package com.checkout.hybris.core.payment.response.strategies.impl;

import com.checkout.hybris.core.authorisation.AuthorizeResponse;
import com.checkout.hybris.core.model.CheckoutComSepaPaymentInfoModel;
import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.core.payment.response.mappers.CheckoutComPaymentResponseStrategyMapper;
import com.checkout.hybris.core.payment.response.strategies.CheckoutComPaymentResponseStrategy;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentInfoService;
import com.checkout.payments.PaymentPending;
import com.google.common.base.Preconditions;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

/**
 * Specific {@link CheckoutComPaymentResponseStrategy} implementation for Sepa apm payment responses
 */
public class CheckoutComSepaPaymentResponseStrategy extends CheckoutComAbstractPaymentResponseStrategy {

    protected final CheckoutComPaymentInfoService paymentInfoService;

    public CheckoutComSepaPaymentResponseStrategy(final CheckoutComPaymentResponseStrategyMapper checkoutComPaymentResponseStrategyMapper,
                                                  final CheckoutComPaymentInfoService paymentInfoService) {
        super(checkoutComPaymentResponseStrategyMapper);
        this.paymentInfoService = paymentInfoService;
    }

    @Override
    protected CheckoutComPaymentType getStrategyKey() {
        return CheckoutComPaymentType.SEPA;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AuthorizeResponse handlePendingPaymentResponse(final PaymentPending paymentPendingResponse, final PaymentInfoModel paymentInfo) {
        validateParameterNotNull(paymentPendingResponse, "Payment pending response cannot be null");
        Preconditions.checkArgument(paymentInfo instanceof CheckoutComSepaPaymentInfoModel, "Payment info null or not valid for Sepa.");

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
