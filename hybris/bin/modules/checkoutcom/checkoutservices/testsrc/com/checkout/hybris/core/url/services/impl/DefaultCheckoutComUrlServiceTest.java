package com.checkout.hybris.core.url.services.impl;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.acceleratorservices.urlresolver.SiteBaseUrlResolutionService;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.site.BaseSiteService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultCheckoutComUrlServiceTest {

    private static final String FULL_SITE_URL = "fullSiteUrl";
    private static final String URL = "url";

    @InjectMocks
    private DefaultCheckoutComUrlService testObj;

    @Mock
    private BaseSiteService baseSiteServiceMock;
    @Mock
    private SiteBaseUrlResolutionService siteBaseUrlResolutionServiceMock;
    @Mock
    private BaseSiteModel currentBaseSiteMock;

    @Test
    public void testGetFullUrlSecure() throws Exception {
        when(baseSiteServiceMock.getCurrentBaseSite()).thenReturn(currentBaseSiteMock);
        when(siteBaseUrlResolutionServiceMock.getWebsiteUrlForSite(currentBaseSiteMock, true, URL)).thenReturn(FULL_SITE_URL);

        final String result = testObj.getFullUrl(URL, true);

        assertEquals(FULL_SITE_URL, result);
    }

    @Test
    public void testGetFullUrlUnSecure() throws Exception {
        when(baseSiteServiceMock.getCurrentBaseSite()).thenReturn(currentBaseSiteMock);
        when(siteBaseUrlResolutionServiceMock.getWebsiteUrlForSite(currentBaseSiteMock, false, URL)).thenReturn(FULL_SITE_URL);

        final String result = testObj.getFullUrl(URL, false);

        assertEquals(FULL_SITE_URL, result);
    }
}
