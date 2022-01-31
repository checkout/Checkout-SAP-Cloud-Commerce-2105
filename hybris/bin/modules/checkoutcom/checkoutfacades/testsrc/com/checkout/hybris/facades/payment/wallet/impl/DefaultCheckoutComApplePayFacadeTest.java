package com.checkout.hybris.facades.payment.wallet.impl;

import com.checkout.hybris.facades.beans.*;
import com.checkout.hybris.facades.merchant.CheckoutComMerchantConfigurationFacade;
import com.checkout.hybris.facades.payment.CheckoutComPaymentFacade;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commercefacades.order.CartFacade;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.product.data.PriceData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @InjectMocks
    private DefaultCheckoutComApplePayFacade testObj;
    @Mock
    private CheckoutComMerchantConfigurationFacade checkoutComMerchantConfigurationFacadeMock;
    @Mock
    private Converter<ApplePaySettingsData, ApplePayValidateMerchantData> checkoutComApplePayToValidateMerchantConverterMock;
    @Mock
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
    private PriceData priceData = new PriceData();

    @Before
    public void setUp() {
        testObj = new DefaultCheckoutComApplePayFacade(checkoutComMerchantConfigurationFacadeMock, checkoutComApplePayToValidateMerchantConverterMock, checkoutComPaymentFacadeMock, cartFacadeMock);

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
}
