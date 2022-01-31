package com.checkout.hybris.facades.payment.converters.populators;

import com.checkout.hybris.core.url.services.CheckoutComUrlService;
import com.checkout.hybris.facades.beans.ApplePaySettingsData;
import com.checkout.hybris.facades.beans.ApplePayValidateMerchantData;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.checkout.hybris.facades.payment.converters.populators.CheckoutComApplePayToValidateMerchantPopulator.WEB;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComApplePayToValidateMerchantPopulatorTest {

    private static final String URL_VALUE = "https://www.test.com";
    private static final String MERCHANT_NAME_VALUE = "merchant name";
    private static final String MERCHANT_ID_VALUE = "merchant_id";
    private static final String INITIATIVE_CONTEXT_VALUE = "www.test.com";

    @InjectMocks
    private CheckoutComApplePayToValidateMerchantPopulator testObj;

    private ApplePaySettingsData source = new ApplePaySettingsData();
    private ApplePayValidateMerchantData target = new ApplePayValidateMerchantData();

    @Mock
    private CheckoutComUrlService checkoutComUrlServiceMock;

    @Before
    public void setUp() {
        when(checkoutComUrlServiceMock.getWebsiteUrlForCurrentSite()).thenReturn(URL_VALUE);
        source.setMerchantId(MERCHANT_ID_VALUE);
        source.setMerchantName(MERCHANT_NAME_VALUE);
    }

    @Test
    public void populate_ShouldPopulateTargetCorrectly() {
        testObj.populate(source, target);

        assertEquals(MERCHANT_NAME_VALUE, target.getDisplayName());
        assertEquals(WEB, target.getInitiative());
        assertEquals(INITIATIVE_CONTEXT_VALUE, target.getInitiativeContext());
        assertEquals(MERCHANT_ID_VALUE, target.getMerchantIdentifier());
    }

    @Test(expected = ConversionException.class)
    public void populate_WhenMalformedUrl_ShouldThrowException() {
        when(checkoutComUrlServiceMock.getWebsiteUrlForCurrentSite()).thenReturn(null);

        testObj.populate(source, target);
    }

    @Test(expected = IllegalArgumentException.class)
    public void populate_WhenSourceNull_ShouldThrowException() {
        testObj.populate(null, target);
    }

    @Test(expected = IllegalArgumentException.class)
    public void populate_WhenTargetNull_ShouldThrowException() {
        testObj.populate(source, null);
    }
}