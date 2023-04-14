package com.checkout.hybris.addon.controllers.pages.checkout.steps;

import com.checkout.hybris.addon.forms.PaymentDetailsForm;
import com.checkout.hybris.facades.payment.CheckoutComPaymentInfoFacade;
import de.hybris.bootstrap.annotations.UnitTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.support.BindingAwareModelMap;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComChoosePaymentAndBillingCheckoutStepControllerTest {

	private static final String PAYMENT_METHOD = "payment-method";
	@Spy
	@InjectMocks
	private CheckoutComChoosePaymentAndBillingCheckoutStepController testObj;

	@Mock
	private CheckoutComPaymentInfoFacade checkoutComPaymentInfoFacadeMock;
	private final Object paymentInfoData = new Object();
	final BindingAwareModelMap model = new BindingAwareModelMap();
	final PaymentDetailsForm paymentDetailsForm = new PaymentDetailsForm();

	final MapBindingResult bindingResult = new MapBindingResult(Map.of(), "errors");

	@Before
	public void setUp() throws Exception {
		ensureHandleAndSaveAddressesDoesNothing();
		paymentDetailsForm.setPaymentMethod(PAYMENT_METHOD);
	}

	@Test
	public void setPaymentToken_shouldCreateThePaymentInfoDataAndSetItToTheCart_WhenThereAreNoErrorsInTheForm() {
		ensureNoErrorsInForm();
		ensureCreatePaymentReturnsThePaymentInfoData();

		final ResponseEntity<Void> result = testObj.setPaymentToken(model, paymentDetailsForm, bindingResult);

		verify(testObj).handleAndSaveAddresses(paymentDetailsForm);
		verify(checkoutComPaymentInfoFacadeMock).createPaymentInfoData(PAYMENT_METHOD);
		verify(checkoutComPaymentInfoFacadeMock).addPaymentInfoToCart(paymentInfoData);
		assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	@Test
	public void setPaymentToken_shouldReturnBadRequest_WhenThereAreErrorsInTheForm() {
		ensureThereAreErrorsInTheForm();

		final ResponseEntity<Void> result = testObj.setPaymentToken(model, paymentDetailsForm, bindingResult);

		assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	private void ensureCreatePaymentReturnsThePaymentInfoData() {
		when(checkoutComPaymentInfoFacadeMock.createPaymentInfoData(paymentDetailsForm.getPaymentMethod())).thenReturn(
				paymentInfoData);
	}

	private void ensureHandleAndSaveAddressesDoesNothing() {
		doNothing().when(testObj).handleAndSaveAddresses(paymentDetailsForm);
	}

	private void ensureNoErrorsInForm() {
		doReturn(false).when(testObj).addGlobalErrors(model, bindingResult);
	}

	private void ensureThereAreErrorsInTheForm() {
		doReturn(true).when(testObj).addGlobalErrors(model, bindingResult);
	}
}
