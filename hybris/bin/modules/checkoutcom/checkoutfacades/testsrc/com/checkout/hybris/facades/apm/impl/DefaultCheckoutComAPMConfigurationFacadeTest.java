package com.checkout.hybris.facades.apm.impl;

import com.checkout.data.apm.CheckoutComAPMConfigurationData;
import com.checkout.hybris.core.apm.services.CheckoutComAPMConfigurationService;
import com.checkout.hybris.core.model.CheckoutComAPMConfigurationModel;
import com.google.common.collect.ImmutableList;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static com.checkout.common.Currency.GBP;
import static java.util.Locale.UK;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultCheckoutComAPMConfigurationFacadeTest {

    private static final String APM_CODE = "apmCode";

    @InjectMocks
    private DefaultCheckoutComAPMConfigurationFacade testObj;

    @Mock
    private CheckoutComAPMConfigurationService checkoutComAPMConfigurationServiceMock;
    @Mock
    private Converter<CheckoutComAPMConfigurationModel, CheckoutComAPMConfigurationData> checkoutComAPMConfigurationConverterMock;
    @Mock
    private CheckoutComAPMConfigurationModel apmConfigurationModelMock;
    @Mock
    private CheckoutComAPMConfigurationData apmConfigurationDataMock;

    @Before
    public void setUp() {
        when(apmConfigurationModelMock.getCode()).thenReturn(APM_CODE);
        when(checkoutComAPMConfigurationServiceMock.getAvailableApms()).thenReturn(ImmutableList.of(apmConfigurationModelMock));
    }

    @Test(expected = IllegalArgumentException.class)
    public void isAvailable_WhenCountryCodeIsNull_ShouldThrowException() {
        testObj.isAvailable(apmConfigurationModelMock, null, GBP);
    }

    @Test(expected = IllegalArgumentException.class)
    public void isAvailable_WhenCurrencyCodeIsNull_ShouldThrowException() {
        testObj.isAvailable(apmConfigurationModelMock, UK.getCountry(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void isAvailable_WhenApmConfigurationIsNull_ShouldThrowException() {
        testObj.isAvailable(null, UK.getCountry(), GBP);
    }

    @Test
    public void isAvailable_WhenApmIsAvailable_ShouldReturnTrue() {
        when(checkoutComAPMConfigurationServiceMock.isApmAvailable(apmConfigurationModelMock, UK.getCountry(), GBP)).thenReturn(true);

        assertTrue(testObj.isAvailable(apmConfigurationModelMock, UK.getCountry(), GBP));
    }

    @Test
    public void isAvailable_WhenApmIsNotAvailable_ShouldReturnFalse() {
        when(checkoutComAPMConfigurationServiceMock.isApmAvailable(apmConfigurationModelMock, UK.getCountry(), GBP)).thenReturn(false);

        assertFalse(testObj.isAvailable(apmConfigurationModelMock, UK.getCountry(), GBP));
    }

    @Test(expected = IllegalArgumentException.class)
    public void isRedirect_WhenApmConfigurationIsNull_ShouldThrowException() {
        testObj.isRedirect(null);
    }

    @Test
    public void isRedirect_WhenApmDefined_ShouldReturnConfiguredValue() {
        when(checkoutComAPMConfigurationServiceMock.isApmRedirect(APM_CODE)).thenReturn(true);

        assertTrue(testObj.isRedirect(apmConfigurationModelMock));
    }

    @Test(expected = IllegalArgumentException.class)
    public void isUserDataRequiredRedirect_WhenApmConfigurationIsNull_ShouldThrowException() {
        testObj.isUserDataRequiredRedirect(null);
    }

    @Test
    public void isUserDataRequiredRedirect_WhenApmDefined_ShouldReturnConfiguredValue() {
        when(checkoutComAPMConfigurationServiceMock.isApmUserDataRequired(APM_CODE)).thenReturn(true);

        assertTrue(testObj.isUserDataRequiredRedirect(apmConfigurationModelMock));
    }

    @Test
    public void getAvailableApms_WhenAvailableApms_ShouldReturnListOfAvailableApmData() {
        when(checkoutComAPMConfigurationConverterMock.convert(apmConfigurationModelMock)).thenReturn(apmConfigurationDataMock);

        final List<CheckoutComAPMConfigurationData> result = testObj.getAvailableApms();

        assertEquals(1, result.size());
        assertEquals(apmConfigurationDataMock, result.get(0));
    }

    @Test
    public void getAvailableApms_WhenNoAvailableApm_ShouldReturnEmptyList() {
        when(checkoutComAPMConfigurationServiceMock.getAvailableApms()).thenReturn(Collections.emptyList());

        final List<CheckoutComAPMConfigurationData> result = testObj.getAvailableApms();

        assertEquals(0, result.size());
    }

}
