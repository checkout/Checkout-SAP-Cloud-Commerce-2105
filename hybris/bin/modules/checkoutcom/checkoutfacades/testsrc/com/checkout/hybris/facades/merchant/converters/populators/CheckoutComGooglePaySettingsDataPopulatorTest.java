package com.checkout.hybris.facades.merchant.converters.populators;

import com.checkout.hybris.core.enums.EnvironmentType;
import com.checkout.hybris.core.enums.GooglePayCardAuthMethods;
import com.checkout.hybris.core.enums.GooglePayCardNetworks;
import com.checkout.hybris.core.model.CheckoutComGooglePayConfigurationModel;
import com.checkout.hybris.facades.beans.GooglePaySettingsData;
import de.hybris.bootstrap.annotations.UnitTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashSet;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComGooglePaySettingsDataPopulatorTest {

    private static final String MERCHANT_NAME_VALUE = "merchant_name";
    private static final String MERCHANT_ID_VALUE = "merchant_id";
    private static final String TEST_VALUE = "TEST";
    private static final String GATEWAY_VALUE = "gateway";
    private static final String GATEWAY_MERCHANT_ID_VALUE = "Gateway merchant id";
    private static final String CARD_VALUE = "CARD";

    @InjectMocks
    private CheckoutComGooglePaySettingsDataPopulator testObj;

    private CheckoutComGooglePayConfigurationModel source = new CheckoutComGooglePayConfigurationModel();
    private GooglePaySettingsData target = new GooglePaySettingsData();

    @Before
    public void setUp() {
        source.setMerchantId(MERCHANT_ID_VALUE);
        source.setMerchantName(MERCHANT_NAME_VALUE);
        source.setGateway(GATEWAY_VALUE);
        source.setGatewayMerchantId(GATEWAY_MERCHANT_ID_VALUE);
        source.setType(CARD_VALUE);
        source.setEnvironment(EnvironmentType.TEST);
        source.setAllowedCardAuthMethods(new HashSet<>(asList(GooglePayCardAuthMethods.PAN_ONLY, GooglePayCardAuthMethods.CRYPTOGRAM_3DS)));
        source.setAllowedCardNetworks(new HashSet<>(asList(GooglePayCardNetworks.AMEX, GooglePayCardNetworks.DISCOVER)));
    }

    @Test
    public void populate_ShouldPopulateTheDataCorrectly() {
        testObj.populate(source, target);

        assertEquals(MERCHANT_ID_VALUE, target.getMerchantId());
        assertEquals(MERCHANT_NAME_VALUE, target.getMerchantName());
        assertEquals(TEST_VALUE, target.getEnvironment());
        assertEquals(GATEWAY_VALUE, target.getGateway());
        assertEquals(GATEWAY_MERCHANT_ID_VALUE, target.getGatewayMerchantId());
        assertEquals(CARD_VALUE, target.getType());
        assertEquals(TEST_VALUE, target.getEnvironment());
        assertEquals(new HashSet<>(asList(GooglePayCardAuthMethods.PAN_ONLY.getCode(), GooglePayCardAuthMethods.CRYPTOGRAM_3DS.getCode())), target.getAllowedAuthMethods());
        assertEquals(new HashSet<>(asList(GooglePayCardNetworks.AMEX.getCode(), GooglePayCardNetworks.DISCOVER.getCode())), target.getAllowedCardNetworks());
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