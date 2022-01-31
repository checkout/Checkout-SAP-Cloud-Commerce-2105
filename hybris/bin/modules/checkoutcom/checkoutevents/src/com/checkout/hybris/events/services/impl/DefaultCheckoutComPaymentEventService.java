package com.checkout.hybris.events.services.impl;

import com.checkout.hybris.core.model.CheckoutComMerchantConfigurationModel;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentInfoService;
import com.checkout.hybris.events.enums.CheckoutComPaymentEventType;
import com.checkout.hybris.events.services.CheckoutComPaymentEventService;
import com.google.common.base.Preconditions;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.cms2.servicelayer.services.CMSSiteService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.util.Preconditions.checkArgument;

/**
 * Default implementation of {@link CheckoutComPaymentEventService}
 */
public class DefaultCheckoutComPaymentEventService implements CheckoutComPaymentEventService {

    protected static final Logger LOG = LogManager.getLogger(DefaultCheckoutComPaymentEventService.class);

    protected static final String KEY_DATA = "data";
    protected static final String KEY_ID = "id";
    private static final String KEY_METADATA = "metadata";
    private static final String KEY_SITE_ID = "site_id";

    protected final CMSSiteService cmsSiteService;
    protected final CheckoutComPaymentInfoService paymentInfoService;

    public DefaultCheckoutComPaymentEventService(final CMSSiteService cmsSiteService,
                                                 final CheckoutComPaymentInfoService paymentInfoService) {
        this.cmsSiteService = cmsSiteService;
        this.paymentInfoService = paymentInfoService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<CheckoutComPaymentEventType> getAllowedPaymentEventTypesForMerchant(final String siteId) {
        Preconditions.checkArgument(StringUtils.isNotBlank(siteId), "Site id is null in the event body.");

        final Collection<CMSSiteModel> sites = cmsSiteService.getSites();

        if (CollectionUtils.isNotEmpty(sites)) {
            final List<CMSSiteModel> matchSites = sites.stream().filter(site -> site.getUid().equalsIgnoreCase(siteId)).collect(toList());
            if (CollectionUtils.isNotEmpty(matchSites)) {
                final CMSSiteModel cmsSiteModel = matchSites.get(0);
                final CheckoutComMerchantConfigurationModel checkoutComMerchantConfiguration = cmsSiteModel.getCheckoutComMerchantConfiguration();
                return checkoutComMerchantConfiguration != null ? checkoutComMerchantConfiguration.getCheckoutComPaymentEventTypes() : Collections.emptySet();
            }
        }

        return Collections.emptySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSiteIdForTheEvent(final Map eventBodyMap) {
        checkArgument(MapUtils.isNotEmpty(eventBodyMap), "Event body cannot be null.");
        checkArgument(isMapElementValid(eventBodyMap, KEY_DATA), "Data object of the event body cannot be null.");

        final Map dataMap = (Map) eventBodyMap.get(KEY_DATA);
        Preconditions.checkArgument(isMapElementValid(dataMap, KEY_ID), "Payment id of the event body cannot be null.");

        if (isSiteIdValid(dataMap)) {
            final String siteId = (String) ((Map) dataMap.get(KEY_METADATA)).get(KEY_SITE_ID);
            LOG.debug("Valid site id [{}] from metadata found.", siteId);
            return siteId;
        } else {
            final String paymentId = (String) dataMap.get(KEY_ID);
            final String siteId = paymentInfoService.getSiteIdFromPaymentId(paymentId);
            LOG.debug("Event missing metadata, found site id [{}] using paymentId [{}].", siteId, paymentId);
            return siteId;
        }
    }

    private boolean isSiteIdValid(final Map dataMap) {
        return isMapElementValid(dataMap, KEY_METADATA) && ((Map) dataMap.get(KEY_METADATA)).containsKey(KEY_SITE_ID) && org.apache.commons.lang.StringUtils.isNotBlank((String) ((Map) dataMap.get(KEY_METADATA)).get(KEY_SITE_ID));
    }

    private boolean isMapElementValid(final Map map, final String key) {
        return MapUtils.isNotEmpty(map) && map.containsKey(key) && map.get(key) != null;
    }

}