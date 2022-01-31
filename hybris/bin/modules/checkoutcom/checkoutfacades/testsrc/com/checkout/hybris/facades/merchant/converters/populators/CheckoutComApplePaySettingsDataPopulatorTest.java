package com.checkout.hybris.facades.merchant.converters.populators;

import com.checkout.hybris.core.enums.ApplePayMerchantCapabilities;
import com.checkout.hybris.core.enums.ApplePaySupportedNetworks;
import com.checkout.hybris.core.model.CheckoutComApplePayConfigurationModel;
import com.checkout.hybris.facades.beans.ApplePaySettingsData;
import de.hybris.bootstrap.annotations.UnitTest;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

@UnitTest
public class CheckoutComApplePaySettingsDataPopulatorTest {

    private static final String PRIVATE_KEY_VALUE = "private_key";
    private static final String MERCHANT_NAME_VALUE = "merchant_name";
    private static final String MERCHANT_ID_VALUE = "merchant_id";
    private static final String COUNTRY_CODE_VALUE = "US";
    private static final String CERTIFICATE_VALUE = "certificate_value";

    private CheckoutComApplePaySettingsDataPopulator testObj = new CheckoutComApplePaySettingsDataPopulator();

    private CheckoutComApplePayConfigurationModel source = new CheckoutComApplePayConfigurationModel();
    private ApplePaySettingsData target = new ApplePaySettingsData();

    @Before
    public void setUp() {
        source.setCertificate(CERTIFICATE_VALUE);
        source.setCountryCode(COUNTRY_CODE_VALUE);
        source.setMerchantId(MERCHANT_ID_VALUE);
        source.setMerchantName(MERCHANT_NAME_VALUE);
        source.setPrivateKey(PRIVATE_KEY_VALUE);
        source.setMerchantCapabilities(new HashSet<>(asList(ApplePayMerchantCapabilities.SUPPORTS3DS, ApplePayMerchantCapabilities.SUPPORTSCREDIT)));
        source.setSupportedNetworks(new HashSet<>(asList(ApplePaySupportedNetworks.MADA, ApplePaySupportedNetworks.CARTESBANCAIRES)));
    }

    @Test
    public void populate_ShouldPopulateTheDataCorrectly() {
        testObj.populate(source, target);

        assertEquals(CERTIFICATE_VALUE, target.getCertificate());
        assertEquals(COUNTRY_CODE_VALUE, target.getCountryCode());
        assertEquals(MERCHANT_ID_VALUE, target.getMerchantId());
        assertEquals(MERCHANT_NAME_VALUE, target.getMerchantName());
        assertEquals(PRIVATE_KEY_VALUE, target.getPrivateKey());
        assertEquals(new HashSet<>(asList(ApplePayMerchantCapabilities.SUPPORTS3DS.getCode(), ApplePayMerchantCapabilities.SUPPORTSCREDIT.getCode())), target.getMerchantCapabilities());
        assertEquals(new HashSet<>(asList(ApplePaySupportedNetworks.MADA.getCode(), ApplePaySupportedNetworks.CARTESBANCAIRES.getCode())), target.getSupportedNetworks());
    }

    @Test(expected = IllegalArgumentException.class)
    public void populate_WithNullSource_ShouldThrowException() {
        testObj.populate(null, target);
    }

    @Test(expected = IllegalArgumentException.class)
    public void populate_WithNullTarget_ShouldThrowException() {
        testObj.populate(source, null);
    }
}