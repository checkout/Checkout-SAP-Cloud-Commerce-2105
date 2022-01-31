package com.checkout.hybris.addon.renderes;

import com.checkout.hybris.addon.model.CheckoutComCardComponentModel;
import de.hybris.platform.addonsupport.renderer.impl.DefaultAddOnCMSComponentRenderer;
import de.hybris.platform.commerceservices.util.ResponsiveUtils;

/**
 * The checkout.com card component renderer
 */
public class CheckoutComCardComponentRenderer extends DefaultAddOnCMSComponentRenderer<CheckoutComCardComponentModel> {

    @Override
    protected String getUIExperienceFolder() {
        if (ResponsiveUtils.isResponsive()) {
            return "responsive";
        }

        return super.getUIExperienceFolder();
    }
}
