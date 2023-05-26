package com.checkout.hybris.addon.controllers.pages;

import com.checkout.hybris.addon.forms.CreditCardDataForm;
import com.checkout.hybris.commercefacades.user.CheckoutComUserFacade;
import com.checkout.hybris.core.payment.exception.CheckoutComPaymentIntegrationException;
import de.hybris.platform.acceleratorstorefrontcommons.annotations.RequireHardLogIn;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.ThirdPartyConstants;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.pages.AbstractPageController;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.util.GlobalMessages;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.commercefacades.order.data.CCPaymentInfoData;
import de.hybris.platform.commercefacades.user.UserFacade;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;

@Controller
@RequestMapping("/my-account")
public class CheckoutComAccountPageController extends AbstractPageController {
	private static final String EDIT_PAYMENT_DETAILS_CMS_PAGE = "edit-payment-details";

	private static final String MY_ACCOUNT_PAYMENT_DETAILS_URL = "/my-account/payment-details";
	private static final String REDIRECT_TO_PAYMENT_INFO_PAGE = REDIRECT_PREFIX + MY_ACCOUNT_PAYMENT_DETAILS_URL;

	private static final String REDIRECT_TO_EDIT_PAYMENT_INFO_PAGE =
			REDIRECT_PREFIX + MY_ACCOUNT_PAYMENT_DETAILS_URL + "/edit/";


	@Resource
	protected CheckoutComUserFacade checkoutComUserFacade;
	@Resource(name = "checkoutComCreditCardDataFormToCCPaymentInfoDataConverter")
	protected Converter<CreditCardDataForm, CCPaymentInfoData> creditCardDataFormCCPaymentInfoDataConverter;

	@RequestMapping(value = "/payment-details/edit/{code}", method = RequestMethod.GET)
	@RequireHardLogIn

	public String getEditPaymentDetails(
			@PathVariable final String code, final Model model) throws CMSItemNotFoundException {
		prepareEditPaymentDetailsPage(model, code);
		return callSuperGetModelViewForPage(model);
	}

	@RequestMapping(value = "/payment-details/edit/{code}", method = RequestMethod.POST)
	@RequireHardLogIn
	public String postEditPaymentDetails(
			@PathVariable final String code, final CreditCardDataForm creditCardDataForm, final Model model,
			final RedirectAttributes redirectAttributes) {
		final CCPaymentInfoData ccPaymentInfoData = getUserFacade().getCCPaymentInfoForCode(code);
		creditCardDataFormCCPaymentInfoDataConverter.convert(
				creditCardDataForm, ccPaymentInfoData);
		try {
			getUserFacade().updateCCPaymentInfo(ccPaymentInfoData);
			GlobalMessages.addFlashMessage(redirectAttributes, GlobalMessages.INFO_MESSAGES_HOLDER,
										   "checkoutcom.creditcard.edit.ok");
			return REDIRECT_TO_PAYMENT_INFO_PAGE;
		}
		catch (final CheckoutComPaymentIntegrationException e) {
			GlobalMessages.addErrorMessage(model, "checkoutcom.creditcard.edit.ko");
			return REDIRECT_TO_EDIT_PAYMENT_INFO_PAGE + code;
		}
	}

	@RequestMapping(value = "/remove-payment-method-cko", method = RequestMethod.POST)
	@RequireHardLogIn
	public String removePaymentMethod(
			@RequestParam(value = "paymentInfoId")
			final String paymentMethodId, final RedirectAttributes redirectAttributes) {
		try {
			getUserFacade().removeCCPaymentInfo(paymentMethodId);
			GlobalMessages.addFlashMessage(redirectAttributes, GlobalMessages.CONF_MESSAGES_HOLDER,
										   "text.account.profile.paymentCart.removed");
		}
		catch (final CheckoutComPaymentIntegrationException e) {
			GlobalMessages.addErrorMessage(redirectAttributes, "checkout.creditcard.removal.ko");
		}

		return REDIRECT_TO_PAYMENT_INFO_PAGE;
	}

	protected void prepareEditPaymentDetailsPage(final Model model, final String code) throws CMSItemNotFoundException {
		final CCPaymentInfoData ccPaymentInfoData = getUserFacade().getCCPaymentInfoForCode(code);
		model.addAttribute("customerData", getCustomerFacade().getCurrentCustomer());
		model.addAttribute("creditCardDataForm", ccPaymentInfoData);
		model.addAttribute(ThirdPartyConstants.SeoRobots.META_ROBOTS, ThirdPartyConstants.SeoRobots.NOINDEX_NOFOLLOW);
		storeCmsPageInModel(model, EDIT_PAYMENT_DETAILS_CMS_PAGE);
	}


	@Override
	protected UserFacade getUserFacade() {
		return checkoutComUserFacade;
	}

	protected void storeCmsPageInModel(Model model,  String labelOrId) throws CMSItemNotFoundException {
		storeCmsPageInModel(model, getContentPageForLabelOrId(labelOrId));
	}

	protected String callSuperGetModelViewForPage(Model model) {
		return super.getViewForPage(model);
	}
}
