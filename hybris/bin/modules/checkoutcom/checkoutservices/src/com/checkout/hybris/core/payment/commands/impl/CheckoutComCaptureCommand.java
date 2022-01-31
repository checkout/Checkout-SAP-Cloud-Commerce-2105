package com.checkout.hybris.core.payment.commands.impl;

import com.checkout.hybris.core.payment.request.CheckoutComRequestFactory;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentIntegrationService;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentTransactionService;
import com.checkout.payments.CaptureResponse;
import de.hybris.platform.payment.commands.CaptureCommand;
import de.hybris.platform.payment.commands.request.CaptureRequest;
import de.hybris.platform.payment.commands.result.AbstractResult;
import de.hybris.platform.payment.commands.result.CaptureResult;
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
 * Implements the checkout.com capture command calling the capture sdk function
 */
public class CheckoutComCaptureCommand extends CheckoutComAbstractCommand implements CaptureCommand {

    protected static final Logger LOG = LogManager.getLogger(CheckoutComCaptureCommand.class);

    protected final CheckoutComRequestFactory checkoutComRequestFactory;
    protected final CheckoutComPaymentIntegrationService checkoutComPaymentIntegrationService;
    protected final TimeService timeService;
    protected final CheckoutComPaymentTransactionService checkoutComPaymentTransactionService;

    public CheckoutComCaptureCommand(final CheckoutComRequestFactory checkoutComRequestFactory,
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
    public CaptureResult perform(final CaptureRequest captureRequest) {
        validateParameterNotNull(captureRequest, "CaptureRequest cannot be null");
        validateParameterNotNull(captureRequest.getMerchantTransactionCode(), "MerchantTransactionCode cannot be null");
        validateParameterNotNull(captureRequest.getCurrency(), "Currency cannot be null");
        validateParameterNotNull(captureRequest.getTotalAmount(), "Order amount cannot be null");
        validateParameterNotNull(captureRequest.getRequestId(), "The order reference code cannot be null");

        final String paymentReferenceNumber = checkoutComPaymentTransactionService.getPaymentReferenceFromTransactionEntryCode(captureRequest.getMerchantTransactionCode());

        try {
            return capture(captureRequest, paymentReferenceNumber);
        } catch (final InterruptedException e) {
            LOG.error("Interrupted exception [{}] while capturing the payment with Checkout.com for payment reference [{}]", e.getMessage(), paymentReferenceNumber);
            Thread.currentThread().interrupt();
            return (CaptureResult) handleInterruptedException();
        } catch (final CancellationException e) {
            LOG.error("Interrupted exception [{}] while capturing the payment with Checkout.com for payment reference [{}]", e.getMessage(), paymentReferenceNumber);
            return (CaptureResult) handleCancellationException();
        } catch (final ExecutionException e) {
            LOG.error("ExecutionException [{}] while capturing the payment with Checkout.com for payment reference [{}]", e.getMessage(), paymentReferenceNumber);
            return (CaptureResult) handleExecutionException(e);
        }
    }

    protected CaptureResult capture(final CaptureRequest captureRequest, final String paymentReferenceNumber) throws InterruptedException, ExecutionException {
        final com.checkout.payments.CaptureRequest capturePaymentRequest = checkoutComRequestFactory.createCapturePaymentRequest(captureRequest.getTotalAmount(), paymentReferenceNumber, captureRequest.getCurrency().getCurrencyCode());

        final CaptureResponse captureResponse = checkoutComPaymentIntegrationService.capturePayment(capturePaymentRequest, captureRequest.getRequestId());

        CaptureResult captureResult = new CaptureResult();
        captureResult.setCurrency(captureRequest.getCurrency());
        captureResult.setRequestToken(captureResponse.getActionId());
        captureResult.setTotalAmount(captureRequest.getTotalAmount());
        captureResult.setRequestId(captureRequest.getRequestId());
        captureResult.setRequestTime(timeService.getCurrentTime());
        captureResult.setTransactionStatus(TransactionStatus.PENDING);
        captureResult.setTransactionStatusDetails(TransactionStatusDetails.SUCCESFULL);

        return captureResult;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AbstractResult createErrorResult(final TransactionStatusDetails transactionStatusDetails) {
        final CaptureResult captureResult = new CaptureResult();
        captureResult.setTransactionStatus(ERROR);
        captureResult.setTransactionStatusDetails(transactionStatusDetails);
        return captureResult;
    }
}