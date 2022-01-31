package com.checkout.hybris.core.payment.response.strategies.impl;

import com.checkout.hybris.core.authorisation.AuthorizeResponse;
import com.checkout.hybris.core.model.CheckoutComAPMPaymentInfoModel;
import com.checkout.hybris.core.model.CheckoutComBenefitPayPaymentInfoModel;
import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.core.payment.exception.CheckoutComPaymentIntegrationException;
import com.checkout.hybris.core.payment.response.mappers.CheckoutComPaymentResponseStrategyMapper;
import com.checkout.hybris.core.payment.response.strategies.CheckoutComPaymentResponseStrategy;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentInfoService;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentIntegrationService;
import com.checkout.payments.AlternativePaymentSourceResponse;
import com.checkout.payments.GetPaymentResponse;
import com.checkout.payments.PaymentPending;
import com.google.common.base.Preconditions;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.BENEFITPAY;
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;
import static org.assertj.core.util.Preconditions.checkArgument;

/**
 * Specific {@link CheckoutComPaymentResponseStrategy} implementation for BenefitPay apm payment responses
 */
public class CheckoutComBenefitPayPaymentResponseStrategy extends CheckoutComAbstractPaymentResponseStrategy {

    protected static final Logger LOG = LogManager.getLogger(CheckoutComBenefitPayPaymentResponseStrategy.class);
    protected static final String USER_DATA_KEY = "qr_data";

    protected final CheckoutComPaymentInfoService paymentInfoService;
    protected final CheckoutComPaymentIntegrationService checkoutComPaymentIntegrationService;

    public CheckoutComBenefitPayPaymentResponseStrategy(final CheckoutComPaymentResponseStrategyMapper checkoutComPaymentResponseStrategyMapper,
                                                        final CheckoutComPaymentInfoService paymentInfoService,
                                                        final CheckoutComPaymentIntegrationService checkoutComPaymentIntegrationService) {
        super(checkoutComPaymentResponseStrategyMapper);
        this.paymentInfoService = paymentInfoService;
        this.checkoutComPaymentIntegrationService = checkoutComPaymentIntegrationService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CheckoutComPaymentType getStrategyKey() {
        return BENEFITPAY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AuthorizeResponse handlePendingPaymentResponse(final PaymentPending paymentPendingResponse, final PaymentInfoModel paymentInfo) {
        validateParameterNotNull(paymentPendingResponse, "Payment response cannot be null");
        Preconditions.checkArgument(StringUtils.isNotBlank(paymentPendingResponse.getId()), "Payment id cannot be null");
        Preconditions.checkArgument(paymentInfo instanceof CheckoutComBenefitPayPaymentInfoModel, "Payment info null or unsupported type");

        final CheckoutComBenefitPayPaymentInfoModel benefitPayPaymentInfo = (CheckoutComBenefitPayPaymentInfoModel) paymentInfo;
        GetPaymentResponse paymentDetails;
        try {
            paymentDetails = checkoutComPaymentIntegrationService.getPaymentDetails(paymentPendingResponse.getId());
        } catch (final CheckoutComPaymentIntegrationException e) {
            return populateAuthorizeResponse(benefitPayPaymentInfo, false);
        }

        if (paymentDetails != null && paymentDetails.getSource() instanceof AlternativePaymentSourceResponse) {
            setPaymentId(benefitPayPaymentInfo, paymentDetails);
            final AlternativePaymentSourceResponse source = (AlternativePaymentSourceResponse) paymentDetails.getSource();
            checkArgument(source.containsKey(USER_DATA_KEY) && StringUtils.isNotBlank((String) source.get(USER_DATA_KEY)), "QR Data cannot be null for benefitPay.");
            paymentInfoService.addQRCodeDataToBenefitPaymentInfo(benefitPayPaymentInfo, (String) source.get(USER_DATA_KEY));
        } else {
            throw new IllegalArgumentException("The current payment details response does not have a valid AlternativePaymentSourceResponse. The current payment info cannot be updated with user data.");
        }
        return populateAuthorizeResponse(benefitPayPaymentInfo, true);
    }

    private void setPaymentId(final CheckoutComBenefitPayPaymentInfoModel benefitPaymentInfo, final GetPaymentResponse paymentDetails) {
        final String paymentId = paymentDetails.getId();
        Preconditions.checkArgument(StringUtils.isNotBlank(paymentId), "Payment id cannot be empty.");
        benefitPaymentInfo.setPaymentId(paymentId);
    }

    protected AuthorizeResponse populateAuthorizeResponse(final CheckoutComAPMPaymentInfoModel paymentInfo, final boolean isSuccessful) {
        final AuthorizeResponse response = new AuthorizeResponse();
        response.setIsRedirect(false);
        response.setIsSuccess(isSuccessful);
        response.setIsDataRequired(paymentInfo.getUserDataRequired());

        return response;
    }

}
