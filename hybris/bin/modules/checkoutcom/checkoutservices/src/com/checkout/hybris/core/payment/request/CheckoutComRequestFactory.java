package com.checkout.hybris.core.payment.request;


import com.checkout.payments.*;
import de.hybris.platform.core.model.order.CartModel;

import java.math.BigDecimal;

/**
 * Factory interface to build the payment requests for checkout.com
 */
public interface CheckoutComRequestFactory {

    /**
     * Creates the payment request with token or source id for Checkout.com
     *
     * @param cartModel the session cart
     * @return the payment request
     */
    PaymentRequest<RequestSource> createPaymentRequest(CartModel cartModel);

    /**
     * Creates the capture request to Checkout.com
     *
     * @param amount           the order amount
     * @param paymentReference the unique payment reference ID
     * @param currencyCode     the order currency code
     * @return CaptureRequest
     */
    CaptureRequest createCapturePaymentRequest(BigDecimal amount, String paymentReference, String currencyCode);

    /**
     * Creates the refund request to Checkout.com
     *
     * @param amount           the order amount
     * @param paymentReference the unique payment reference ID
     * @param currencyCode     the order currency code
     * @return RefundRequest
     */
    RefundRequest createRefundPaymentRequest(BigDecimal amount, String paymentReference, String currencyCode);

    /**
     * Creates the void request to Checkout.com
     *
     * @param paymentReference the unique payment reference ID
     * @return VoidRequest
     */
    VoidRequest createVoidPaymentRequest(String paymentReference);
}
