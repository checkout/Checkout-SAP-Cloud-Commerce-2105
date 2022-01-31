package com.checkout.hybris.backoffice.jalo;

import com.checkout.hybris.backoffice.constants.CheckoutbackofficeConstants;
import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.extension.ExtensionManager;
import org.apache.log4j.Logger;

/**
 * This is the extension manager of the Checkoutbackoffice extension.
 */
public class CheckoutbackofficeManager extends GeneratedCheckoutbackofficeManager {
    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger(CheckoutbackofficeManager.class.getName());

    /**
     * Get the valid instance of this manager.
     *
     * @return the current instance of this manager
     */
    public static final CheckoutbackofficeManager getInstance() {
        ExtensionManager em = JaloSession.getCurrentSession().getExtensionManager();
        return (CheckoutbackofficeManager) em.getExtension(CheckoutbackofficeConstants.EXTENSIONNAME);
    }

}
