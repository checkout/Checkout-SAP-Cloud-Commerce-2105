package com.checkout.hybris.facades.address.converters.populators;

import com.checkout.hybris.facades.address.CheckoutComAddressFacade;
import com.checkout.hybris.facades.beans.GooglePayPaymentContact;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercefacades.user.data.CountryData;
import de.hybris.platform.commercefacades.user.data.RegionData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComGooglePayAddressReversePopulatorTest {

    private static final String ADDRESS_LINE_1 = "09876 Summit Street Rd SE";
    private static final String ADDRESS_LINE_2 = "Floor 2";
    private static final String ADMINISTRATIVE_AREA = "OH";
    private static final String COUNTRY_CODE = "us";
    private static final String FULL_NAME = "James The Best Name";
    private static final String FIRST_NAME = "James";
    private static final String LAST_NAME = "The Best Name";
    private static final String POSTAL_CODE = "43056";
    private static final String LOCALITY = "Heath";

    @InjectMocks
    private CheckoutComGooglePayAddressReversePopulator testObj;

    @Mock
    private CheckoutComAddressFacade checkoutComAddressFacadeMock;
    @Mock
    private CountryData countryMock;
    @Mock
    private RegionData regionDataMock;

    private GooglePayPaymentContact source = new GooglePayPaymentContact();
    private AddressData target = new AddressData();

    @Before
    public void setUp() {
        source.setAddress1(ADDRESS_LINE_1);
        source.setAddress2(ADDRESS_LINE_2);
        source.setAdministrativeArea(ADMINISTRATIVE_AREA);
        source.setCountryCode(COUNTRY_CODE);
        source.setName(FULL_NAME);
        source.setPostalCode(POSTAL_CODE);
        source.setLocality(LOCALITY);
    }

    @Test
    public void populate_WhenEverythingIsCorrect_ShouldPopulateTheAddressData() {
        testObj.populate(source, target);

        assertEquals(FIRST_NAME, target.getFirstName());
        assertEquals(LAST_NAME, target.getLastName());
        assertTrue(target.isBillingAddress());
        assertEquals(ADDRESS_LINE_1, target.getLine1());
        assertEquals(ADDRESS_LINE_2, target.getLine2());
        assertEquals(POSTAL_CODE, target.getPostalCode());
        assertEquals(LOCALITY, target.getTown());
        verify(checkoutComAddressFacadeMock).setAddressDataCountry(COUNTRY_CODE, target);
        verify(checkoutComAddressFacadeMock).setAddressDataRegion(ADMINISTRATIVE_AREA, COUNTRY_CODE, target);
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
