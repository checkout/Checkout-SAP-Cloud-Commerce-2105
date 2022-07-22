package com.checkout.hybris.facades.conversion.impl;

import com.checkout.hybris.facades.beans.GooglePayMerchantConfigurationData;
import com.checkout.hybris.facades.beans.GooglePaySelectionOption;
import com.checkout.hybris.facades.beans.GooglePaySettingsData;
import com.checkout.hybris.facades.beans.GooglePayTransactionInfoData;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.DeliveryModeData;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultCheckoutComGooglePayConversionFacadeTest {

    @InjectMocks
    private DefaultCheckoutComGooglePayConversionFacade testObj;

    @Mock
    private Converter<GooglePaySettingsData, GooglePayMerchantConfigurationData> checkoutComGooglePayPaymentRequestConverterMock;
    @Mock
    private Converter<CartData, GooglePayTransactionInfoData> checkoutComGooglePayTransactionInfoConverterMock;
    @Mock
    private Converter<DeliveryModeData, GooglePaySelectionOption> checkoutComGooglePayDeliveryModeToSelectionOptionConverterMock;

    @Mock
    private GooglePaySettingsData googlePaySettingsDataMock;
    @Mock
    private CartData cartDataMock;
    @Mock
    private DeliveryModeData deliveryModeDataMock;
    @Mock
    private GooglePayMerchantConfigurationData googlePayMerchantConfigurationDataMock;
    @Mock
    private GooglePayTransactionInfoData googlePayTransactionInfoDataMock;
    @Mock
    private GooglePaySelectionOption googlePaySelectionOptionMock;

    @Before
    public void setUp() {
        ReflectionTestUtils.setField(testObj,
                "checkoutComGooglePayPaymentRequestConverter",
                checkoutComGooglePayPaymentRequestConverterMock);

        ReflectionTestUtils.setField(testObj,
                "checkoutComGooglePayTransactionInfoConverter",
                checkoutComGooglePayTransactionInfoConverterMock);

        ReflectionTestUtils.setField(testObj,
                "checkoutComGooglePayDeliveryModeToSelectionOptionConverter",
                checkoutComGooglePayDeliveryModeToSelectionOptionConverterMock);

        when(googlePayMerchantConfigurationDataMock.getMerchantId()).thenReturn("merchant_id");
        when(googlePayTransactionInfoDataMock.getTotalPrice()).thenReturn("100");
        when(googlePaySelectionOptionMock.getId()).thenReturn("test_id");

        when(checkoutComGooglePayPaymentRequestConverterMock.convert(googlePaySettingsDataMock))
                .thenReturn(googlePayMerchantConfigurationDataMock);
        when(checkoutComGooglePayTransactionInfoConverterMock.convert(cartDataMock))
                .thenReturn(googlePayTransactionInfoDataMock);
        when(checkoutComGooglePayDeliveryModeToSelectionOptionConverterMock.convertAll(List.of(deliveryModeDataMock)))
                .thenReturn(List.of(googlePaySelectionOptionMock));
    }

    @Test
    public void getGooglePayMerchantConfiguration_ShouldReturnGooglePayMerchantConfigurationConverted() {
        final GooglePayMerchantConfigurationData result =
                testObj.getGooglePayMerchantConfiguration(googlePaySettingsDataMock);

        verify(checkoutComGooglePayPaymentRequestConverterMock).convert(googlePaySettingsDataMock);
        assertEquals(googlePayMerchantConfigurationDataMock, result);
    }

    @Test
    public void getGooglePayTransactionInfo_ShouldReturnGooglePayTransactionInfoConverted() {
        final GooglePayTransactionInfoData result = testObj.getGooglePayTransactionInfo(cartDataMock);

        verify(checkoutComGooglePayTransactionInfoConverterMock).convert(cartDataMock);
        assertEquals(googlePayTransactionInfoDataMock, result);
    }

    @Test
    public void getGooglePaySelectionOptions_ShouldReturnGooglePaySelectionOptionsConverted() {
        final List<GooglePaySelectionOption> result =
                testObj.getGooglePaySelectionOptions(List.of(deliveryModeDataMock));

        verify(checkoutComGooglePayDeliveryModeToSelectionOptionConverterMock).convertAll(List.of(deliveryModeDataMock));
        assertEquals(List.of(googlePaySelectionOptionMock), result);
    }
}
