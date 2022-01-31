package com.checkout.hybris.core.payment.response.strategies;

import com.checkout.hybris.core.authorisation.AuthorizeResponse;
import com.checkout.payments.PaymentPending;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;

/**
 * Handles checkout.com payment pending response
 */
public interface CheckoutComPaymentResponseStrategy {

    /**
     * Returns Authorize response populated according to specific payment type
     *
     * @param paymentPendingResponse the request payment response from checkout.com
     * @param paymentInfo            the payment info
     * @return a {@link AuthorizeResponse}
     */
    AuthorizeResponse handlePendingPaymentResponse(PaymentPending paymentPendingResponse, final PaymentInfoModel paymentInfo);
}
