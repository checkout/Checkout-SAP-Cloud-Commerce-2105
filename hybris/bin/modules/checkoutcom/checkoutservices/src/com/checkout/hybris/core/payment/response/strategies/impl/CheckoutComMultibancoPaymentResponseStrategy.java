package com.checkout.hybris.core.payment.response.strategies.impl;

import com.checkout.hybris.core.authorisation.AuthorizeResponse;
import com.checkout.hybris.core.model.CheckoutComAPMPaymentInfoModel;
import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.core.payment.response.mappers.CheckoutComPaymentResponseStrategyMapper;
import com.checkout.hybris.core.payment.response.strategies.CheckoutComPaymentResponseStrategy;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentInfoService;
import com.checkout.payments.PaymentPending;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import org.apache.commons.collections.MapUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.MULTIBANCO;
import static com.google.common.base.Preconditions.checkArgument;
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

/**
 * Specific {@link CheckoutComPaymentResponseStrategy} implementation for Multibanco apm payment responses
 */
public class CheckoutComMultibancoPaymentResponseStrategy extends CheckoutComAbstractPaymentResponseStrategy {

    protected static final Logger LOG = LogManager.getLogger(CheckoutComMultibancoPaymentResponseStrategy.class);

    protected static final String MULTIBANCO_REDIRECT_LINK_KEY = "multibanco:static-reference-page";

    protected final CheckoutComPaymentInfoService paymentInfoService;

    public CheckoutComMultibancoPaymentResponseStrategy(final CheckoutComPaymentResponseStrategyMapper checkoutComPaymentResponseStrategyMapper,
                                                        final CheckoutComPaymentInfoService paymentInfoService) {
        super(checkoutComPaymentResponseStrategyMapper);
        this.paymentInfoService = paymentInfoService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CheckoutComPaymentType getStrategyKey() {
        return MULTIBANCO;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AuthorizeResponse handlePendingPaymentResponse(final PaymentPending paymentPendingResponse, final PaymentInfoModel paymentInfo) {
        validateParameterNotNull(paymentPendingResponse, "Payment pending response cannot be null");
        checkArgument(MapUtils.isNotEmpty(paymentPendingResponse.getLinks()), "Payment response links cannot be empty.");
        checkArgument(paymentPendingResponse.getLinks().containsKey(MULTIBANCO_REDIRECT_LINK_KEY), "Redirect Multibanco link is missing.");
        checkArgument(paymentInfo instanceof CheckoutComAPMPaymentInfoModel, "Payment info null or not valid for APMs.");

        paymentInfoService.addPaymentId(paymentPendingResponse.getId(), paymentInfo);

        return populateAuthorizeResponse(paymentPendingResponse, (CheckoutComAPMPaymentInfoModel) paymentInfo);
    }

    protected AuthorizeResponse populateAuthorizeResponse(final PaymentPending paymentPendingResponse, final CheckoutComAPMPaymentInfoModel paymentInfo) {
        final AuthorizeResponse response = new AuthorizeResponse();
        response.setIsRedirect(true);
        response.setIsSuccess(true);
        response.setIsDataRequired(paymentInfo.getUserDataRequired());
        response.setRedirectUrl(paymentPendingResponse.getLinks().get(MULTIBANCO_REDIRECT_LINK_KEY).getHref());

        return response;
    }
}