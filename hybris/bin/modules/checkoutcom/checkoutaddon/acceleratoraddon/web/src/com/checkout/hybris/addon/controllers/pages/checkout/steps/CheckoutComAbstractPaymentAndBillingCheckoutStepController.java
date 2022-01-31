package com.checkout.hybris.addon.controllers.pages.checkout.steps;

import com.checkout.hybris.addon.forms.PaymentDetailsForm;
import com.checkout.hybris.facades.accelerator.CheckoutComCheckoutFlowFacade;
import com.checkout.hybris.facades.address.CheckoutComAddressFacade;
import com.checkout.hybris.facades.payment.CheckoutComPaymentInfoFacade;
import de.hybris.platform.acceleratorstorefrontcommons.constants.WebConstants;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.pages.checkout.steps.AbstractCheckoutStepController;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.util.GlobalMessages;
import de.hybris.platform.acceleratorstorefrontcommons.forms.AddressForm;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.cms2.model.pages.ContentPageModel;
import de.hybris.platform.commercefacades.order.data.CCPaymentInfoData;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercefacades.user.data.RegionData;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.apache.commons.lang.StringUtils;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import javax.annotation.Resource;
import java.util.List;

import static com.checkout.hybris.addon.constants.CheckoutaddonWebConstants.*;

/**
 * Abstract controller that handles frontend logic for select payment method and billing address steps
 */
public abstract class CheckoutComAbstractPaymentAndBillingCheckoutStepController extends AbstractCheckoutStepController {

    protected static final String CHOOSE_PAYMENT_METHOD = "choose-payment-method";
    protected static final String PAYMENT_INFOS_MODEL_ATTRIBUTE_KEY = "paymentInfos";
    protected static final String CHECKOUT_MULTI_PAYMENT_METHOD_BREADCRUMB = "checkout.multi.paymentMethod.breadcrumb";
    protected static final String CHECKOUT_BILLING_ADDRESS_PAGE_GLOBAL_FIELD_ERROR = "checkoutcom.billing.address.page.global.field.error";
    protected static final String PAYMENT_DETAILS_FORM = "paymentDetailsForm";
    protected static final String REGIONS_MODEL_ATTRIBUTE_KEY = "regions";
    protected static final String HASNO_PAYMENT_INFO_MODEL_ATTRIBUTE_KEY = "hasNoPaymentInfo";

    @Resource
    protected CheckoutComPaymentInfoFacade checkoutComPaymentInfoFacade;
    @Resource
    protected CheckoutComCheckoutFlowFacade checkoutFlowFacade;
    @Resource
    protected CheckoutComAddressFacade checkoutComAddressFacade;
    @Resource
    protected Converter<AddressForm, AddressData> checkoutComAddressDataReverseConverter;

    /**
     * Set up the select payment method step model
     *
     * @param model       the model
     * @param paymentStep the payment method step
     * @param page        the given page
     * @throws CMSItemNotFoundException
     */
    protected void setupAddPaymentPage(final Model model, final String paymentStep, final String page) throws CMSItemNotFoundException {
        model.addAttribute(META_ROBOTS_MODEL_ATTRIBUTE_KEY, "noindex,nofollow");
        model.addAttribute(HASNO_PAYMENT_INFO_MODEL_ATTRIBUTE_KEY, getCheckoutFlowFacade().hasNoPaymentInfo());
        prepareDataForPage(model);
        model.addAttribute(WebConstants.BREADCRUMBS_KEY,
                getResourceBreadcrumbBuilder().getBreadcrumbs(CHECKOUT_MULTI_PAYMENT_METHOD_BREADCRUMB));
        final ContentPageModel contentPage = getContentPageForLabelOrId(page);
        storeCmsPageInModel(model, contentPage);
        setUpMetaDataForContentPage(model, contentPage);
        setCheckoutStepLinksForModel(model, getCheckoutStep(paymentStep));
        final CartData cartData = getCheckoutFacade().getCheckoutCart();
        model.addAttribute(CART_DATA_MODEL_ATTRIBUTE_KEY, cartData);
        model.addAttribute(DELIVERY_ADDRESS_MODEL_ATTRIBUTE_KEY, cartData.getDeliveryAddress());
        model.addAttribute(PAYMENT_INFOS_MODEL_ATTRIBUTE_KEY, getUserFacade().getCCPaymentInfos(true));
        model.addAttribute(WebConstants.BREADCRUMBS_KEY, getResourceBreadcrumbBuilder().getBreadcrumbs(CHECKOUT_MULTI_PAYMENT_METHOD_BREADCRUMB));
    }

