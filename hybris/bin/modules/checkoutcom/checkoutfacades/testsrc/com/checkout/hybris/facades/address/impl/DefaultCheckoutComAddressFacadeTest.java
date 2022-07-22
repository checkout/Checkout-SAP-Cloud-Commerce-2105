package com.checkout.hybris.facades.address.impl;

import com.checkout.hybris.core.address.services.CheckoutComAddressService;
import com.checkout.hybris.facades.accelerator.CheckoutComCheckoutFlowFacade;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commercefacades.i18n.I18NFacade;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercefacades.user.data.CountryData;
import de.hybris.platform.commercefacades.user.data.RegionData;
import de.hybris.platform.commerceservices.delivery.DeliveryService;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.order.CartService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultCheckoutComAddressFacadeTest {

    private static final String ADDRESS_ID = "ADDRESS_ID";
    private static final String COUNTRY_CODE = "US";
    private static final String REGION_CODE = "Region";

    @InjectMocks
    private DefaultCheckoutComAddressFacade testObj;

    @Mock
    private Converter<AddressModel, AddressData> addressConverterMock;
    @Mock
    private DeliveryService deliveryServiceMock;
    @Mock
    private I18NFacade i18NFacadeMock;
    @Mock
    private CheckoutComAddressService addressServiceMock;
    @Mock
    private CheckoutComCheckoutFlowFacade checkoutFlowFacadeMock;

    @Mock
    private AddressModel addressModelMock;
    @Mock
    private CartModel cartModelMock;
    @Mock
    private AddressData addressDataMock;
    @Mock
    private CartService cartServiceMock;
    @Mock
    private CountryData countryMock;
    @Mock
    private RegionData regionMock;

    @Before
    public void setUp() {
        ReflectionTestUtils.setField(testObj, "addressConverter", addressConverterMock);
        when(addressConverterMock.convert(addressModelMock)).thenReturn(addressDataMock);
        when(deliveryServiceMock.getSupportedDeliveryAddressesForOrder(cartModelMock, false)).thenReturn(Collections.singletonList(addressModelMock));
        when(addressDataMock.getId()).thenReturn(ADDRESS_ID);
        when(cartServiceMock.hasSessionCart()).thenReturn(true);
        when(cartServiceMock.getSessionCart()).thenReturn(cartModelMock);
        when(checkoutFlowFacadeMock.getDeliveryAddressModelForCode(ADDRESS_ID)).thenReturn(addressModelMock);
        when(cartModelMock.getPaymentAddress()).thenReturn(addressModelMock);
    }

    @Test
    public void getCartBillingAddress_ShouldReturnCartPaymentAddress() {
        final AddressData result = testObj.getCartBillingAddress();

        verify(addressConverterMock).convert(addressModelMock);
        assertEquals(addressDataMock, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getCartBillingAddress_WhenNoCartFound_ShouldThrowException() {
        when(cartServiceMock.hasSessionCart()).thenReturn(false);

        testObj.getCartBillingAddress();
    }

    @Test
    public void getCartBillingAddress_WhenCartHasNoPaymentAddress_ShouldReturnNull() {
        when(cartModelMock.getPaymentAddress()).thenReturn(null);

        final AddressData result = testObj.getCartBillingAddress();

        verify(addressConverterMock, never()).convert(addressModelMock);
        assertNull(result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setCartBillingDetails_WhenNoCartFound_ShouldThrowException() {
        when(cartServiceMock.hasSessionCart()).thenReturn(false);

        testObj.setCartBillingDetails(addressDataMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setCartBillingDetails_WhenNoAddressData_ShouldThrowException() {
        testObj.setCartBillingDetails(null);
    }

    @Test
    public void setCartBillingDetails_WhenNoAddressModel_ShouldDoNothing() {
        when(addressDataMock.getId()).thenReturn(null);
        when(checkoutFlowFacadeMock.getDeliveryAddressModelForCode(ADDRESS_ID)).thenReturn(null);

        testObj.setCartBillingDetails(addressDataMock);

        verifyZeroInteractions(addressServiceMock);
    }

    @Test
    public void setCartBillingDetails_WhenEverythingCorrect_ShouldWorkCorrectly() {
        testObj.setCartBillingDetails(addressDataMock);

        verify(addressServiceMock).setCartPaymentAddress(cartModelMock, addressModelMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setCartBillingDetailsByAddressId_WhenNoCartFound_ShouldThrowException() {
        when(cartServiceMock.hasSessionCart()).thenReturn(false);

        testObj.setCartBillingDetailsByAddressId(ADDRESS_ID);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setCartBillingDetailsByAddressId_WhenNoAddressData_ShouldThrowException() {
        testObj.setCartBillingDetails(null);
    }

    @Test
    public void setCartBillingDetailsByAddressId_WhenNoAddressModel_ShouldDoNothing() {
        when(addressDataMock.getId()).thenReturn(null);
        when(checkoutFlowFacadeMock.getDeliveryAddressModelForCode(ADDRESS_ID)).thenReturn(null);

        testObj.setCartBillingDetailsByAddressId(ADDRESS_ID);

        verifyZeroInteractions(addressServiceMock);
    }

    @Test
    public void setCartBillingDetailsByAddressId_WhenEverythingCorrect_ShouldWorkCorrectly() {
        testObj.setCartBillingDetailsByAddressId(ADDRESS_ID);

        verify(addressServiceMock).setCartPaymentAddress(cartModelMock, addressModelMock);
    }

    @Test
    public void setAddressDataCountry_WhenValidCountryCode_ShouldSetCountry() {
        when(i18NFacadeMock.getCountryForIsocode(COUNTRY_CODE)).thenReturn(countryMock);

        testObj.setAddressDataCountry(COUNTRY_CODE, addressDataMock);

        verify(addressDataMock).setCountry(countryMock);
    }

    @Test
    public void setAddressDataRegion_WhenValidCountryCode_ShouldSetCountry() {
        when(i18NFacadeMock.getRegion(COUNTRY_CODE, REGION_CODE.toUpperCase())).thenReturn(regionMock);

        testObj.setAddressDataRegion(REGION_CODE, COUNTRY_CODE, addressDataMock);

        verify(addressDataMock).setRegion(regionMock);
    }
}
