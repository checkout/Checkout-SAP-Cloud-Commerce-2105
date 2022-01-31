package com.checkout.hybris.addon.controllers.pages.checkout.steps;

import com.checkout.hybris.addon.constants.CheckoutaddonConstants;
import com.checkout.hybris.addon.forms.PaymentDetailsForm;
import de.hybris.platform.acceleratorstorefrontcommons.forms.AddressForm;
import de.hybris.platform.commercefacades.user.data.AddressData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Checkout address controller
 */
@Controller
@RequestMapping(value = "/checkout/multi/checkout-com/address")
public class CheckoutComAddressCheckoutStepController extends CheckoutComChoosePaymentAndBillingCheckoutStepController {

    protected static final Logger LOG = LogManager.getLogger(CheckoutComAddressCheckoutStepController.class);
    protected static final String BILLING_ADDRESS_MODEL_ATTRIBUTE_KEY = "coBillingAddressForm";
    protected static final String BILLING_ADDRESS_FORM_FRAGMENT = "fragments/checkout/checkoutBillingAddressForm";
    protected static final String COUNTRY_MODEL_ATTRIBUTE_KEY = "country";
    protected static final String SUPPORTED_COUNTRIES_MODEL_ATTRIBUTE_KEY = "supportedCountries";

    /**
     * Endpoint to get billing address form
     *
     * @param countryIsoCode     the requested country code
     * @param useDeliveryAddress boolean value that says if use delivery address as billing
     * @param model              model
     * @return the jsp fragment
     */
    @GetMapping(value = "/billingaddressform")
    public String getCountryAddressForm(@RequestParam("countryIsoCode") final String countryIsoCode,
                                        @RequestParam("useDeliveryAddress") final boolean useDeliveryAddress, final Model model) {
        model.addAttribute(SUPPORTED_COUNTRIES_MODEL_ATTRIBUTE_KEY, getCountries());
        model.addAttribute(REGIONS_MODEL_ATTRIBUTE_KEY, getI18NFacade().getRegionsForCountryIso(countryIsoCode));
        model.addAttribute(COUNTRY_MODEL_ATTRIBUTE_KEY, countryIsoCode);

        final PaymentDetailsForm checkoutPaymentDetailsForm = new PaymentDetailsForm();
        model.addAttribute(BILLING_ADDRESS_MODEL_ATTRIBUTE_KEY, checkoutPaymentDetailsForm);
        if (useDeliveryAddress) {
            populateAddressForm(countryIsoCode, checkoutPaymentDetailsForm);
        }
        return getConfigurationService().getConfiguration().getString(CheckoutaddonConstants.CHECKOUT_ADDON_PREFIX, CheckoutaddonConstants.UNDEFINED_PREFIX) + BILLING_ADDRESS_FORM_FRAGMENT;
    }

    /**
     * Populates the address form
     *
     * @param countryIsoCode     country code to add
     * @param paymentDetailsForm payment form to populate
     */
    protected void populateAddressForm(final String countryIsoCode, final PaymentDetailsForm paymentDetailsForm) {
        final AddressData deliveryAddress = getCheckoutFacade().getCheckoutCart().getDeliveryAddress();
        final AddressForm addressForm = new AddressForm();

        populateAddressBasicInfo(deliveryAddress, addressForm, deliveryAddress.getTitleCode());
        addressForm.setCountryIso(countryIsoCode);
        addressForm.setPhone(deliveryAddress.getPhone());
        paymentDetailsForm.setBillingAddress(addressForm);
    }
}
