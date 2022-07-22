package com.checkout.hybris.facades.payment.google.impl;

import com.checkout.hybris.facades.accelerator.CheckoutComCheckoutFlowFacade;
import com.checkout.hybris.facades.address.CheckoutComWalletAddressFacade;
import com.checkout.hybris.facades.beans.*;
import com.checkout.hybris.facades.conversion.CheckoutComGooglePayConversionFacade;
import com.checkout.hybris.facades.merchant.CheckoutComMerchantConfigurationFacade;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commercefacades.order.CartFacade;
import de.hybris.platform.commercefacades.order.data.DeliveryModeData;
import de.hybris.platform.commerceservices.customer.DuplicateUidException;
import de.hybris.platform.commerceservices.delivery.DeliveryService;
import de.hybris.platform.core.model.c2l.CountryModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.order.CartService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultCheckoutComGooglePayFacadeTest {

    private static final String US_COUNTRY_CODE = "US";
    private static final String SHIPPING_METHOD_IDENTIFIER = "shippingMethodIdentifier";

    @Spy
    @InjectMocks
    private DefaultCheckoutComGooglePayFacade testObj;

    @Mock
    private CartFacade cartFacadeMock;
    @Mock
    private CartService cartServiceMock;
    @Mock
    private DeliveryService deliveryServiceMock;
    @Mock
    private CheckoutComCheckoutFlowFacade checkoutComCheckoutFlowFacadeMock;
    @Mock
    private CheckoutComWalletAddressFacade checkoutComWalletAddressFacadeMock;
    @Mock
    private CheckoutComGooglePayConversionFacade checkoutComGooglePayConversionFacadeMock;
    @Mock
    private CheckoutComMerchantConfigurationFacade checkoutComMerchantConfigurationFacadeMock;

    @Mock
    private CartModel cartModelMock;
    @Mock
    private CountryModel countryModelMock;
    @Mock
    private DeliveryModeData deliveryModeDataMock;
    @Mock
    private GooglePaySettingsData googlePaySettingsDataMock;
    @Mock
    private GooglePayTransactionInfoData transactionInfoDataMock;
    @Mock
    private GooglePayIntermediateAddress intermediateAddressMock;
    @Mock
    private GooglePaySelectionOption googlePaySelectionOptionMock;
    @Mock
    private GooglePayMerchantConfigurationData merchantConfigurationMock;

    private GooglePayIntermediatePaymentData googlePayIntermediatePaymentData;

    @Before
    public void setUp() {
        when(transactionInfoDataMock.getCurrencyCode()).thenReturn("USD");
        when(transactionInfoDataMock.getTotalPrice()).thenReturn("100.0d");

        when(intermediateAddressMock.getCountryCode()).thenReturn(US_COUNTRY_CODE);

        when(countryModelMock.getName()).thenReturn("country_name");
        when(countryModelMock.getIsocode()).thenReturn(US_COUNTRY_CODE);
        when(cartModelMock.getPaymentCost()).thenReturn(10.0d);
        when(cartModelMock.getTotalPrice()).thenReturn(150.0d);

        when(cartServiceMock.getSessionCart()).thenReturn(cartModelMock);
        when(cartServiceMock.hasSessionCart()).thenReturn(Boolean.TRUE);

        createGooglePayIntermediatePaymentData();
        when(checkoutComGooglePayConversionFacadeMock.getGooglePayTransactionInfo(cartFacadeMock.getSessionCart()))
                .thenReturn(transactionInfoDataMock);
        when(deliveryServiceMock.getDeliveryCountriesForOrder(cartServiceMock.getSessionCart()))
                .thenReturn(List.of(countryModelMock));
        when(googlePaySelectionOptionMock.getId()).thenReturn("selection_option_id");
        when(googlePaySelectionOptionMock.getLabel()).thenReturn("selection_option_label");
        when(googlePaySelectionOptionMock.getDescription()).thenReturn("selection_option_description");
        final ArrayList supportedDeliveryModes = new ArrayList();
        supportedDeliveryModes.add(deliveryModeDataMock);
        when(checkoutComGooglePayConversionFacadeMock.getGooglePaySelectionOptions(supportedDeliveryModes))
                .thenReturn(List.of(googlePaySelectionOptionMock));
        when(checkoutComCheckoutFlowFacadeMock.getSupportedDeliveryModes()).thenReturn(supportedDeliveryModes);

    }

    @Test(expected = IllegalArgumentException.class)
    public void getGooglePayMerchantConfiguration_whenGooglePaySettingsAreMissing_ShouldThrowAnException() {
        when(checkoutComMerchantConfigurationFacadeMock.getGooglePaySettings()).thenReturn(Optional.empty());

        testObj.getGooglePayMerchantConfiguration();
    }

    @Test
    public void getGooglePayMerchantConfiguration_whenGooglePaySettingsAreNotMissing_ShouldReturnGooglePayMerchantSettings() {
        when(googlePaySettingsDataMock.getMerchantId()).thenReturn("merchant_id_test");
        when(googlePaySettingsDataMock.getEnvironment()).thenReturn("TEST");

        when(merchantConfigurationMock.getMerchantId()).thenReturn("merchant_id_test");
        when(merchantConfigurationMock.getMerchantName()).thenReturn("merchant_name_test");

        when(checkoutComGooglePayConversionFacadeMock.getGooglePayMerchantConfiguration(googlePaySettingsDataMock)).thenReturn(merchantConfigurationMock);
        when(checkoutComMerchantConfigurationFacadeMock.getGooglePaySettings()).thenReturn(Optional.of(googlePaySettingsDataMock));

        final GooglePayMerchantConfigurationData result = testObj.getGooglePayMerchantConfiguration();

        assertThat(result).isNotNull();
    }

    @Test
    public void getGooglePayPaymentDataRequestUpdate_WhenIntermediatePaymentDataIsPassed_ShouldReturnPaymentRequestUpdate() {
        when(deliveryServiceMock.getDeliveryCountriesForOrder(cartServiceMock.getSessionCart()))
                .thenReturn(List.of());

        final GooglePayPaymentDataRequestUpdate result =
                testObj.getGooglePayPaymentDataRequestUpdate(googlePayIntermediatePaymentData);

        assertThat(result).isNotNull();
        assertThat(result.getError()).isNotNull();
        assertThat(result.getNewShippingOptionParameters()).isNull();
    }

    @Test
    public void getGooglePayDataRequestUpdate_WhenCountryIsSupported_ShouldSetShippingParams() {
        final GooglePayPaymentDataRequestUpdate result = testObj.getGooglePayPaymentDataRequestUpdate(googlePayIntermediatePaymentData);

        assertThat(result).isNotNull();
        assertThat(result.getNewShippingOptionParameters()).isNotNull();
    }

    @Test
    public void getGooglePayPaymentDataRequestUpdate__WhenShippingCountryNotSupported_ShouldReturnGooglePayPaymentRequestUpdateWithError() {
        when(checkoutComGooglePayConversionFacadeMock.getGooglePayTransactionInfo(cartFacadeMock.getSessionCart()))
                .thenReturn(transactionInfoDataMock);

        final GooglePayPaymentDataRequestUpdate result = testObj.getGooglePayPaymentDataRequestUpdate();

        assertThat(result).isNotNull();
    }

    @Test
    public void getGooglePayDeliveryInfo_WhenDeliveryModeSelected_ShouldUpdateDeliveryModeAndDeliveryAddress() throws DuplicateUidException {
        testObj.getGooglePayDeliveryInfo(googlePayIntermediatePaymentData);

        final InOrder inOrder = inOrder(checkoutComWalletAddressFacadeMock, checkoutComCheckoutFlowFacadeMock, testObj);
        inOrder.verify(checkoutComWalletAddressFacadeMock).handleAndSaveShippingAddress(intermediateAddressMock);
        inOrder.verify(checkoutComCheckoutFlowFacadeMock).setDeliveryMode(SHIPPING_METHOD_IDENTIFIER);
        inOrder.verify(testObj).getGooglePayPaymentDataRequestUpdate(googlePayIntermediatePaymentData);
    }

    @Test
    public void getGooglePayDeliveryInfo_WhenNoDeliveryMode_ShouldUpdateDeliveryModeAndDeliveryAddress() throws DuplicateUidException {
        googlePayIntermediatePaymentData.setShippingOptionData(null);

        testObj.getGooglePayDeliveryInfo(googlePayIntermediatePaymentData);

        final InOrder inOrder = inOrder(checkoutComWalletAddressFacadeMock, checkoutComCheckoutFlowFacadeMock, testObj);
        inOrder.verify(checkoutComWalletAddressFacadeMock).handleAndSaveShippingAddress(intermediateAddressMock);
        inOrder.verify(testObj).getGooglePayPaymentDataRequestUpdate(googlePayIntermediatePaymentData);
    }

    private void createGooglePayIntermediatePaymentData() {
        final GooglePaySelectionOptionData shippingOptionData = new GooglePaySelectionOptionData();
        shippingOptionData.setId(SHIPPING_METHOD_IDENTIFIER);

        googlePayIntermediatePaymentData = new GooglePayIntermediatePaymentData();
        googlePayIntermediatePaymentData.setShippingAddress(intermediateAddressMock);
        googlePayIntermediatePaymentData.setShippingOptionData(shippingOptionData);
        googlePayIntermediatePaymentData.setCallbackTrigger("test_callback_trigger");
    }

}
