package com.checkout.hybris.addon.controllers.cms;

import com.checkout.hybris.facades.beans.GooglePaySettingsData;
import com.checkout.hybris.facades.merchant.CheckoutComMerchantConfigurationFacade;
import de.hybris.platform.addonsupport.controllers.cms.GenericCMSAddOnComponentController;
import de.hybris.platform.cms2.model.contents.components.AbstractCMSComponentModel;
import de.hybris.platform.commercefacades.order.CartFacade;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * Renders the Google Pay payment method component
 */
@Controller("CheckoutComGooglePayComponentController")
@RequestMapping(value = "/view/CheckoutComGooglePayComponentController")
public class CheckoutComGooglePayComponentController extends GenericCMSAddOnComponentController {

    protected static final Logger LOG = LogManager.getLogger(CheckoutComGooglePayComponentController.class);

    protected static final String CART_DATA = "cartData";
    protected static final String REDIRECT_MODEL_ATTRIBUTE = "redirect";
    protected static final String DATA_REQUIRED_MODEL_ATTRIBUTE = "dataRequired";
    protected static final String IS_AVAILABLE_MODEL_ATTRIBUTE = "isAvailable";
    protected static final String GOOGLE_PAY_SETTINGS_MODEL_ATTRIBUTE = "googlePaySettings";

    @Resource(name = "cartFacade")
    private CartFacade cartFacade;

    @Resource
    private CheckoutComMerchantConfigurationFacade checkoutComMerchantConfigurationFacade;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void fillModel(final HttpServletRequest request, final Model model, final AbstractCMSComponentModel component) {
        callSuperFillModel(request, model, component);
        model.addAttribute(CART_DATA, cartFacade.getSessionCart());
        final Optional<GooglePaySettingsData> googlePaySettings = checkoutComMerchantConfigurationFacade.getGooglePaySettings();

        if (googlePaySettings.isPresent()) {
            model.addAttribute(GOOGLE_PAY_SETTINGS_MODEL_ATTRIBUTE, googlePaySettings.get());
            model.addAttribute(DATA_REQUIRED_MODEL_ATTRIBUTE, false);
            model.addAttribute(REDIRECT_MODEL_ATTRIBUTE, false);
            model.addAttribute(IS_AVAILABLE_MODEL_ATTRIBUTE, true);

        } else {
            LOG.warn("GooglePay configuration not found. Payment will be disabled on the site.");
            model.addAttribute(IS_AVAILABLE_MODEL_ATTRIBUTE, false);
        }
    }

    protected void callSuperFillModel(final HttpServletRequest request, final Model model, final AbstractCMSComponentModel component) {
        super.fillModel(request, model, component);
    }
}
