package com.checkout.hybris.core.payment.commands.impl;

import com.checkout.CheckoutApiException;
import com.checkout.hybris.core.payment.exception.CheckoutComPaymentIntegrationException;
import de.hybris.platform.payment.commands.result.AbstractResult;
import de.hybris.platform.payment.dto.TransactionStatusDetails;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutionException;

import static de.hybris.platform.payment.dto.TransactionStatusDetails.COMMUNICATION_PROBLEM;
import static de.hybris.platform.payment.dto.TransactionStatusDetails.INVALID_REQUEST;

/**
 * Class that contains methods and dependencies used by the implemented Commands
 */
public abstract class CheckoutComAbstractCommand {

    protected static final Logger LOG = LogManager.getLogger(CheckoutComAbstractCommand.class);

    /**
     * Handle the ExecutionException calling the checkout.com sdk
     *
     * @param e the exception
     * @return the populated result
     */
    protected AbstractResult handleExecutionException(final ExecutionException e) {
        final Throwable cause = e.getCause();
        if (cause instanceof CheckoutApiException) {
            final CheckoutApiException apiException = (CheckoutApiException) cause;
            final int httpStatusCode = apiException.getApiResponseInfo().getHttpStatusCode();
            if (httpStatusCode >= 500) {
                LOG.error("CheckoutApiException exception with status code [{}] found.", httpStatusCode);
                throw new CheckoutComPaymentIntegrationException(cause.getMessage());
            }
        }

        return createErrorResult(INVALID_REQUEST);
    }

    protected AbstractResult handleCancellationException() {
        return createErrorResult(COMMUNICATION_PROBLEM);
    }

    protected AbstractResult handleInterruptedException() {
        return createErrorResult(COMMUNICATION_PROBLEM);
    }

    /**
     * Abstract method to override specifically for each command
     *
     * @param transactionStatusDetails the transaction details status to set
     * @return the populated result
     */
    protected abstract AbstractResult createErrorResult(TransactionStatusDetails transactionStatusDetails);
}