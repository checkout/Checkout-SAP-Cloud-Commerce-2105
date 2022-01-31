package com.checkout.hybris.facades.address.converters.populators;

import com.checkout.hybris.facades.beans.ApplePayPaymentContact;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commercefacades.i18n.I18NFacade;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercefacades.user.data.CountryData;
import de.hybris.platform.commercefacades.user.data.RegionData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComApplePayAddressReversePopulatorTest {

    private static final String ADDRESS_LINE_1 = "09876 Summit Street Rd SE";
    private static final String ADDRESS_LINE_2 = "Floor 2";
    private static final String ADMINISTRATIVE_AREA = "OH";
    private static final String COUNTRY_CODE = "us";
    private static final String GIVEN_NAME = "James";
    private static final String FAMILY_NAME = "Bond";
    private static final String POSTAL_CODE = "43056";
    private static final String EMAIL_ADDRESS = "email@test.com";
    private static final String LOCALITY = "Heath";

    @InjectMocks
    private CheckoutComApplePayAddressReversePopulator testObj;

    @Mock
    private I18NFacade i18NFacadeMock;
    @Mock
    private CountryData countryMock;
    @Mock
    private RegionData regionDataMock;

    private ApplePayPaymentContact source = new ApplePayPaymentContact();
    private AddressData target = new AddressData();

    @Before
    public void setUp() {
        source.setAddressLines(Arrays.asList(ADDRESS_LINE_1, ADDRESS_LINE_2));
        source.setAdministrativeArea(ADMINISTRATIVE_AREA);
        source.setCountryCode(COUNTRY_CODE);
        source.setGivenName(GIVEN_NAME);
        source.setFamilyName(FAMILY_NAME);
        source.setPostalCode(POSTAL_CODE);
        source.setEmailAddress(EMAIL_ADDRESS);
        source.setLocality(LOCALITY);
        when(i18NFacadeMock.getCountryForIsocode(COUNTRY_CODE.toUpperCase())).thenReturn(countryMock);
        when(i18NFacadeMock.getRegion(COUNTRY_CODE.toUpperCase(), ADMINISTRATIVE_AREA)).thenReturn(regionDataMock);
    }

    @Test
    public void populate_WhenEverythingIsCorrect_ShouldPopulateTheAddressData() {
        testObj.populate(source, target);

        assertEquals(GIVEN_NAME, target.getFirstName());
        assertEquals(FAMILY_NAME, target.getLastName());
        assertEquals(countryMock, target.getCountry());
        assertEquals(EMAIL_ADDRESS, target.getEmail());
        assertTrue(target.isBillingAddress());
        assertEquals(ADDRESS_LINE_1, target.getLine1());
        assertEquals(ADDRESS_LINE_2, target.getLine2());
        assertEquals(POSTAL_CODE, target.getPostalCode());
        assertEquals(LOCALITY, target.getTown());
        assertEquals(regionDataMock, target.getRegion());
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