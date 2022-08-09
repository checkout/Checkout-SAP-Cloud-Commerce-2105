package com.checkout.hybris.facades.payment.wallet.impl;

import com.checkout.hybris.facades.accelerator.CheckoutComCheckoutFlowFacade;
import com.checkout.hybris.facades.beans.*;
import com.checkout.hybris.facades.merchant.CheckoutComMerchantConfigurationFacade;
import com.checkout.hybris.facades.payment.CheckoutComPaymentFacade;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commercefacades.order.CartFacade;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.DeliveryModeData;
import de.hybris.platform.commercefacades.product.data.PriceData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultCheckoutComApplePayFacadeTest {

    private static final String CURRENCY_ISO_CODE = "USD";
    private static final String MERCHANT_NAME = "merchantName";
    private static final String COUNTRY_CODE = "UK";
    private static final String REQUIRED_POSTAL_ADDR = "postalAddress";
    private static final String TOTAL_LINE_ITEM_TYPE = "final";
    private static final Set<String> SUPPORTED_NETWORKS = Set.of("VISA", "MASTERCARD");
    private static final Set<String> MERCHANT_CAPABILITIES = Set.of("supportsCredit", "supportsDebit");
    private static final String APPLE_PAY_LINE_ITEM_TYPE_FINAL = "final";
    private static final String SHIPPING_METHOD_IDENTIFIER = "shippingMethodIdentifier";

    @InjectMocks
    private DefaultCheckoutComApplePayFacade testObj;
    @Mock
    private CheckoutComMerchantConfigurationFacade checkoutComMerchantConfigurationFacadeMock;
    @Mock
    private CheckoutComCheckoutFlowFacade checkoutComCheckoutFlowFacadeMock;
    @Mock
    private Converter<ApplePaySettingsData, ApplePayValidateMerchantData> checkoutComApplePayToValidateMerchantConverterMock;
    @Mock
    private Converter<DeliveryModeData, ApplePayShippingMethod> checkoutComDeliveryModeDataToApplePayShippingMethodConverterMock;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private CartFacade cartFacadeMock;
    @Mock
    private CheckoutComPaymentFacade checkoutComPaymentFacadeMock;
    @Mock
    private Converter<AddressData, ApplePayPaymentContact> checkoutComApplePayAddressConverterMock;
    @Mock
    private ApplePaySettingsData applePaySettingsMock;
    @Mock
    private CartData cartMock;
    @Mock
    private AddressData addressDataMock;
    @Mock
    private ApplePayPaymentContact applePayContactMock;
    @Mock
    private DeliveryModeData deliveryModeDataOneMock, deliveryModeDataTwoMock;
    @Mock
    private ApplePayShippingMethod applePayShippingMethodOneMock, getApplePayShippingMethodTwoMock;
    @Mock
    private PriceData priceDataMock;
    private PriceData priceData = new PriceData();


    @Before
    public void setUp() {
        testObj = new DefaultCheckoutComApplePayFacade(checkoutComMerchantConfigurationFacadeMock, checkoutComApplePayToValidateMerchantConverterMock, checkoutComDeliveryModeDataToApplePayShippingMethodConverterMock, checkoutComPaymentFacadeMock, cartFacadeMock, checkoutComCheckoutFlowFacadeMock);

        priceData.setCurrencyIso(CURRENCY_ISO_CODE);
        priceData.setValue(BigDecimal.TEN);
        when(checkoutComMerchantConfigurationFacadeMock.getApplePaySettings()).thenReturn(Optional.of(applePaySettingsMock));
        when(cartFacadeMock.getSessionCart()).thenReturn(cartMock);
        when(cartMock.getTotalPrice()).thenReturn(priceData);
        when(cartMock.getDeliveryAddress()).thenReturn(addressDataMock);
        when(checkoutComApplePayAddressConverterMock.convert(addressDataMock)).thenReturn(applePayContactMock);
        when(applePaySettingsMock.getMerchantName()).thenReturn(MERCHANT_NAME);
        when(applePaySettingsMock.getSupportedNetworks()).thenReturn(SUPPORTED_NETWORKS);
        when(applePaySettingsMock.getMerchantCapabilities()).thenReturn(MERCHANT_CAPABILITIES);
        when(applePaySettingsMock.getCountryCode()).thenReturn(COUNTRY_CODE);
    }

    @Test
    public void getValidateMerchantData_ShouldReturnMerchantData() {
        testObj.getValidateMerchantData();

        verify(checkoutComApplePayToValidateMerchantConverterMock).convert(applePaySettingsMock);
    }

    @Test
    public void getApplePayPaymentRequest_WhenApplePaySettingFound_ShouldReturnPopulatedRequest() {
        final ApplePayPaymentRequestData result = testObj.getApplePayPaymentRequest();

        final ApplePayTotalData total = result.getTotal();
        assertThat(total).isNotNull();
        assertThat(total.getAmount()).isEqualTo(BigDecimal.TEN.toString());
        assertThat(total.getLabel()).isEqualTo(MERCHANT_NAME);
        assertThat(total.getType()).isEqualTo(TOTAL_LINE_ITEM_TYPE);

        assertThat(result.getCountryCode()).isEqualTo(COUNTRY_CODE);
        assertThat(result.getMerchantCapabilities()).isEqualTo(MERCHANT_CAPABILITIES);
        assertThat(result.getSupportedNetworks()).isEqualTo(SUPPORTED_NETWORKS);
        assertThat(result.getRequiredBillingContactFields()).containsOnly(REQUIRED_POSTAL_ADDR);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getApplePayPaymentRequest_WhenApplePaySettingNotFound_ShouldThrowException() {
        when(checkoutComMerchantConfigurationFacadeMock.getApplePaySettings()).thenReturn(Optional.empty());

        testObj.getApplePayPaymentRequest();
    }

    @Test
    public void getApplePayShippingContactUpdate_shouldFillTheTotalAndDeliveryModes() {
        final List<DeliveryModeData> supportedDeliveryMethods = List.of(deliveryModeDataOneMock,deliveryModeDataTwoMock);
        final List<ApplePayShippingMethod> applePayShippingMethods = List.of(applePayShippingMethodOneMock, getApplePayShippingMethodTwoMock);
        doReturn(supportedDeliveryMethods).when(checkoutComCheckoutFlowFacadeMock).getSupportedDeliveryModes();
        when(checkoutComDeliveryModeDataToApplePayShippingMethodConverterMock.convertAll(supportedDeliveryMethods)).thenReturn(applePayShippingMethods);
        when(cartFacadeMock.getSessionCart().getTotalPrice()).thenReturn(priceDataMock);
        when(priceDataMock.getValue()).thenReturn(BigDecimal.TEN);
        when(applePayShippingMethodOneMock.getIdentifier()).thenReturn(SHIPPING_METHOD_IDENTIFIER);

        final ApplePayShippingContactUpdate result = testObj.getApplePayShippingContactUpdate();

        assertThat(result.getNewTotal().getAmount()).isEqualTo(BigDecimal.TEN.toString());
        assertThat(result.getNewTotal().getLabel()).isEqualTo("Total amount");
        assertThat(result.getNewTotal().getType()).isEqualTo(APPLE_PAY_LINE_ITEM_TYPE_FINAL);
        assertThat(result.getNewShippingMethods()).isEqualTo(applePayShippingMethods);
        verify(checkoutComCheckoutFlowFacadeMock).setDeliveryMode(SHIPPING_METHOD_IDENTIFIER);
    }

    @Test
    public void getApplePayShippingMethodUpdate_shouldReturnTheNewTotalUpdated() {
        when(cartFacadeMock.getSessionCart().getTotalPrice()).thenReturn(priceDataMock);
        when(priceDataMock.getValue()).thenReturn(BigDecimal.TEN);

        final ApplePayShippingMethodUpdate result = testObj.getApplePayShippingMethodUpdate();

        assertThat(result.getNewTotal().getAmount()).isEqualTo(BigDecimal.TEN.toString());
        assertThat(result.getNewTotal().getLabel()).isEqualTo("Total amount");
        assertThat(result.getNewTotal().getType()).isEqualTo(APPLE_PAY_LINE_ITEM_TYPE_FINAL);
    }
}
