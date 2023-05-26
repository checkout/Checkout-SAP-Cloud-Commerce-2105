package com.checkout.hybris.addon.controllers.pages.checkout.steps;

import com.checkout.hybris.addon.constants.CheckoutaddonConstants;
import com.checkout.hybris.addon.controllers.CheckoutaddonControllerConstants;
import com.checkout.hybris.addon.forms.PaymentDetailsForm;
import com.checkout.hybris.core.model.CheckoutComCreditCardPaymentInfoModel;
import de.hybris.platform.acceleratorstorefrontcommons.annotations.PreValidateCheckoutStep;
import de.hybris.platform.acceleratorstorefrontcommons.annotations.RequireHardLogIn;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.util.GlobalMessages;
import de.hybris.platform.acceleratorstorefrontcommons.forms.AddressForm;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import javax.validation.Valid;

import static com.checkout.hybris.addon.constants.CheckoutaddonWebConstants.REDIRECT_TO_CHECKOUT_PAYMENT_METHOD_FORM;
import static org.apache.commons.lang.StringUtils.isNotBlank;

@Controller
@RequestMapping(value = "/checkout/multi/checkout-com")
public class CheckoutComChoosePaymentAndBillingCheckoutStepController extends CheckoutComAbstractPaymentAndBillingCheckoutStepController {

    protected static final Logger LOG = LogManager.getLogger(CheckoutComChoosePaymentAndBillingCheckoutStepController.class);
    protected static final String PAYMENT_STATUS_PARAMETER_NAME = "paymentStatus";
    protected static final String CHECKOUTCOM_PAYMENT_METHOD_AND_BILLING_CHECKOUT_STEP_PAGE = "checkoutComPaymentMethodAndBillingCheckoutPage";
    protected static final String REDIRECT_TO_SUMMARY_PAGE = "redirect:/checkout/multi/checkout-com/summary/view";
    protected static final String SELECTED_COUNTRY_CODE_MODEL_ATTRIBUTE = "selectedCountryCode";
    protected static final String CHECKOUTCOM_PAYMENT_BUTTONS_PAGE = "checkoutComPaymentButtonsPage";
    protected static final String SELECTED_PAYMENT_METHOD_MODEL_ATTRIBUTE = "selectedPaymentMethod";

    @Resource
    protected ConfigurationService configurationService;

    /**
     * {@inheritDoc}
     */
    @Override
    @GetMapping(value = "/choose-payment-method")
    @RequireHardLogIn
    @PreValidateCheckoutStep(checkoutStep = CHOOSE_PAYMENT_METHOD)
    public String enterStep(final Model model, final RedirectAttributes redirectAttributes) throws CMSItemNotFoundException {
        getCheckoutFacade().setDeliveryModeIfAvailable();

        if (model.asMap().get(PAYMENT_STATUS_PARAMETER_NAME) != null) {
            GlobalMessages.addErrorMessage(model, "declined");
        }

        setupAddPaymentPage(model, CHOOSE_PAYMENT_METHOD, CHECKOUTCOM_PAYMENT_METHOD_AND_BILLING_CHECKOUT_STEP_PAGE);
        setupPaymentDetailsForm(model);
        setCheckoutStepLinksForModel(model, getCheckoutStep(CHOOSE_PAYMENT_METHOD));

        return getViewForPage(model);
    }

    /**
     * Sets the payment form in the model
     *
     * @param model the current model
     */
    protected void setupPaymentDetailsForm(final Model model) {
        final PaymentDetailsForm paymentDetailsForm = new PaymentDetailsForm();
        final AddressForm addressForm = new AddressForm();

        final AddressData billingAddress = checkoutComAddressFacade.getCartBillingAddress();
        if (billingAddress != null && billingAddress.getCountry() != null) {
            model.addAttribute(REGIONS_MODEL_ATTRIBUTE_KEY, getI18NFacade().getRegionsForCountryIso(billingAddress.getCountry().getIsocode()));
            populateAddressForm(billingAddress, addressForm);
        }

        paymentDetailsForm.setBillingAddress(addressForm);
        model.addAttribute(PAYMENT_DETAILS_FORM, paymentDetailsForm);
    }

    /**
     * Validates and saves the PaymentDetailsForm details on submit. Receives the payment method selected and the
     * billing address that has been filled.
     *
     * @param model              the model
     * @param paymentDetailsForm the payment form
     * @param bindingResult      the binding results
     * @return The view to redirect to.
     * @throws CMSItemNotFoundException
     */
    @PostMapping(value = "/add-payment-details")
    @RequireHardLogIn
    public String addPaymentDetails(final Model model,
                                    @Valid final PaymentDetailsForm paymentDetailsForm,
                                    final BindingResult bindingResult)
            throws CMSItemNotFoundException {

        if (addGlobalErrors(model, bindingResult)) {
            model.addAttribute(PAYMENT_DETAILS_FORM, paymentDetailsForm);
            model.addAttribute(SELECTED_COUNTRY_CODE_MODEL_ATTRIBUTE, getPaymentFormCountryIsoCode(paymentDetailsForm));
            return handleFormErrors(model, paymentDetailsForm, CHOOSE_PAYMENT_METHOD, CHECKOUTCOM_PAYMENT_METHOD_AND_BILLING_CHECKOUT_STEP_PAGE);
        }

        handleAndSaveAddresses(paymentDetailsForm);

        final Object paymentInfoData = checkoutComPaymentInfoFacade.createPaymentInfoData(paymentDetailsForm.getPaymentMethod());
        checkoutComPaymentInfoFacade.addPaymentInfoToCart(paymentInfoData);
        if (paymentDetailsForm.isDataRequired()) {
            return REDIRECT_TO_CHECKOUT_PAYMENT_METHOD_FORM;
        } else {
            return REDIRECT_TO_SUMMARY_PAGE;
        }
    }

