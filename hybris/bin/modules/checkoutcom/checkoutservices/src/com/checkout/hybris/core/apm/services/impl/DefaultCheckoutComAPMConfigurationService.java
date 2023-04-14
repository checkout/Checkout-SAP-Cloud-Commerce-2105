package com.checkout.hybris.core.apm.services.impl;

import com.checkout.hybris.addon.model.CheckoutComAPMComponentModel;
import com.checkout.hybris.addon.model.CheckoutComPaymentMethodComponentModel;
import com.checkout.hybris.core.apm.configuration.CheckoutComAPMConfigurationSettings;
import com.checkout.hybris.core.apm.services.CheckoutComAPMConfigurationService;
import com.checkout.hybris.core.merchant.services.CheckoutComMerchantConfigurationService;
import com.checkout.hybris.core.model.CheckoutComAPMConfigurationModel;
import com.checkout.hybris.core.model.CheckoutComGlobalAPMConfigurationModel;
import com.google.common.collect.ImmutableMap;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.order.CartService;
import de.hybris.platform.servicelayer.internal.dao.GenericDao;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * Default implementation of {@link CheckoutComAPMConfigurationService}
 */
public class DefaultCheckoutComAPMConfigurationService implements CheckoutComAPMConfigurationService {

    protected static final Logger LOG = LogManager.getLogger(DefaultCheckoutComAPMConfigurationService.class);

    protected static final String APM_CONFIGURATION_CODE_CANNOT_BE_NULL = "APM configuration code cannot be null.";

    protected final CartService cartService;
    protected final GenericDao<CheckoutComAPMComponentModel> checkoutComApmComponentDao;
    protected final GenericDao<CheckoutComAPMConfigurationModel> checkoutComApmConfigurationDao;
    protected final CheckoutComMerchantConfigurationService checkoutComMerchantConfigurationService;
    protected final GenericDao<CheckoutComGlobalAPMConfigurationModel> globalAPMConfigurationDao;
    protected final Map<String, CheckoutComAPMConfigurationSettings> checkoutComAPMConfigurationSettings;

    public DefaultCheckoutComAPMConfigurationService(final CartService cartService,
                                                     final GenericDao<CheckoutComAPMComponentModel> checkoutComApmComponentDao,
                                                     final GenericDao<CheckoutComAPMConfigurationModel> checkoutComApmConfigurationDao,
                                                     final GenericDao<CheckoutComGlobalAPMConfigurationModel> globalAPMConfigurationDao,
                                                     final CheckoutComMerchantConfigurationService checkoutComMerchantConfigurationService,
                                                     final Map<String, CheckoutComAPMConfigurationSettings> checkoutComAPMConfigurationSettings) {
        this.cartService = cartService;
        this.checkoutComApmComponentDao = checkoutComApmComponentDao;
        this.globalAPMConfigurationDao = globalAPMConfigurationDao;
        this.checkoutComApmConfigurationDao = checkoutComApmConfigurationDao;
        this.checkoutComAPMConfigurationSettings = checkoutComAPMConfigurationSettings;
        this.checkoutComMerchantConfigurationService = checkoutComMerchantConfigurationService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isApmAvailable(final CheckoutComAPMConfigurationModel apmConfiguration, final String countryCode, final String currencyCode) {
        checkArgument(isNotBlank(countryCode), "Country code cannot be null");
        checkArgument(isNotBlank(currencyCode), "Currency code cannot be null");

        if (apmConfiguration == null) {
            LOG.warn("The apm is not defined, the apm component is not restricted.");
            return true;
        }

        final CheckoutComGlobalAPMConfigurationModel globalAPMConfig = globalAPMConfigurationDao.find().stream()
                .findAny()
                .orElseThrow(() -> new IllegalStateException("No CheckoutComAPMConfiguration has been found in the system"));

        final Collection<CheckoutComAPMConfigurationModel> allowedAPMs;
        if (Boolean.TRUE.equals(checkoutComMerchantConfigurationService.isNasUsed())) {
            allowedAPMs = globalAPMConfig.getNasAPMs();
        } else {
            allowedAPMs = globalAPMConfig.getAbcAPMs();
        }

        if (!allowedAPMs.contains(apmConfiguration)) {
            return false;
        }

        final boolean countryMatch = isEmpty(apmConfiguration.getRestrictedCountries()) ||
                apmConfiguration.getRestrictedCountries().stream().anyMatch(country -> countryCode.equalsIgnoreCase(country.getIsocode()));

        final boolean currencyMatch = isEmpty(apmConfiguration.getRestrictedCurrencies()) ||
                apmConfiguration.getRestrictedCurrencies().stream().anyMatch(currency -> currencyCode.equalsIgnoreCase(currency.getIsocode()));

        return countryMatch && currencyMatch;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<CheckoutComAPMConfigurationModel> getApmConfigurationByCode(final String apmCode) {
        checkArgument(isNotBlank(apmCode), APM_CONFIGURATION_CODE_CANNOT_BE_NULL);

        final List<CheckoutComAPMConfigurationModel> searchResults = checkoutComApmConfigurationDao.find(ImmutableMap.of(CheckoutComAPMConfigurationModel.CODE, apmCode));

        return CollectionUtils.isNotEmpty(searchResults) ? Optional.of(searchResults.get(0)) : Optional.empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isApmRedirect(final String apmCode) {
        checkArgument(isNotBlank(apmCode), APM_CONFIGURATION_CODE_CANNOT_BE_NULL);
        checkArgument(checkoutComAPMConfigurationSettings.containsKey(apmCode) && checkoutComAPMConfigurationSettings.get(apmCode) != null, "There is no setting for the APM configuration code");

        return checkoutComAPMConfigurationSettings.get(apmCode).getIsApmRedirect();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isApmUserDataRequired(final String apmCode) {
        checkArgument(isNotBlank(apmCode), APM_CONFIGURATION_CODE_CANNOT_BE_NULL);
        checkArgument(checkoutComAPMConfigurationSettings.containsKey(apmCode) && checkoutComAPMConfigurationSettings.get(apmCode) != null, "There is no setting for the APM configuration code");

        return checkoutComAPMConfigurationSettings.get(apmCode).getIsApmUserDataRequired();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CheckoutComAPMConfigurationModel> getAvailableApms() {
        final CartModel sessionCart = cartService.getSessionCart();
        if (Objects.nonNull(sessionCart.getPaymentAddress()) || Objects.nonNull(sessionCart.getDeliveryAddress())) {
            final AddressModel address = sessionCart.getPaymentAddress() != null ? sessionCart.getPaymentAddress() : sessionCart.getDeliveryAddress();

            return checkoutComApmComponentDao.find().stream()
                    .filter(CheckoutComAPMComponentModel::getVisible)
                    .map(CheckoutComAPMComponentModel::getApmConfiguration)
                    .filter(apm -> isApmAvailable(apm, address.getCountry().getIsocode(), sessionCart.getCurrency().getIsocode()))
                    .distinct()
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<MediaModel> getApmConfigurationMedia(final CheckoutComAPMConfigurationModel apmConfigurationModel) {
        return checkoutComApmComponentDao.find(ImmutableMap.of(CheckoutComAPMComponentModel.APMCONFIGURATION, apmConfigurationModel))
                .stream()
                .findAny()
                .map(CheckoutComPaymentMethodComponentModel::getMedia);
    }
}
