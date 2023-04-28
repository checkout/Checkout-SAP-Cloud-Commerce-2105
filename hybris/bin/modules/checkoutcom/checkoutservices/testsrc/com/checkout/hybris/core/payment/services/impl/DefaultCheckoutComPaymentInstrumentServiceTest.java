package com.checkout.hybris.core.payment.services.impl;

import com.checkout.hybris.core.payment.exception.CheckoutComPaymentIntegrationException;
import com.checkout.instruments.InstrumentsClient;
import com.checkout.instruments.UpdateInstrumentRequest;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.payment.CreditCardPaymentInfoModel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultCheckoutComPaymentInstrumentServiceTest {

	private static final String SUBSCRIPTION_ID = "subscriptionId";

	private static final String INSTRUMENT_REMOVAL_FAILED = "Instrument removal failed";

	private static final String INSTRUMENT_UPDATE_FAILED = "Instrument update failed";
	private static final String CC_OWNER = "Mike Hammer";
	private static final String VALID_TO_YEAR = "20";
	private static final String VALID_TO_MONTH = "12";
	@Spy
	@InjectMocks
	private DefaultCheckoutComPaymentInstrumentService testObj;

	@Mock
	private InstrumentsClient instrumentClientMock;

	private final CreditCardPaymentInfoModel creditCardPaymentInfoModel = new CreditCardPaymentInfoModel();

	@Spy
	private CompletableFuture<Object> removeInstrumentsCompletableFuture;

	@Spy
	private CompletableFuture<Object> updateInstrumentsCompletableFuture;
	private final InterruptedException interruptedException = new InterruptedException();
	private final ExecutionException executionException = new ExecutionException(new RuntimeException());
	@Captor
	private ArgumentCaptor<UpdateInstrumentRequest> captureUpdateInstrumentRequest;


	@Before
	public void setUp() throws Exception {
		doReturn(instrumentClientMock).when(testObj).getInstrumentsClient();

	}

	@Test
	public void removeInstrumentByCreditCard_shouldCallDeleteInstrumentWithSubscriptionId() {
		ensureCreditCardDetailsAreSet(SUBSCRIPTION_ID, null, null, null);
		ensureRemoveInstrumentsClientsDoesNothing();

		testObj.removeInstrumentByCreditCard(creditCardPaymentInfoModel);

		verify(instrumentClientMock).deleteInstrument(SUBSCRIPTION_ID);
	}

	@Test
	public void removeInstrumentByCreditCard_shouldRaiseCheckoutComPaymentIntegrationException_WhenCompletableFutureThrowsInterruptedException() throws ExecutionException, InterruptedException {
		ensureCreditCardDetailsAreSet(SUBSCRIPTION_ID, null, null, null);
		ensureRemoveInstrumentsThrowsAnException(interruptedException);

		assertThatThrownBy(() -> testObj.removeInstrumentByCreditCard(creditCardPaymentInfoModel))
				.isInstanceOf(CheckoutComPaymentIntegrationException.class)
				.hasCause(interruptedException)
				.hasMessage(INSTRUMENT_REMOVAL_FAILED);
		
		verify(instrumentClientMock).deleteInstrument(SUBSCRIPTION_ID);
	}

	@Test
	public void removeInstrumentByCreditCard_shouldRaiseCheckoutComPaymentIntegrationException_WhenCompletableFutureThrowsExecutionException() throws ExecutionException, InterruptedException {
		ensureCreditCardDetailsAreSet(SUBSCRIPTION_ID, null, null, null);
		ensureRemoveInstrumentsThrowsAnException(executionException);

		assertThatThrownBy(() -> testObj.removeInstrumentByCreditCard(creditCardPaymentInfoModel))
				.isInstanceOf(CheckoutComPaymentIntegrationException.class)
				.hasCause(executionException)
				.hasMessage(INSTRUMENT_REMOVAL_FAILED);

		verify(instrumentClientMock).deleteInstrument(SUBSCRIPTION_ID);
	}


	@Test
	public void updateInstrumentByCreditCard_shouldCallUpdateInstrumentWithSubscriptionAndUpdateRequest() {
		ensureCreditCardDetailsAreSet(SUBSCRIPTION_ID, CC_OWNER, VALID_TO_YEAR, VALID_TO_MONTH);
		ensureUpdateInstrumentsClientsDoesNothing();

		testObj.updateInstrumentByCreditCard(creditCardPaymentInfoModel);

		verify(instrumentClientMock).updateInstrument(eq(SUBSCRIPTION_ID), captureUpdateInstrumentRequest.capture());
		final UpdateInstrumentRequest capturedUpdateInstrumentRequest = captureUpdateInstrumentRequest.getValue();

		assertThat(capturedUpdateInstrumentRequest.getName()).isEqualTo(CC_OWNER);
		assertThat(capturedUpdateInstrumentRequest.getExpiryMonth()).isEqualTo(Integer.valueOf(VALID_TO_MONTH));
		assertThat(capturedUpdateInstrumentRequest.getExpiryYear()).isEqualTo(Integer.valueOf(VALID_TO_YEAR));
	}

	@Test
	public void updateInstrumentByCreditCard_shouldRaiseAnInterruptedException_WhenCompletableFutureThrowsInterruptedException() throws ExecutionException, InterruptedException {
		ensureCreditCardDetailsAreSet(SUBSCRIPTION_ID, CC_OWNER, VALID_TO_YEAR, VALID_TO_MONTH);
		ensureUpdateInstrumentsThrowsAnException(interruptedException);

		assertThatThrownBy(() -> testObj.updateInstrumentByCreditCard(creditCardPaymentInfoModel))
				.isInstanceOf(CheckoutComPaymentIntegrationException.class)
				.hasCause(interruptedException)
				.hasMessage(INSTRUMENT_UPDATE_FAILED);

		verify(instrumentClientMock).updateInstrument(eq(SUBSCRIPTION_ID), captureUpdateInstrumentRequest.capture());
		final UpdateInstrumentRequest capturedUpdateInstrumentRequest = captureUpdateInstrumentRequest.getValue();

		assertThat(capturedUpdateInstrumentRequest.getName()).isEqualTo(CC_OWNER);
		assertThat(capturedUpdateInstrumentRequest.getExpiryMonth()).isEqualTo(Integer.valueOf(VALID_TO_MONTH));
		assertThat(capturedUpdateInstrumentRequest.getExpiryYear()).isEqualTo(Integer.valueOf(VALID_TO_YEAR));
	}

	@Test
	public void updateInstrumentByCreditCard_shouldRaiseAnInterruptedException_WhenCompletableFutureThrowsExecutionException() throws ExecutionException, InterruptedException {
		ensureCreditCardDetailsAreSet(SUBSCRIPTION_ID, CC_OWNER, VALID_TO_YEAR, VALID_TO_MONTH);
		ensureUpdateInstrumentsThrowsAnException(executionException);

		assertThatThrownBy(() -> testObj.updateInstrumentByCreditCard(creditCardPaymentInfoModel))
				.isInstanceOf(CheckoutComPaymentIntegrationException.class)
				.hasCause(executionException)
				.hasMessage(INSTRUMENT_UPDATE_FAILED);

		verify(instrumentClientMock).updateInstrument(eq(SUBSCRIPTION_ID), captureUpdateInstrumentRequest.capture());
		final UpdateInstrumentRequest capturedUpdateInstrumentRequest = captureUpdateInstrumentRequest.getValue();

		assertThat(capturedUpdateInstrumentRequest.getName()).isEqualTo(CC_OWNER);
		assertThat(capturedUpdateInstrumentRequest.getExpiryMonth()).isEqualTo(Integer.valueOf(VALID_TO_MONTH));
		assertThat(capturedUpdateInstrumentRequest.getExpiryYear()).isEqualTo(Integer.valueOf(VALID_TO_YEAR));
	}

	private void ensureCreditCardDetailsAreSet(final String subscriptionId, final String ccOwner,
											   final String validToYear,
											   final String validToMonth) {
		creditCardPaymentInfoModel.setSubscriptionId(subscriptionId);
		creditCardPaymentInfoModel.setCcOwner(ccOwner);
		creditCardPaymentInfoModel.setValidToMonth(validToMonth);
		creditCardPaymentInfoModel.setValidToYear(validToYear);
	}

	private void ensureRemoveInstrumentsClientsDoesNothing() {
		removeInstrumentsCompletableFuture = new CompletableFuture<>();
		doReturn(removeInstrumentsCompletableFuture).when(instrumentClientMock).deleteInstrument(SUBSCRIPTION_ID);
		ensureFutureCompletes(removeInstrumentsCompletableFuture);
	}

	private void ensureUpdateInstrumentsClientsDoesNothing() {
		updateInstrumentsCompletableFuture = new CompletableFuture<>();
		doReturn(updateInstrumentsCompletableFuture).when(instrumentClientMock).updateInstrument(eq(SUBSCRIPTION_ID),
																								 any(UpdateInstrumentRequest.class));
		ensureFutureCompletes(updateInstrumentsCompletableFuture);
	}

	private void ensureFutureCompletes(final CompletableFuture<Object> completableFuture) {
		completableFuture.complete(null);
	}


	private void ensureRemoveInstrumentsThrowsAnException(final Exception exception) throws ExecutionException,
			InterruptedException {
		doReturn(removeInstrumentsCompletableFuture).when(instrumentClientMock).deleteInstrument(SUBSCRIPTION_ID);
		doThrow(exception).when(removeInstrumentsCompletableFuture).get();
	}

	private void ensureUpdateInstrumentsThrowsAnException(final Exception exception) throws ExecutionException,
			InterruptedException {
		doReturn(updateInstrumentsCompletableFuture).when(instrumentClientMock).updateInstrument(eq(SUBSCRIPTION_ID),
																								 any(UpdateInstrumentRequest.class));
		doThrow(exception).when(updateInstrumentsCompletableFuture).get();
	}
}
