package com.checkout.hybris.addon.controllers.payment;

import com.checkout.hybris.facades.address.CheckoutComWalletAddressFacade;
import com.checkout.hybris.facades.beans.*;
import com.checkout.hybris.facades.customer.CheckoutComCustomerFacade;
import com.checkout.hybris.facades.enums.WalletPaymentType;
import com.checkout.hybris.facades.payment.google.CheckoutComGooglePayFacade;
import com.checkout.hybris.facades.payment.wallet.CheckoutComWalletOrderFacade;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.acceleratorstorefrontcommons.security.GUIDCookieStrategy;
import de.hybris.platform.commercefacades.user.UserFacade;
import de.hybris.platform.commerceservices.customer.DuplicateUidException;
import de.hybris.platform.order.InvalidCartException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.validation.Validator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComGooglePayControllerTest {

    private static final String SHIPPING_ID = "shippingId";
    private static final String CUSTOMER_EMAIL = "test@email.com";
    private static final String CUSTOMER_NAME = "Name";

    @InjectMocks
    private CheckoutComGooglePayController testObj;

    @Mock
    private UserFacade userFacadeMock;
    @Mock
    private GUIDCookieStrategy guidCookieStrategyMock;
    @Mock
    private Validator checkoutComPlaceOrderCartValidatorMock;
    @Mock
    private CheckoutComGooglePayFacade checkoutComGooglePayFacadeMock;
    @Mock
    private CheckoutComWalletOrderFacade checkoutComWalletOrderFacadeMock;
    @Mock
    private CheckoutComCustomerFacade checkoutComCustomerFacadeMock;
    @Mock
    private CheckoutComWalletAddressFacade checkoutComWalletAddressFacadeMock;

    @Mock
    private HttpServletRequest httpServletRequestMock;
    @Mock
    private HttpServletResponse httpServletResponseMock;
    @Mock
    private GooglePayPaymentToken googlePayPaymentTokenMock;
    @Mock
    private GooglePayIntermediateAddress shippingAddressMock;
    @Mock
    private GooglePayAuthorisationRequest googlePayRequestMock;
    @Mock
    private GooglePaySelectionOptionData shippingOptionsDataMock;
    @Mock
    private GooglePayPaymentContact billingContactMock, shippingContactMock;
    @Mock
    private GooglePayIntermediatePaymentData googlePayIntermediatePaymentDataMock;

    @Before
    public void setUp() throws InvalidCartException {
        when(googlePayRequestMock.getToken()).thenReturn(googlePayPaymentTokenMock);
        when(googlePayRequestMock.getEmail()).thenReturn(CUSTOMER_EMAIL);
        when(googlePayRequestMock.getBillingAddress()).thenReturn(billingContactMock);
        when(googlePayRequestMock.getShippingAddress()).thenReturn(shippingContactMock);
        when(shippingContactMock.getName()).thenReturn(CUSTOMER_NAME);
        doNothing().when(checkoutComWalletOrderFacadeMock).validateCartForPlaceOrder(checkoutComPlaceOrderCartValidatorMock);
        when(userFacadeMock.isAnonymousUser()).thenReturn(Boolean.TRUE);
        when(googlePayIntermediatePaymentDataMock.getShippingAddress()).thenReturn(shippingAddressMock);
        when(googlePayIntermediatePaymentDataMock.getShippingOptionData()).thenReturn(shippingOptionsDataMock);
        when(shippingOptionsDataMock.getId()).thenReturn(SHIPPING_ID);
    }

    @Test
    public void authoriseOrder_WhenExpressCheckout_ShouldUpdateUserDetails_AndPlaceOrder() throws InvalidCartException {
        when(checkoutComCustomerFacadeMock.isGooglePayExpressGuestCustomer()).thenReturn(Boolean.TRUE);

        testObj.authoriseOrder(googlePayRequestMock);

        final InOrder inOrder = Mockito.inOrder(checkoutComCustomerFacadeMock, checkoutComWalletAddressFacadeMock, checkoutComWalletOrderFacadeMock);
        inOrder.verify(checkoutComCustomerFacadeMock).updateExpressCheckoutUserEmail(CUSTOMER_EMAIL, CUSTOMER_NAME);
        inOrder.verify(checkoutComWalletAddressFacadeMock).handleAndSaveBillingAddress(billingContactMock);
        inOrder.verify(checkoutComWalletAddressFacadeMock).handleAndSaveShippingAddress(shippingContactMock);
        inOrder.verify(checkoutComWalletOrderFacadeMock).validateCartForPlaceOrder(checkoutComPlaceOrderCartValidatorMock);
        inOrder.verify(checkoutComWalletOrderFacadeMock).placeWalletOrder(googlePayPaymentTokenMock, WalletPaymentType.GOOGLEPAY);
    }

    @Test
    public void authoriseOrder_WhenNonExpressCheckout_ShouldUpdateUserDetails_AndPlaceOrder() throws InvalidCartException {
        when(checkoutComCustomerFacadeMock.isGooglePayExpressGuestCustomer()).thenReturn(Boolean.FALSE);

        testObj.authoriseOrder(googlePayRequestMock);

        final InOrder inOrder = Mockito.inOrder(checkoutComCustomerFacadeMock, checkoutComWalletAddressFacadeMock, checkoutComWalletOrderFacadeMock);
        verify(checkoutComCustomerFacadeMock, never()).updateExpressCheckoutUserEmail(CUSTOMER_EMAIL, CUSTOMER_NAME);
        inOrder.verify(checkoutComWalletAddressFacadeMock).handleAndSaveBillingAddress(billingContactMock);
        inOrder.verify(checkoutComWalletAddressFacadeMock).handleAndSaveShippingAddress(shippingContactMock);
        inOrder.verify(checkoutComWalletOrderFacadeMock).validateCartForPlaceOrder(checkoutComPlaceOrderCartValidatorMock);
        inOrder.verify(checkoutComWalletOrderFacadeMock).placeWalletOrder(googlePayPaymentTokenMock, WalletPaymentType.GOOGLEPAY);
    }

    @Test
    public void getGooglePayPaymentDataRequestUpdate_WhenUserIsAnonymous_ShouldCreateGuestUser() throws DuplicateUidException {
        testObj.getGooglePayDeliveryInfo(httpServletRequestMock, httpServletResponseMock, googlePayIntermediatePaymentDataMock);

        final InOrder inOrder = inOrder(checkoutComCustomerFacadeMock, guidCookieStrategyMock, checkoutComGooglePayFacadeMock);
        inOrder.verify(checkoutComCustomerFacadeMock).createGooglePayExpressCheckoutGuestUserForAnonymousCheckoutAndSetItOnSession();
        inOrder.verify(guidCookieStrategyMock).setCookie(httpServletRequestMock,httpServletResponseMock);
        inOrder.verify(checkoutComGooglePayFacadeMock).getGooglePayDeliveryInfo(googlePayIntermediatePaymentDataMock);
    }

    @Test
    public void getGooglePayPaymentDataRequestUpdate_WhenUserIsNotAnonymous_ShouldCreateGuestUser() throws DuplicateUidException {
        when(userFacadeMock.isAnonymousUser()).thenReturn(Boolean.FALSE);

        testObj.getGooglePayDeliveryInfo(httpServletRequestMock, httpServletResponseMock, googlePayIntermediatePaymentDataMock);

        verifyZeroInteractions(checkoutComCustomerFacadeMock);
        verifyZeroInteractions(guidCookieStrategyMock);
        verify(checkoutComGooglePayFacadeMock).getGooglePayDeliveryInfo(googlePayIntermediatePaymentDataMock);
    }
}
