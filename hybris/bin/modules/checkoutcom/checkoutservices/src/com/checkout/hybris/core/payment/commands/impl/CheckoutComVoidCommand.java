package com.checkout.hybris.core.payment.commands.impl;

import com.checkout.hybris.core.payment.request.CheckoutComRequestFactory;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentIntegrationService;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentTransactionService;
import com.checkout.payments.VoidResponse;
import de.hybris.platform.payment.commands.VoidCommand;
import de.hybris.platform.payment.commands.request.VoidRequest;
import de.hybris.platform.payment.commands.result.AbstractResult;
import de.hybris.platform.payment.commands.result.VoidResult;
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
 * Implements the checkout.com void command calling the void authorize sdk function
 */
public class CheckoutComVoidCommand extends CheckoutComAbstractCommand implements VoidCommand {

    protected static final Logger LOG = LogManager.getLogger(CheckoutComVoidCommand.class);

    protected final CheckoutComRequestFactory checkoutComRequestFactory;
    protected final CheckoutComPaymentIntegrationService checkoutComPaymentIntegrationService;
    protected final TimeService timeService;
    protected final CheckoutComPaymentTransactionService checkoutComPaymentTransactionService;

    public CheckoutComVoidCommand(final CheckoutComRequestFactory checkoutComRequestFactory,
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
    public VoidResult perform(final VoidRequest voidRequest) {
        validateParameterNotNull(voidRequest, "VoidRequest cannot be null");
        validateParameterNotNull(voidRequest.getMerchantTransactionCode(), "MerchantTransactionCode cannot be null");
        validateParameterNotNull(voidRequest.getCurrency(), "Currency cannot be null");
        validateParameterNotNull(voidRequest.getTotalAmount(), "Order amount cannot be null");
        validateParameterNotNull(voidRequest.getRequestId(), "The order reference code cannot be null");

        final String paymentReferenceNumber = checkoutComPaymentTransactionService.getPaymentReferenceFromTransactionEntryCode(voidRequest.getMerchantTransactionCode());

        try {
            return voidAuthorization(voidRequest, paymentReferenceNumber);
        } catch (final InterruptedException e) {
            LOG.error("Interrupted exception [{}] while voiding the authorized payment with Checkout.com for payment reference [{}]", e.getMessage(), paymentReferenceNumber);
            Thread.currentThread().interrupt();
            return (VoidResult) handleInterruptedException();
        } catch (final CancellationException e) {
            LOG.error("Interrupted exception [{}] while voiding the authorized payment with Checkout.com for payment reference [{}]", e.getMessage(), paymentReferenceNumber);
            return (VoidResult) handleCancellationException();
        } catch (final ExecutionException e) {
            LOG.error("ExecutionException [{}] while voiding the authorized payment with Checkout.com for payment reference [{}]", e.getMessage(), paymentReferenceNumber);
            return (VoidResult) handleExecutionException(e);
        }
    }

    protected VoidResult voidAuthorization(final VoidRequest voidRequest, final String paymentReferenceNumber) throws InterruptedException, ExecutionException {
        final com.checkout.payments.VoidRequest voidPaymentRequest = checkoutComRequestFactory.createVoidPaymentRequest(paymentReferenceNumber);

        final VoidResponse voidResponse = checkoutComPaymentIntegrationService.voidPayment(voidPaymentRequest, voidRequest.getRequestId());

        final VoidResult voidResult = new VoidResult();
        voidResult.setCurrency(voidRequest.getCurrency());
        voidResult.setRequestToken(voidResponse.getActionId());
        voidResult.setAmount(voidRequest.getTotalAmount());
        voidResult.setRequestId(voidRequest.getRequestId());
        voidResult.setRequestTime(timeService.getCurrentTime());
        voidResult.setTransactionStatus(TransactionStatus.PENDING);
        voidResult.setTransactionStatusDetails(TransactionStatusDetails.SUCCESFULL);

        return voidResult;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AbstractResult createErrorResult(final TransactionStatusDetails transactionStatusDetails) {
        final VoidResult voidResult = new VoidResult();
        voidResult.setTransactionStatus(ERROR);
        voidResult.setTransactionStatusDetails(transactionStatusDetails);
        return voidResult;
    }
}