    /**
     * Save the tokenized card and go to summary page
     *
     * @return the summary page
     */

    @PostMapping(value = "/set-payment-details")
    @RequireHardLogIn
    @ResponseBody
    public ResponseEntity<Void> setPaymentToken(final Model model,
                                                @Valid
                                                final PaymentDetailsForm paymentDetailsForm,
                                                final BindingResult bindingResult) {


        if (addGlobalErrors(model, bindingResult)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        handleAndSaveAddresses(paymentDetailsForm);

        final Object paymentInfoData = checkoutComPaymentInfoFacade.createPaymentInfoData(
                paymentDetailsForm.getPaymentMethod());
        checkoutComPaymentInfoFacade.addPaymentInfoToCart(paymentInfoData);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Sets the saved paymentMethod selected and proceeds to summary step
     *
     * @param selectedPaymentMethodId The id of the {@link CheckoutComCreditCardPaymentInfoModel} to use
     * @return the checkout summary step
     */

    @GetMapping(value = "/choose-payment-method/choose")
    @RequireHardLogIn
    public String selectExistingPaymentMethod(@RequestParam(value = "selectedPaymentMethodId") final String selectedPaymentMethodId,
                                              final Model model, final RedirectAttributes redirectAttributes) throws CMSItemNotFoundException {
        if (isNotBlank(selectedPaymentMethodId)) {
            getCheckoutFacade().setPaymentDetails(selectedPaymentMethodId);
            checkoutFlowFacade.setPaymentInfoBillingAddressOnSessionCart();
            return REDIRECT_TO_SUMMARY_PAGE;
        } else {
            LOG.error("The payment id for the selected payment method null or empty.");
            GlobalMessages.addErrorMessage(model, "checkoutcom.error.selected.payment.invalid");
            return enterStep(model, redirectAttributes);
        }
    }

    /**
     * Reloads the payment buttons content in order to validate apms for the selected country code
     *
     * @param countryIsoCode        the isoCode of the country
     * @param selectedPaymentMethod the selected payment method
     * @return the content page for payment buttons
     */
    @GetMapping(value = "/reload-payment-buttons")
    @RequireHardLogIn
    public String reloadPaymentButtonSlot(@RequestParam("countryIsoCode") final String countryIsoCode,
                                          @RequestParam("selectedPaymentMethod") final String selectedPaymentMethod,
                                          final Model model) throws CMSItemNotFoundException {

        setupAddPaymentPage(model, CHOOSE_PAYMENT_METHOD, CHECKOUTCOM_PAYMENT_BUTTONS_PAGE);
        setupPaymentDetailsForm(model);
        model.addAttribute(SELECTED_COUNTRY_CODE_MODEL_ATTRIBUTE, countryIsoCode);
        model.addAttribute(SELECTED_PAYMENT_METHOD_MODEL_ATTRIBUTE, selectedPaymentMethod);

        return configurationService.getConfiguration().getString(CheckoutaddonConstants.CHECKOUT_ADDON_PREFIX) +
                CheckoutaddonControllerConstants.Views.Fragments.CheckoutPaymentFrames.CheckoutComPaymentButtonsPage;
    }

    /**
     * Populates the address form form the billing saved
     *
     * @param addressData the given data saved
     * @param addressForm form to populate
     */
    protected void populateAddressForm(final AddressData addressData, final AddressForm addressForm) {
        populateAddressBasicInfo(addressData, addressForm, addressData.getTitle());
        addressForm.setCountryIso(addressData.getCountry().getIsocode());
        addressForm.setPhone(addressData.getPhone());
    }

    /**
     * Gets the country code from payment form if available
     *
     * @param paymentDetailsForm the payment form
     * @return the country iso code
     */
    protected Object getPaymentFormCountryIsoCode(final PaymentDetailsForm paymentDetailsForm) {
        return paymentDetailsForm != null && paymentDetailsForm.getBillingAddress() != null && StringUtils.isNotBlank(paymentDetailsForm.getBillingAddress().getCountryIso()) ? paymentDetailsForm.getBillingAddress().getCountryIso() : null;
    }

    @Override
    public String back(final RedirectAttributes redirectAttributes) {
        return getCheckoutStep(CHOOSE_PAYMENT_METHOD).previousStep();
    }

    @Override
    public String next(final RedirectAttributes redirectAttributes) {
        return getCheckoutStep(CHOOSE_PAYMENT_METHOD).nextStep();
    }
}
