package com.checkout.hybris.core.url.services.impl;

import com.checkout.hybris.core.url.services.CheckoutComUrlService;
import de.hybris.platform.acceleratorservices.urlresolver.SiteBaseUrlResolutionService;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.site.BaseSiteService;

/**
 * Default implementation of {@link CheckoutComUrlService}
 */
public class DefaultCheckoutComUrlService implements CheckoutComUrlService {

    protected final BaseSiteService baseSiteService;
    protected final SiteBaseUrlResolutionService siteBaseUrlResolutionService;

    public DefaultCheckoutComUrlService(final BaseSiteService baseSiteService, final SiteBaseUrlResolutionService siteBaseUrlResolutionService) {
        this.baseSiteService = baseSiteService;
        this.siteBaseUrlResolutionService = siteBaseUrlResolutionService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFullUrl(final String url, final boolean isSecure) {
        final BaseSiteModel currentBaseSite = baseSiteService.getCurrentBaseSite();
        return siteBaseUrlResolutionService.getWebsiteUrlForSite(currentBaseSite, isSecure, url);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getWebsiteUrlForCurrentSite() {
        return siteBaseUrlResolutionService.getWebsiteUrlForSite(baseSiteService.getCurrentBaseSite(), true, null);
    }
}
