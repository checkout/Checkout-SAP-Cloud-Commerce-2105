package com.checkout.hybris.addon.controllers.pages.checkout.steps;

import com.checkout.hybris.facades.accelerator.CheckoutComCheckoutFlowFacade;
import com.checkout.hybris.facades.beans.AuthorizeResponseData;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.acceleratorfacades.order.AcceleratorCheckoutFacade;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.order.InvalidCartException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.ui.Model;
import org.springframework.validation.support.BindingAwareModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComSummaryCheckoutStepControllerTest {

	@Spy
	@InjectMocks
	private CheckoutComSummaryCheckoutStepController testObj;
	@Mock
	private CheckoutComCheckoutFlowFacade checkoutComCheckoutFlowFacadeMock;
	@Mock
	private AcceleratorCheckoutFacade acceleratorCheckoutFacadeMock;

	final Model model = new BindingAwareModelMap();
	final RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

	final OrderData orderData = new OrderData();

	@Test
	public void authorisePlaceOrderAndRedirectToResultPage_shouldRedirectTo3DSecure_WhenAuthorisationIsSuccessfulAndRedirectIsTrue() throws CMSItemNotFoundException {
		authorizePaymentWithResponseData(true, true, "/3dsecure");

		final String result = testObj.authorisePlaceOrderAndRedirectToResultPage(model, redirectAttributes);

		assertThat(result).isEqualTo("redirect:/3dsecure");
	}

	@Test
	public void authorisePlaceOrderAndRedirectToResultPage_shouldRedirectToChoosePaymentMethodAndRemovePaymentInfoFromSessionWhenAuthoriseIsNotSuccessful() throws CMSItemNotFoundException {
		authorizePaymentWithResponseData(false, false, null);

		final String result = testObj.authorisePlaceOrderAndRedirectToResultPage(model, redirectAttributes);

		verify(checkoutComCheckoutFlowFacadeMock).removePaymentInfoFromSessionCart();
		assertThat(result).isEqualTo("redirect:/checkout/multi/checkout-com/choose-payment-method");
	}

	@Test
	public void authorisePlaceOrderAndRedirectToResultPage_shouldPlaceTheOrderAndRedirectToConfirmationPage() throws CMSItemNotFoundException, InvalidCartException {
		authorizePaymentWithResponseData(true, false, null);
		ensurePlaceOrderIsSuccessful();
		ensureRedirectToOrderPageRedirectsTo("redirect:/confirmationPage");

		final String result = testObj.authorisePlaceOrderAndRedirectToResultPage(model, redirectAttributes);


		assertThat(result).isEqualTo("redirect:/confirmationPage");
	}

	@Test
	public void authorisePlaceOrderAndRedirectToResultPage_shouldRemovePaymentInfoFromCartAndRedirectToEnterStep() throws CMSItemNotFoundException, InvalidCartException {
		authorizePaymentWithResponseData(true, false, null);
		ensurePlaceOrderIsUnsuccessful();
		ensureEnterStepReturnsRedirectToView(model, redirectAttributes);

		final String result = testObj.authorisePlaceOrderAndRedirectToResultPage(model, redirectAttributes);

		verify(checkoutComCheckoutFlowFacadeMock).removePaymentInfoFromSessionCart();
		assertThat(result).isEqualTo("redirect:/view");
	}

	private void ensureEnterStepReturnsRedirectToView(final Model model, final RedirectAttributes redirectAttributes) throws CMSItemNotFoundException {
		doReturn("redirect:/view").when(testObj).enterStep(model, redirectAttributes);
	}

	private void ensureRedirectToOrderPageRedirectsTo(final String redirectUrl) {
		doReturn(redirectUrl).when(testObj).redirectToOrderConfirmationPage(orderData);
	}

	private void ensurePlaceOrderIsSuccessful() throws InvalidCartException {
		when(acceleratorCheckoutFacadeMock.placeOrder()).thenReturn(orderData);
	}

	private void ensurePlaceOrderIsUnsuccessful() throws InvalidCartException {
		doThrow(new InvalidCartException("Cart is invalid")).when(acceleratorCheckoutFacadeMock).placeOrder();
	}

	private void authorizePaymentWithResponseData(final boolean success, final boolean redirect,
												  final String redirectUrl) {
		final AuthorizeResponseData authorizeResponseData = new AuthorizeResponseData();
		authorizeResponseData.setIsSuccess(success);
		authorizeResponseData.setRedirectUrl(redirectUrl);
		authorizeResponseData.setIsRedirect(redirect);
		when(checkoutComCheckoutFlowFacadeMock.authorizePayment()).thenReturn(authorizeResponseData);
	}
}
