package com.checkout.hybris.core.payment.services.impl;

import com.checkout.CheckoutApi;
import com.checkout.hybris.core.payment.exception.CheckoutComPaymentIntegrationException;
import com.checkout.hybris.core.payment.services.CheckoutComApiService;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentInstrumentsService;
import com.checkout.instruments.InstrumentsClient;
import com.checkout.instruments.UpdateInstrumentRequest;
import de.hybris.platform.core.model.order.payment.CreditCardPaymentInfoModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutionException;

public class DefaultCheckoutComPaymentInstrumentService implements CheckoutComPaymentInstrumentsService {

	private static final Logger LOG = LogManager.getLogger(DefaultCheckoutComPaymentInstrumentService.class);
	private static final String INSTRUMENT_REMOVAL_FAILED = "Instrument removal failed";
	private static final String INSTRUMENT_UPDATE_FAILED = "Instrument update failed";
	private final CheckoutComApiService checkoutComApiService;


	public DefaultCheckoutComPaymentInstrumentService(final CheckoutComApiService checkoutComApiService) {
		this.checkoutComApiService = checkoutComApiService;
	}

	@Override
	public void removeInstrumentByCreditCard(final CreditCardPaymentInfoModel creditCardPaymentInfoModel) {
		try {
			LOG.debug("Trying to remove credit card with code [{}]", creditCardPaymentInfoModel.getCode());
			getInstrumentsClient().deleteInstrument(creditCardPaymentInfoModel.getSubscriptionId()).get();
			LOG.debug("Removal successful for credit card with code [{}]", creditCardPaymentInfoModel.getCode());
		}
		catch (final InterruptedException e) {
			LOG.error("Error while removing the instrument associated with credit card with code [{}]",
					  creditCardPaymentInfoModel.getCode());
			Thread.currentThread().interrupt();
			throw new CheckoutComPaymentIntegrationException(INSTRUMENT_REMOVAL_FAILED, e);
		}
		catch (final ExecutionException e) {
			LOG.error("Error while removing the instrument associated with credit card with code [{}]",
					  creditCardPaymentInfoModel.getCode());
			throw new CheckoutComPaymentIntegrationException(INSTRUMENT_REMOVAL_FAILED, e);
		}
	}

	@Override
	public void updateInstrumentByCreditCard(final CreditCardPaymentInfoModel creditCardPaymentInfoModel) {
		try {
			LOG.debug("Trying to update credit card with code [{}]", creditCardPaymentInfoModel.getCode());
			final UpdateInstrumentRequest updateInstrumentRequest = createUpdateInstrumentRequest(
					creditCardPaymentInfoModel.getCcOwner(), creditCardPaymentInfoModel.getValidToYear(),
					creditCardPaymentInfoModel.getValidToMonth());
			getInstrumentsClient().updateInstrument(creditCardPaymentInfoModel.getSubscriptionId(),
													updateInstrumentRequest).get();
			LOG.debug("Update successful for credit card with code [{}]", creditCardPaymentInfoModel.getCode());
		}
		catch (final InterruptedException e) {
			LOG.error("Error while updating the instrument associated with credit card with code [{}]",
					  creditCardPaymentInfoModel.getCode());
			Thread.currentThread().interrupt();
			throw new CheckoutComPaymentIntegrationException(INSTRUMENT_UPDATE_FAILED, e);
		}
		catch (final ExecutionException e) {
			LOG.error("Error while updating the instrument associated with credit card with code [{}]",
					  creditCardPaymentInfoModel.getCode());
			throw new CheckoutComPaymentIntegrationException(INSTRUMENT_UPDATE_FAILED, e);
		}
	}

	protected UpdateInstrumentRequest createUpdateInstrumentRequest(final String cardHolderName,
																	final String validToYear,
																	final String validToMonth) {
		final UpdateInstrumentRequest updateInstrumentRequest = new UpdateInstrumentRequest();
		updateInstrumentRequest.setName(cardHolderName);
		updateInstrumentRequest.setExpiryYear(Integer.parseInt(validToYear));
		updateInstrumentRequest.setExpiryMonth(Integer.parseInt(validToMonth));

		return updateInstrumentRequest;
	}

	protected InstrumentsClient getInstrumentsClient() {
		final CheckoutApi checkoutApi = checkoutComApiService.createCheckoutApi();
		
		return checkoutApi.instrumentsClient();
	}
}
