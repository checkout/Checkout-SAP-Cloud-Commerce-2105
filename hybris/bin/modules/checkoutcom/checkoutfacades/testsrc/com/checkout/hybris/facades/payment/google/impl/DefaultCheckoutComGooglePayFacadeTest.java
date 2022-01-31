package com.checkout.hybris.facades.payment.google.impl;

import com.checkout.hybris.facades.beans.GooglePayMerchantConfigurationData;
import com.checkout.hybris.facades.beans.GooglePaySettingsData;
import com.checkout.hybris.facades.merchant.CheckoutComMerchantConfigurationFacade;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultCheckoutComGooglePayFacadeTest {

    @InjectMocks
    private DefaultCheckoutComGooglePayFacade testObj;

    @Mock
    private CheckoutComMerchantConfigurationFacade checkoutComMerchantConfigurationFacadeMock;
    @Mock
    private Converter<GooglePaySettingsData, GooglePayMerchantConfigurationData> checkoutComGooglePayPaymentRequestConverterMock;

    @Mock
    private GooglePaySettingsData googlePaySettingsDataMock;

    @Test(expected = IllegalArgumentException.class)
    public void getGooglePayPaymentRequest_whenGooglePaySettingsAreMissing_ShouldThrowAnException() {
        when(checkoutComMerchantConfigurationFacadeMock.getGooglePaySettings()).thenReturn(Optional.empty());

        testObj.getGooglePayMerchantConfiguration();
    }

    @Test
    public void getGooglePayPaymentRequest_whenGooglePaySettingsAreNotMissing_ShouldReturnGooglePayMerchantSettings() {
        final GooglePayMerchantConfigurationData googlePayMerchantConfiguration = new GooglePayMerchantConfigurationData();
        when(checkoutComGooglePayPaymentRequestConverterMock.convert(googlePaySettingsDataMock)).thenReturn(googlePayMerchantConfiguration);
        when(checkoutComMerchantConfigurationFacadeMock.getGooglePaySettings()).thenReturn(Optional.of(googlePaySettingsDataMock));

        final GooglePayMerchantConfigurationData result = testObj.getGooglePayMerchantConfiguration();

        assertThat(result).isEqualTo(googlePayMerchantConfiguration);
    }
}
