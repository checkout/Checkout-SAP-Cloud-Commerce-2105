package com.checkout.hybris.addon.controllers.pages.checkout.steps;

import com.checkout.hybris.addon.converters.CheckoutComMappedPaymentDataFormReverseConverter;
import com.checkout.hybris.addon.forms.PaymentDataForm;
import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.core.payment.resolvers.CheckoutComPaymentTypeResolver;
import com.checkout.hybris.facades.merchant.CheckoutComMerchantConfigurationFacade;
import com.checkout.hybris.facades.payment.attributes.mapper.CheckoutComPaymentAttributesStrategyMapper;
import de.hybris.platform.acceleratorstorefrontcommons.annotations.PreValidateCheckoutStep;
import de.hybris.platform.acceleratorstorefrontcommons.annotations.RequireHardLogIn;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.util.GlobalMessages;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;

import static com.checkout.hybris.addon.constants.CheckoutaddonWebConstants.PAYMENT_METHOD_MODEL_ATTRIBUTE_KEY;

/**
 * Web controller to handle a Payment checkout step
 */
@Controller
@RequestMapping(value = "/checkout/multi/checkout-com/payment")
public class CheckoutComPaymentMethodCheckoutStepController extends CheckoutComAbstractPaymentAndBillingCheckoutStepController {

    protected static final Logger LOG = LogManager.getLogger(CheckoutComPaymentMethodCheckoutStepController.class);
    protected static final String PAYMENT_METHOD = "payment-method";
    protected static final String CHECKOUTCOM_PAYMENT_FRAMES_PAGE = "checkoutComPaymentFramesCheckoutPage";
    protected static final String PAYMENT_DATA_FORM = "paymentDataForm";
    protected static final String BILLING_ADDRESS = "billingAddress";
    protected static final String PUBLIC_KEY = "publicKey";
    protected static final String PAYMENT_TYPE_FORM_KEY = "type";

    @Resource
    protected CheckoutComMerchantConfigurationFacade checkoutComMerchantConfigurationFacade;
    @Resource
    protected Validator checkoutComPaymentDataFormValidValidator;
    @Resource
    protected CheckoutComPaymentTypeResolver checkoutComPaymentTypeResolver;
    @Resource
    protected CheckoutComMappedPaymentDataFormReverseConverter checkoutComMappedPaymentDataFormReverseConverter;
    @Resource
    protected CheckoutComPaymentAttributesStrategyMapper checkoutComPaymentAttributesStrategyMapper;

    /**
     * Returns the Checkout.com payment details form
     *
     * @param model the model
     * @return the checkout.com payment form
     * @throws CMSItemNotFoundException
     */
    @Override
    @GetMapping(value = {"/payment-method"})
    @RequireHardLogIn
    @PreValidateCheckoutStep(checkoutStep = PAYMENT_METHOD)
    public String enterStep(final Model model, final RedirectAttributes redirectAttributes) throws CMSItemNotFoundException {
        if (getCheckoutFacade().hasCheckoutCart()) {
            model.addAttribute(PAYMENT_DATA_FORM, new PaymentDataForm());
            setUpPaymentMethodStep(model);
            return getViewForPage(model);
        } else {
            return REDIRECT_URL_CART;
        }
    }

    /**
     * Save the tokenized card and go to summary page
     *
     * @param redirectAttributes attributes
     * @return the summary page
     * @throws CMSItemNotFoundException
     */
    @PostMapping(value = {"/submit-payment-data"})
    @RequireHardLogIn
    public String submitPaymentToken(final Model model,
                                     final PaymentDataForm paymentDataForm,
                                     final BindingResult bindingResult,
                                     final RedirectAttributes redirectAttributes) throws CMSItemNotFoundException {

        checkoutComPaymentDataFormValidValidator.validate(paymentDataForm, bindingResult);

        if (bindingResult.hasErrors()) {
            GlobalMessages.addErrorMessage(model, "checkoutcom.error.payment.form.invalid");
            bindingResult.getAllErrors().forEach(error -> GlobalMessages.addErrorMessage(model, error.getCode()));
            model.addAttribute(PAYMENT_DATA_FORM, paymentDataForm);
            setUpPaymentMethodStep(model);
            return getViewForPage(model);
        }

        final CheckoutComPaymentType paymentType = checkoutComPaymentTypeResolver.resolvePaymentMethod((String) paymentDataForm.getFormAttributes().get(PAYMENT_TYPE_FORM_KEY));
        final Object paymentInfo = checkoutComMappedPaymentDataFormReverseConverter.convertPaymentDataForm(paymentDataForm, paymentType);

        checkoutComPaymentInfoFacade.addPaymentInfoToCart(paymentInfo);
        return next(redirectAttributes);
    }

    /**
     * Setup the model to display the payment method form step
     *
     * @param model the model
     * @throws CMSItemNotFoundException
     */
    private void setUpPaymentMethodStep(final Model model) throws CMSItemNotFoundException {
        model.addAttribute(BILLING_ADDRESS, checkoutComAddressFacade.getCartBillingAddress());
        model.addAttribute(PUBLIC_KEY, checkoutComMerchantConfigurationFacade.getCheckoutComMerchantPublicKey());
        final String currentPaymentMethodType = checkoutFlowFacade.getCurrentPaymentMethodType();
        model.addAttribute(PAYMENT_METHOD_MODEL_ATTRIBUTE_KEY, currentPaymentMethodType);

        final CheckoutComPaymentType checkoutComPaymentType = checkoutComPaymentTypeResolver.resolvePaymentMethod(currentPaymentMethodType);
        checkoutComPaymentAttributesStrategyMapper.findStrategy(checkoutComPaymentType)
                .ifPresent(checkoutComPaymentAttributeStrategy -> checkoutComPaymentAttributeStrategy.addPaymentAttributeToModel(model));

        setupAddPaymentPage(model, PAYMENT_METHOD, CHECKOUTCOM_PAYMENT_FRAMES_PAGE);
    }

    @Override
    public String back(final RedirectAttributes redirectAttributes) {
        return getCheckoutStep(PAYMENT_METHOD).previousStep();
    }

    @Override
    public String next(final RedirectAttributes redirectAttributes) {
        return getCheckoutStep(PAYMENT_METHOD).nextStep();
    }
}
