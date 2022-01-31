package com.checkout.hybris.core.payment.commands.impl;

import com.checkout.hybris.core.payment.request.CheckoutComRequestFactory;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentIntegrationService;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentTransactionService;
import com.checkout.payments.RefundRequest;
import com.checkout.payments.RefundResponse;
import de.hybris.platform.payment.commands.FollowOnRefundCommand;
import de.hybris.platform.payment.commands.request.FollowOnRefundRequest;
import de.hybris.platform.payment.commands.result.AbstractResult;
import de.hybris.platform.payment.commands.result.RefundResult;
import de.hybris.platform.payment.dto.TransactionStatus;
import de.hybris.platform.payment.dto.TransactionStatusDetails;
import de.hybris.platform.servicelayer.time.TimeService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import static de.hybris.platform.payment.dto.TransactionStatus.ERROR;
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

/**
 * Implements the checkout.com refund command calling the refund sdk function
 */
public class CheckoutComRefundCommand extends CheckoutComAbstractCommand implements FollowOnRefundCommand<FollowOnRefundRequest> {

    protected static final Logger LOG = LogManager.getLogger(CheckoutComRefundCommand.class);

    protected final CheckoutComRequestFactory checkoutComRequestFactory;
    protected final CheckoutComPaymentIntegrationService checkoutComPaymentIntegrationService;
    protected final TimeService timeService;
    protected final CheckoutComPaymentTransactionService checkoutComPaymentTransactionService;

    public CheckoutComRefundCommand(final CheckoutComRequestFactory checkoutComRequestFactory,
                                    final CheckoutComPaymentIntegrationService checkoutComPaymentIntegrationService,
                                    final TimeService timeService,
                                    final CheckoutComPaymentTransactionService checkoutComPaymentTransactionService) {
        this.checkoutComRequestFactory = checkoutComRequestFactory;
        this.checkoutComPaymentIntegrationService = checkoutComPaymentIntegrationService;
        this.timeService = timeService;
        this.checkoutComPaymentTransactionService = checkoutComPaymentTransactionService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RefundResult perform(final FollowOnRefundRequest refundRequest) {
        validateParameterNotNull(refundRequest, "FollowOnRefundRequest cannot be null");
        validateParameterNotNull(refundRequest.getMerchantTransactionCode(), "MerchantTransactionCode cannot be null");
        validateParameterNotNull(refundRequest.getCurrency(), "Currency cannot be null");
        validateParameterNotNull(refundRequest.getTotalAmount(), "Order amount cannot be null");
        validateParameterNotNull(refundRequest.getRequestId(), "The order reference code cannot be null");

        final String paymentReferenceNumber = checkoutComPaymentTransactionService.getPaymentReferenceFromTransactionEntryCode(refundRequest.getMerchantTransactionCode());

        try {
            return refund(refundRequest, paymentReferenceNumber);
        } catch (final InterruptedException e) {
            LOG.error("Interrupted exception [{}] while refunding the payment with Checkout.com for payment reference [{}]", e.getMessage(), paymentReferenceNumber);
            Thread.currentThread().interrupt();
            return (RefundResult) handleInterruptedException();
        } catch (final CancellationException e) {
            LOG.error("Interrupted exception [{}] while refunding the payment with Checkout.com for payment reference [{}]", e.getMessage(), paymentReferenceNumber);
            return (RefundResult) handleCancellationException();
        } catch (final ExecutionException e) {
            LOG.error("ExecutionException [{}] while refunding the payment with Checkout.com for payment reference [{}]", e.getMessage(), paymentReferenceNumber);
            return (RefundResult) handleExecutionException(e);
        }
    }

    protected RefundResult refund(final FollowOnRefundRequest refundRequest, final String paymentReferenceNumber) throws InterruptedException, ExecutionException {
        final RefundRequest refundPaymentRequest = checkoutComRequestFactory.createRefundPaymentRequest(refundRequest.getTotalAmount(), paymentReferenceNumber, refundRequest.getCurrency().getCurrencyCode());

        final RefundResponse refundResponse = checkoutComPaymentIntegrationService.refundPayment(refundPaymentRequest, refundRequest.getRequestId());

        RefundResult refundResult = new RefundResult();
        refundResult.setCurrency(refundRequest.getCurrency());
        refundResult.setRequestToken(refundResponse.getActionId());
        refundResult.setTotalAmount(refundRequest.getTotalAmount());
        refundResult.setRequestId(refundRequest.getRequestId());
        refundResult.setRequestTime(timeService.getCurrentTime());
        refundResult.setTransactionStatus(TransactionStatus.PENDING);
        refundResult.setTransactionStatusDetails(TransactionStatusDetails.SUCCESFULL);

        return refundResult;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AbstractResult createErrorResult(final TransactionStatusDetails transactionStatusDetails) {
        final RefundResult refundResult = new RefundResult();
        refundResult.setTransactionStatus(ERROR);
        refundResult.setTransactionStatusDetails(transactionStatusDetails);
        return refundResult;
    }

}