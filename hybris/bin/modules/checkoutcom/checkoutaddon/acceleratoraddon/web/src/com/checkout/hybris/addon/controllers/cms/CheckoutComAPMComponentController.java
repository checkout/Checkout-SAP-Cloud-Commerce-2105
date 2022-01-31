package com.checkout.hybris.addon.controllers.cms;

import com.checkout.hybris.addon.model.CheckoutComAPMComponentModel;
import com.checkout.hybris.core.model.CheckoutComAPMConfigurationModel;
import com.checkout.hybris.facades.apm.CheckoutComAPMConfigurationFacade;
import de.hybris.platform.addonsupport.controllers.cms.GenericCMSAddOnComponentController;
import de.hybris.platform.cms2.model.contents.components.AbstractCMSComponentModel;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * Renders the apm payment method components
 */
@Controller("CheckoutComAPMComponentController")
@RequestMapping(value = "/view/CheckoutComAPMComponentController")
public class CheckoutComAPMComponentController extends GenericCMSAddOnComponentController {

    protected static final String IS_AVAILABLE_MODEL_ATTRIBUTE = "isAvailable";
    protected static final String IS_REDIRECT_MODEL_ATTRIBUTE = "isRedirect";
    protected static final String IS_USER_DATA_REQUIRED_MODEL_ATTRIBUTE = "isUserDataRequired";
    protected static final String CURRENCY_CODE_MODEL_ATTRIBUTE = "currencyCode";
    protected static final String COUNTRY_CODE_MODEL_ATTRIBUTE = "countryCode";

    @Resource
    private CheckoutComAPMConfigurationFacade checkoutComAPMConfigurationFacade;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void fillModel(final HttpServletRequest request, final Model model, final AbstractCMSComponentModel component) {
        super.fillModel(request, model, component);

        if (component instanceof CheckoutComAPMComponentModel) {

            final String currencyCode = (String) request.getAttribute(CURRENCY_CODE_MODEL_ATTRIBUTE);
            final String countryCode = (String) request.getAttribute(COUNTRY_CODE_MODEL_ATTRIBUTE);

            final CheckoutComAPMConfigurationModel apmConfiguration = ((CheckoutComAPMComponentModel) component).getApmConfiguration();
            if (apmConfiguration != null) {
                model.addAttribute(IS_AVAILABLE_MODEL_ATTRIBUTE, checkoutComAPMConfigurationFacade.isAvailable(apmConfiguration, countryCode, currencyCode));
                model.addAttribute(IS_REDIRECT_MODEL_ATTRIBUTE, checkoutComAPMConfigurationFacade.isRedirect(apmConfiguration));
                model.addAttribute(IS_USER_DATA_REQUIRED_MODEL_ATTRIBUTE, checkoutComAPMConfigurationFacade.isUserDataRequiredRedirect(apmConfiguration));
            } else {
                model.addAttribute(IS_AVAILABLE_MODEL_ATTRIBUTE, Boolean.FALSE);
            }
        }
    }
}
