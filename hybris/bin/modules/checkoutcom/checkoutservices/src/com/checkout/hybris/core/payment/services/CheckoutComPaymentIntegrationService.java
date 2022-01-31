package com.checkout.hybris.core.payment.services;

import com.checkout.hybris.core.klarna.session.request.KlarnaSessionRequestDto;
import com.checkout.hybris.core.klarna.session.response.KlarnaSessionResponseDto;
import com.checkout.payments.*;
import com.checkout.sources.SourceRequest;
import com.checkout.sources.SourceResponse;
import com.checkout.tokens.TokenResponse;
import com.checkout.tokens.WalletTokenRequest;

import java.util.concurrent.ExecutionException;

/**
 * This interface exposes methods to integrate with Checkout.com
 */
public interface CheckoutComPaymentIntegrationService {

    /**
     * Performs the authorize request with checkout.com
     *
     * @param paymentRequest the populated request
     * @return PaymentResponse the checkout.com response
     */
    PaymentResponse authorizePayment(PaymentRequest<RequestSource> paymentRequest);

    /**
     * Fetches and views relevant information and parameters related to a specific payment.
     *
     * @param paymentIdentifier The current payment identifier is provided
     * @return GetPaymentResponse the checkout.com response
     */
    GetPaymentResponse getPaymentDetails(String paymentIdentifier);

    /**
     * Performs the capture request with checkout.com
     *
     * @param captureRequest the populated request
     * @param paymentId      the checkout.com unique payment id
     * @return CaptureResponse the capture payment response
     */
    CaptureResponse capturePayment(CaptureRequest captureRequest, String paymentId) throws ExecutionException, InterruptedException;

    /**
     * Performs the refund request with checkout.com
     *
     * @param refundRequest the populated request
     * @param paymentId     the checkout.com unique payment id
     * @return RefundResponse the refund payment response
     */
    RefundResponse refundPayment(RefundRequest refundRequest, String paymentId) throws ExecutionException, InterruptedException;

    /**
     * Performs the void payment request with checkout.com
     *
     * @param refundRequest the populated request
     * @param paymentId     the checkout.com unique payment id
     * @return VoidResponse the void payment response
     */
    VoidResponse voidPayment(VoidRequest refundRequest, String paymentId) throws ExecutionException, InterruptedException;

    /**
     * Sets up the payment source for the checkout.com payment
     *
     * @param sourceRequest the populated payment source request
     * @return SourceResponse the source payment response
     */
    SourceResponse setUpPaymentSource(SourceRequest sourceRequest);

    /**
     * Generates the wallet payment token with checkout.com
     *
     * @param walletTokenRequest the generate wallet token request
     * @return the TokenResponse payment response
     */
    TokenResponse generateWalletPaymentToken(WalletTokenRequest walletTokenRequest);

    /**
     * Creates the Klarna session calling the checkout.com api
     *
     * @param klarnaSessionRequestDto the Klarna session request populated
     * @return the Klarna session response
     */
    KlarnaSessionResponseDto createKlarnaSession(KlarnaSessionRequestDto klarnaSessionRequestDto) throws ExecutionException;

}