package com.checkout.hybris.addon.controllers.cms;

import com.checkout.hybris.facades.beans.ApplePaySettingsData;
import com.checkout.hybris.facades.merchant.CheckoutComMerchantConfigurationFacade;
import de.hybris.platform.addonsupport.controllers.cms.GenericCMSAddOnComponentController;
import de.hybris.platform.cms2.model.contents.components.AbstractCMSComponentModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * Renders the Apple Pay payment method component
 */
@Controller("CheckoutComApplePayComponentController")
@RequestMapping(value = "/view/CheckoutComApplePayComponentController")
public class CheckoutComApplePayComponentController extends GenericCMSAddOnComponentController {

    protected static final Logger LOG = LogManager.getLogger(CheckoutComApplePayComponentController.class);
    protected static final String APPLE_PAY_CONFIG_MODEL_ATTRIBUTE = "applePaySettings";
    protected static final String REDIRECT_MODEL_ATTRIBUTE = "redirect";
    protected static final String DATA_REQUIRED_MODEL_ATTRIBUTE = "dataRequired";
    protected static final String IS_AVAILABLE_MODEL_ATTRIBUTE = "isAvailable";

    @Resource
    private CheckoutComMerchantConfigurationFacade checkoutComMerchantConfigurationFacade;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void fillModel(final HttpServletRequest request, final Model model, final AbstractCMSComponentModel component) {
        super.fillModel(request, model, component);

        final Optional<ApplePaySettingsData> applePaySettings = checkoutComMerchantConfigurationFacade.getApplePaySettings();

        if (applePaySettings.isPresent()) {
            model.addAttribute(APPLE_PAY_CONFIG_MODEL_ATTRIBUTE, applePaySettings.get());
            model.addAttribute(DATA_REQUIRED_MODEL_ATTRIBUTE, false);
            model.addAttribute(REDIRECT_MODEL_ATTRIBUTE, false);
            model.addAttribute(IS_AVAILABLE_MODEL_ATTRIBUTE, true);
        } else {
            LOG.warn("ApplePay configuration not found. Payment will be disabled on the site.");
            model.addAttribute(IS_AVAILABLE_MODEL_ATTRIBUTE, false);
        }
    }
}