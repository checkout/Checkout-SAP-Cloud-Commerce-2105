package com.checkout.hybris.facades.address.impl;

import com.checkout.hybris.facades.address.CheckoutComAddressFacade;
import com.checkout.hybris.facades.beans.ApplePayPaymentContact;
import com.checkout.hybris.facades.beans.GooglePayPaymentContact;
import com.checkout.hybris.facades.beans.WalletPaymentContact;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commercefacades.order.CheckoutFacade;
import de.hybris.platform.commercefacades.user.UserFacade;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commerceservices.strategies.CheckoutCustomerStrategy;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultCheckoutComWalletAddressFacadeTest {

    private static final String CUSTOMER_EMAIL = "customerEmail@test.com";

    @InjectMocks
    private DefaultCheckoutComWalletAddressFacade testObj;

    @Mock
    private Converter<GooglePayPaymentContact, AddressData> checkoutComGooglePayAddressReverseConverterMock;
    @Mock
    private Converter<ApplePayPaymentContact, AddressData> checkoutComApplePayAddressReverseConverterMock;
    @Mock
    private CheckoutCustomerStrategy checkoutCustomerStrategyMock;
    @Mock
    private CheckoutComAddressFacade checkoutComAddressFacadeMock;
    @Mock
    private UserFacade userFacadeMock;

    @Mock
    private GooglePayPaymentContact googlePayPaymentContactMock;
    @Mock
    private ApplePayPaymentContact applePayPaymentContactMock;
    @Mock
    private WalletPaymentContact walletPaymentContactMock;
    @Mock
    private AddressData googlePayAddressDataMock, applePayAddressDataMock;
    @Mock
    private CustomerModel customerMock;
    @Mock
    private CheckoutFacade checkoutFacadeMock;

    @Before
    public void setUp() {
        Whitebox.setInternalState(testObj, "checkoutComGooglePayAddressReverseConverter", checkoutComGooglePayAddressReverseConverterMock);
        Whitebox.setInternalState(testObj, "checkoutComApplePayAddressReverseConverter", checkoutComApplePayAddressReverseConverterMock);

        when(checkoutCustomerStrategyMock.getCurrentUserForCheckout()).thenReturn(customerMock);
        when(customerMock.getContactEmail()).thenReturn(CUSTOMER_EMAIL);
        when(checkoutComGooglePayAddressReverseConverterMock.convert(googlePayPaymentContactMock)).thenReturn(googlePayAddressDataMock);
        when(checkoutComApplePayAddressReverseConverterMock.convert(applePayPaymentContactMock)).thenReturn(applePayAddressDataMock);
    }

    @Test
    public void placeWalletOrder_WhenBillingGooglePayWallet_ShouldPlaceOrderAndSetEmail() {
        testObj.handleAndSaveBillingAddress(googlePayPaymentContactMock);

        verify(googlePayAddressDataMock).setEmail(CUSTOMER_EMAIL);
        verify(userFacadeMock).addAddress(googlePayAddressDataMock);
        verify(checkoutComAddressFacadeMock).setCartBillingDetails(googlePayAddressDataMock);
    }

    @Test
    public void placeWalletOrder_WhenBillingApplePayWallet_ShouldPlaceOrderAndSetEmail() {
        testObj.handleAndSaveBillingAddress(applePayPaymentContactMock);

        verify(applePayAddressDataMock).setEmail(CUSTOMER_EMAIL);
        verify(userFacadeMock).addAddress(applePayAddressDataMock);
        verify(checkoutComAddressFacadeMock).setCartBillingDetails(applePayAddressDataMock);
    }

    @Test
    public void placeWalletOrder_WhenCustomerNull_ShouldPlaceOrderAndSetEmailNull() {
        when(checkoutCustomerStrategyMock.getCurrentUserForCheckout()).thenReturn(null);

        testObj.handleAndSaveBillingAddress(googlePayPaymentContactMock);

        verify(googlePayAddressDataMock).setEmail(null);
        verify(userFacadeMock).addAddress(googlePayAddressDataMock);
        verify(checkoutComAddressFacadeMock).setCartBillingDetails(googlePayAddressDataMock);
    }

    @Test
    public void placeWalletOrder_WhenNotInstanceOfGoogleOrAppleBillingWallet_ShouldPlaceOrder() {
        testObj.handleAndSaveBillingAddress(walletPaymentContactMock);

        verifyZeroInteractions(checkoutCustomerStrategyMock);
        verify(googlePayAddressDataMock, never()).setEmail(any());
        verify(userFacadeMock).addAddress(null);
        verify(checkoutComAddressFacadeMock).setCartBillingDetails(null);
    }

    @Test
    public void placeWalletOrder_WhenShippingGooglePayWallet_ShouldPlaceOrderAndSetEmail() {
        testObj.handleAndSaveShippingAddress(googlePayPaymentContactMock);

        verify(userFacadeMock).addAddress(googlePayAddressDataMock);
        verify(checkoutFacadeMock).setDeliveryAddress(googlePayAddressDataMock);
    }

    @Test
    public void placeWalletOrder_WhenShippingApplePayWallet_ShouldPlaceOrderAndSetEmail() {
        testObj.handleAndSaveShippingAddress(applePayPaymentContactMock);

        verify(userFacadeMock).addAddress(applePayAddressDataMock);
        verify(checkoutFacadeMock).setDeliveryAddress(applePayAddressDataMock);
    }
}
