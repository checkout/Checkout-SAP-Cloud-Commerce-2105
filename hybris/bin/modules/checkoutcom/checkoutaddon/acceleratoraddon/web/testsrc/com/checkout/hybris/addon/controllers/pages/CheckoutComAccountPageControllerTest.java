package com.checkout.hybris.addon.controllers.pages;

import com.checkout.hybris.addon.forms.CreditCardDataForm;
import com.checkout.hybris.commercefacades.user.CheckoutComUserFacade;
import com.checkout.hybris.core.payment.exception.CheckoutComPaymentIntegrationException;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.commercefacades.customer.CustomerFacade;
import de.hybris.platform.commercefacades.order.data.CCPaymentInfoData;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Stubber;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComAccountPageControllerTest {

	private static final String CODE = "8999645";

	private static final String EDIT_PAYMENT_DETAILS_CMS_PAGE = "edit-payment-details";
	private static final String PAGE_STRING = "PAGE";

	private static final String REDIRECT_PREFIX = "redirect:";
	private static final String MY_ACCOUNT_PAYMENT_DETAILS_URL = "/my-account/payment-details";

	private static final String REDIRECT_TO_EDIT_PAYMENT_INFO_PAGE =
			REDIRECT_PREFIX + MY_ACCOUNT_PAYMENT_DETAILS_URL + "/edit/";
	private static final String REDIRECT_TO_PAYMENT_INFO_PAGE = REDIRECT_PREFIX + MY_ACCOUNT_PAYMENT_DETAILS_URL;

	@Spy
	@InjectMocks
	private CheckoutComAccountPageController testObj;

	@Mock
	private CheckoutComUserFacade checkoutComUserFacadeMock;

	@Mock
	private CustomerFacade customerFacade;

	@Mock
	private Converter<CreditCardDataForm, CCPaymentInfoData> creditCardDataFormCCPaymentInfoDataConverterMock;


	private Model model = new RedirectAttributesModelMap();
	private CCPaymentInfoData ccPaymentInfoForCode = new CCPaymentInfoData();
	private CustomerData currentCustomer = new CustomerData();

	private CreditCardDataForm creditCardDataForm = new CreditCardDataForm();
	private RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();
	private CheckoutComPaymentIntegrationException checkoutComPaymentIntegrationException = new CheckoutComPaymentIntegrationException("Something went wrong when calling Checkout API");;

	@Test
	public void getEditPaymentDetails_shouldReturnThePageReturnedByGetModelViewForPage() throws CMSItemNotFoundException {
		ensureModelIsPopulated();
		ensurePageIsReturnted();

		String result = testObj.getEditPaymentDetails(CODE, model);

		assertThat(result).isEqualTo(PAGE_STRING);
	}

	@Test
	public void postEditPaymentDetails_souldRedirectToPaymentInfoPageWhenEverythingGoesFine() {
		ensurePaymentDataIsPopulatedWithFormData();
		updatingCCPaymentInfoDoes(doNothing());

		String result = testObj.postEditPaymentDetails(CODE, creditCardDataForm, model, redirectAttributes);

		assertThat(result).isEqualTo(REDIRECT_TO_PAYMENT_INFO_PAGE);
	}
	@Test
	public void postEditPaymentDetails_shouldRedirectToCurrentEditPageWhenSomethingGoesWrong() {
		ensurePaymentDataIsPopulatedWithFormData();
		updatingCCPaymentInfoDoes(doThrow(checkoutComPaymentIntegrationException));

		String result = testObj.postEditPaymentDetails(CODE, creditCardDataForm, model, redirectAttributes);

		assertThat(result).isEqualTo(REDIRECT_TO_EDIT_PAYMENT_INFO_PAGE + CODE);
	}

	@Test
	public void removePaymentMethod_shouldReturnToPaymentInfoPageWhenEverythingGoesFine() {
		removingCCPaymentInfoDoes(doNothing());

		String result = testObj.removePaymentMethod(CODE, redirectAttributes);

		assertThat(result).isEqualTo(REDIRECT_TO_PAYMENT_INFO_PAGE);
	}


	@Test
	public void removePaymentMethod_shouldReturnToPaymentInfoPageWhenSomethingGoesWrong() {
		removingCCPaymentInfoDoes(doThrow(checkoutComPaymentIntegrationException));

		String result = testObj.removePaymentMethod(CODE, redirectAttributes);

		assertThat(result).isEqualTo(REDIRECT_TO_PAYMENT_INFO_PAGE);
	}

	private void ensurePageIsReturnted() {
		doReturn(PAGE_STRING).when(testObj).callSuperGetModelViewForPage(model);
	}

	private void ensureModelIsPopulated() throws CMSItemNotFoundException {
		when(checkoutComUserFacadeMock.getCCPaymentInfoForCode(CODE)).thenReturn(ccPaymentInfoForCode);
		when(customerFacade.getCurrentCustomer()).thenReturn(currentCustomer);
		doNothing().when(testObj).storeCmsPageInModel(model, EDIT_PAYMENT_DETAILS_CMS_PAGE);
	}

	private void updatingCCPaymentInfoDoes(Stubber doNothing) {
		doNothing.when(checkoutComUserFacadeMock).updateCCPaymentInfo(ccPaymentInfoForCode);
	}

	private void ensurePaymentDataIsPopulatedWithFormData() {
		when(checkoutComUserFacadeMock.getCCPaymentInfoForCode(CODE)).thenReturn(ccPaymentInfoForCode);
		doReturn(ccPaymentInfoForCode).when(creditCardDataFormCCPaymentInfoDataConverterMock).convert(creditCardDataForm, ccPaymentInfoForCode);
	}

	private void removingCCPaymentInfoDoes(Stubber doNothing) {
		doNothing.when(checkoutComUserFacadeMock).removeCCPaymentInfo(CODE);
	}

}