    /**
     * Adds the global errors based on the validation results
     *
     * @param model         the model
     * @param bindingResult the binding result
     * @return true if there are errors, false otherwise
     */
    protected boolean addGlobalErrors(final Model model, final BindingResult bindingResult) {
        if (bindingResult.hasGlobalErrors()) {
            GlobalMessages.addErrorMessage(model, bindingResult.getGlobalErrors().get(0).getCode());
            return true;
        }
        if (bindingResult.hasErrors()) {
            GlobalMessages.addErrorMessage(model, CHECKOUT_BILLING_ADDRESS_PAGE_GLOBAL_FIELD_ERROR);
            return true;
        }
        return false;
    }

    /**
     * Adds the errors into the form
     *
     * @param model              the model
     * @param paymentDetailsForm the form
     * @param paymentStep        the payment step
     * @param page               the cms page
     * @return the page
     * @throws CMSItemNotFoundException
     */
    protected String handleFormErrors(final Model model, final PaymentDetailsForm paymentDetailsForm, final String paymentStep, final String page) throws CMSItemNotFoundException {
        prepareErrorView(model, paymentDetailsForm, paymentStep, page);
        return getViewForPage(model);
    }

    /**
     * Prepares the error model view
     *
     * @param model              the model
     * @param paymentDetailsForm the form
     * @param paymentStep        the payment step
     * @param page               the cms page
     * @throws CMSItemNotFoundException
     */
    protected void prepareErrorView(final Model model, final PaymentDetailsForm paymentDetailsForm, final String paymentStep, final String page) throws CMSItemNotFoundException {
        final AddressForm billingAddress = paymentDetailsForm.getBillingAddress();
        if (billingAddress != null && StringUtils.isNotBlank(billingAddress.getCountryIso())) {
            model.addAttribute(REGIONS_MODEL_ATTRIBUTE_KEY, getI18NFacade().getRegionsForCountryIso(billingAddress.getCountryIso()));
        }
        final List<CCPaymentInfoData> paymentInfos = getUserFacade().getCCPaymentInfos(true);
        setupAddPaymentPage(model, paymentStep, page);
        model.addAttribute(PAYMENT_INFOS_MODEL_ATTRIBUTE_KEY, paymentInfos);
        model.addAttribute(PAYMENT_DETAILS_FORM, paymentDetailsForm);
    }

    /**
     * Populate the address data based on the form values and set the billing address into the cart
     *
     * @param paymentDetailsForm the payment form
     */
    protected void handleAndSaveAddresses(final PaymentDetailsForm paymentDetailsForm) {
        final boolean useDeliveryAddress = paymentDetailsForm.getUseDeliveryAddress();
        final AddressData addressData = getAddressData(paymentDetailsForm.getBillingAddress(), useDeliveryAddress);

        addressData.setEmail(getCheckoutCustomerStrategy().getCurrentUserForCheckout().getContactEmail());

        if (shouldSaveAddressInProfile(useDeliveryAddress)) {
            getUserFacade().addAddress(addressData);
        }

        checkoutComAddressFacade.setCartBillingDetails(addressData);
    }

    /**
     * Checks if the address has to be saved into the customer profile
     *
     * @param useDeliveryAddress the boolean checkbox value
     * @return true if the save is needed, false otherwise
     */
    protected boolean shouldSaveAddressInProfile(final boolean useDeliveryAddress) {
        return !useDeliveryAddress || getUserFacade().isAnonymousUser();
    }

    /**
     * Gets the address data from the address form
     *
     * @param billingAddress     the billing address form
     * @param useDeliveryAddress the boolean checkbox value
     * @return the address data populated
     */
    protected AddressData getAddressData(final AddressForm billingAddress, final boolean useDeliveryAddress) {
        if (useDeliveryAddress) {
            final AddressData addressData = getCheckoutFacade().getCheckoutCart().getDeliveryAddress();
            addressData.setBillingAddress(true);
            return addressData;
        } else {
            return checkoutComAddressDataReverseConverter.convert(billingAddress);
        }
    }

    /**
     * Populates payment form basic infos
     *
     * @param addressData the address data
     * @param addressForm the address form to populate
     * @param title       the customer title
     */
    protected void populateAddressBasicInfo(final AddressData addressData, final AddressForm addressForm, final String title) {
        final RegionData region = addressData.getRegion();
        if (region != null && !StringUtils.isEmpty(region.getIsocode())) {
            addressForm.setRegionIso(region.getIsocodeShort());
        }
        addressForm.setTitleCode(title);
        addressForm.setFirstName(addressData.getFirstName());
        addressForm.setLastName(addressData.getLastName());
        addressForm.setLine1(addressData.getLine1());
        addressForm.setLine2(addressData.getLine2());
        addressForm.setTownCity(addressData.getTown());
        addressForm.setPostcode(addressData.getPostalCode());
    }
}