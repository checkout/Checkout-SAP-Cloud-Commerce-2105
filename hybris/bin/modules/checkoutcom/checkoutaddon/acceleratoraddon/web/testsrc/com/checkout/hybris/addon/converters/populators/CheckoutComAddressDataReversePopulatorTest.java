package com.checkout.hybris.addon.converters.populators;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.acceleratorstorefrontcommons.forms.AddressForm;
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

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComAddressDataReversePopulatorTest {

    private static final String ADDRESS_LINE_1 = "09876 Summit Street Rd SE";
    private static final String ADDRESS_LINE_2 = "Floor 2";
    private static final String REGION_ISO = "OH";
    private static final String COUNTRY_CODE = "US";
    private static final String FIRST_NAME = "James";
    private static final String LAST_NAME = "Bond";
    private static final String POSTAL_CODE = "43056";
    private static final String TOWN = "Heath";
    private static final String TITLE_CODE = "Mr";
    private static final String ADDRESS_ID = "address_id";

    @InjectMocks
    private CheckoutComAddressDataReversePopulator testObj;

    @Mock
    private I18NFacade i18NFacadeMock;
    @Mock
    private CountryData countryMock;
    @Mock
    private RegionData regionDataMock;

    private AddressForm source = new AddressForm();
    private AddressData target = new AddressData();

    @Before
    public void setUp() {
        source.setTitleCode(TITLE_CODE);
        source.setLine1(ADDRESS_LINE_1);
        source.setLine2(ADDRESS_LINE_2);
        source.setRegionIso(REGION_ISO);
        source.setCountryIso(COUNTRY_CODE);
        source.setFirstName(FIRST_NAME);
        source.setLastName(LAST_NAME);
        source.setPostcode(POSTAL_CODE);
        source.setTownCity(TOWN);
        source.setAddressId(ADDRESS_ID);
        when(i18NFacadeMock.getCountryForIsocode(COUNTRY_CODE)).thenReturn(countryMock);
        when(i18NFacadeMock.getRegion(COUNTRY_CODE, REGION_ISO)).thenReturn(regionDataMock);
    }

    @Test
    public void populate_WhenEverythingIsCorrect_ShouldPopulateTheAddressData() {
        testObj.populate(source, target);

        assertEquals(TITLE_CODE, target.getTitleCode());
        assertEquals(FIRST_NAME, target.getFirstName());
        assertEquals(LAST_NAME, target.getLastName());
        assertEquals(countryMock, target.getCountry());
        assertFalse(target.isBillingAddress());
        assertTrue(target.isShippingAddress());
        assertEquals(ADDRESS_LINE_1, target.getLine1());
        assertEquals(ADDRESS_LINE_2, target.getLine2());
        assertEquals(POSTAL_CODE, target.getPostalCode());
        assertEquals(TOWN, target.getTown());
        assertEquals(regionDataMock, target.getRegion());
        assertEquals(ADDRESS_ID, target.getId());
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