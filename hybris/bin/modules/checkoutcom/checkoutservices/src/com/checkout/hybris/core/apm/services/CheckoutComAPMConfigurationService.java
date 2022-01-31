package com.checkout.hybris.core.apm.services;

import com.checkout.hybris.core.model.CheckoutComAPMConfigurationModel;
import de.hybris.platform.core.model.media.MediaModel;

import java.util.List;
import java.util.Optional;

/**
 * Used to find APM configuration and settings
 */
public interface CheckoutComAPMConfigurationService {

    /**
     * Checks if the apm is available for the given country isoCode and currency isoCode.
     * When the apm configuration is null it returns true, because the apm is considered not restricted.
     * It returns true also if restricted countries and restricted currencies sets are not defined.
     * If the apm config has defined restricted countries and restricted currencies sets, but the given country and
     * currency codes do not belong to those sets, the apm is not available.
     *
     * @param apmConfiguration the checkout.com apm configuration
     * @param countryCode      the country isoCode
     * @param currencyCode     the currency isoCode
     * @return true if it's available, false otherwise
     */
    boolean isApmAvailable(CheckoutComAPMConfigurationModel apmConfiguration, String countryCode, String currencyCode);

    /**
     * Finds the apm configuration for the given code
     *
     * @param apmCode the apm code
     * @return an optional of {@link CheckoutComAPMConfigurationModel} for the given code, optional empty if not found
     */
    Optional<CheckoutComAPMConfigurationModel> getApmConfigurationByCode(String apmCode);

    /**
     * Checks if the APM is a redirect type or not. Redirect APMs will redirect the user to the dedicated 3rd party
     * page where the purchase can be completed.
     *
     * @param apmCode the code of the APM
     * @return true is the APM is redirect, false otherwise
     */
    boolean isApmRedirect(String apmCode);

    /**
     * Checks if the APM requires the user to input additional data.
     *
     * @param apmCode the code of the APM
     * @return true is the APM requires user data, false otherwise
     */
    boolean isApmUserDataRequired(String apmCode);

    /**
     * Returns a list of available apms for the session cart
     *
     * @return List of {@link CheckoutComAPMConfigurationModel} which are available for the cart
     */
    List<CheckoutComAPMConfigurationModel> getAvailableApms();

    /**
     * Returns the Media for the apm configuration
     *
     * @param apmConfigurationModel teh apm configuration
     * @return the media
     */
    Optional<MediaModel> getApmConfigurationMedia(CheckoutComAPMConfigurationModel apmConfigurationModel);
}
