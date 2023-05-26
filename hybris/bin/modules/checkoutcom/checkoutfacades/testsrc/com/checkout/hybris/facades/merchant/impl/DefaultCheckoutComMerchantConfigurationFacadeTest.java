package com.checkout.hybris.facades.merchant.impl;

import com.checkout.hybris.core.merchant.services.CheckoutComMerchantConfigurationService;
import com.checkout.hybris.core.model.CheckoutComApplePayConfigurationModel;
import com.checkout.hybris.core.model.CheckoutComGooglePayConfigurationModel;
import com.checkout.hybris.facades.beans.ApplePaySettingsData;
import com.checkout.hybris.facades.beans.GooglePaySettingsData;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultCheckoutComMerchantConfigurationFacadeTest {

    private static final String PUBLIC_KEY = "PUBLIC_KEY";

    @InjectMocks
    private DefaultCheckoutComMerchantConfigurationFacade testObj;

    @Mock
    private CheckoutComMerchantConfigurationService checkoutComMerchantConfigurationServiceMock;
    @Mock
    private CheckoutComApplePayConfigurationModel applePayConfigurationMock;
    @Mock
    private Converter<CheckoutComApplePayConfigurationModel, ApplePaySettingsData> checkoutComApplePaySettingsDataConverterMock;
    @Mock
    private Converter<CheckoutComGooglePayConfigurationModel, GooglePaySettingsData> checkoutComGooglePaySettingsDataConverterMock;
    @Mock
    private ApplePaySettingsData applePaySettingsDataMock;
    @Mock
    private CheckoutComGooglePayConfigurationModel googlePayConfigurationMock;
    @Mock
    private GooglePaySettingsData googlePaySettingsDataMock;

    @Before
    public void setUp() {
        ReflectionTestUtils.setField(testObj, "checkoutComApplePaySettingsDataConverter", checkoutComApplePaySettingsDataConverterMock);
        ReflectionTestUtils.setField(testObj, "checkoutComGooglePaySettingsDataConverter", checkoutComGooglePaySettingsDataConverterMock);
    }

    @Test
    public void getCheckoutComMerchantPublicKey_ShouldReturnTheValueAsExpected() {
        when(checkoutComMerchantConfigurationServiceMock.getPublicKey()).thenReturn(PUBLIC_KEY);

        final String publicKey = testObj.getCheckoutComMerchantPublicKey();

        assertEquals(PUBLIC_KEY, publicKey);
        verify(checkoutComMerchantConfigurationServiceMock).getPublicKey();
    }

    @Test
    public void getApplePaySettings_ShouldReturnTheApplePaySettings() {
        when(checkoutComMerchantConfigurationServiceMock.getApplePayConfiguration()).thenReturn(applePayConfigurationMock);
        when(checkoutComApplePaySettingsDataConverterMock.convert(applePayConfigurationMock)).thenReturn(applePaySettingsDataMock);

        final Optional<ApplePaySettingsData> result = testObj.getApplePaySettings();

        assertTrue(result.isPresent());
        assertEquals(applePaySettingsDataMock, result.get());
        verify(checkoutComMerchantConfigurationServiceMock).getApplePayConfiguration();
    }

    @Test
    public void getApplePaySettings_WhenNoConfigurationFound_ShouldReturnOptionalEmpty() {
        when(checkoutComMerchantConfigurationServiceMock.getApplePayConfiguration()).thenReturn(null);

        final Optional<ApplePaySettingsData> result = testObj.getApplePaySettings();

        assertFalse(result.isPresent());
    }

    @Test
    public void getGooglePaySettings_ShouldReturnTheApplePaySettings() {
        when(checkoutComMerchantConfigurationServiceMock.getGooglePayConfiguration()).thenReturn(googlePayConfigurationMock);
        when(checkoutComGooglePaySettingsDataConverterMock.convert(googlePayConfigurationMock)).thenReturn(googlePaySettingsDataMock);

        final Optional<GooglePaySettingsData> result = testObj.getGooglePaySettings();

        assertTrue(result.isPresent());
        assertEquals(googlePaySettingsDataMock, result.get());
        verify(checkoutComMerchantConfigurationServiceMock).getGooglePayConfiguration();
    }

    @Test
    public void getGooglePaySettings_WhenNoConfigurationFound_ShouldReturnOptionalEmpty() {
        when(checkoutComMerchantConfigurationServiceMock.getGooglePayConfiguration()).thenReturn(null);

        final Optional<GooglePaySettingsData> result = testObj.getGooglePaySettings();

        assertFalse(result.isPresent());
    }
    
    @Test
    public void isCheckoutComMerchantABC_WhenMerchantIsNAS_shouldReturnFalse() {
        when(checkoutComMerchantConfigurationServiceMock.isNasUsed()).thenReturn(true);
        
        final Boolean result = testObj.isCheckoutComMerchantABC();
        
        assertFalse(result);
    }

    @Test
    public void isCheckoutComMerchantABC_WhenMerchantIsABC_shouldReturnTrue() {
        when(checkoutComMerchantConfigurationServiceMock.isNasUsed()).thenReturn(false);

        final Boolean result = testObj.isCheckoutComMerchantABC();

        assertTrue(result);
    }
}
