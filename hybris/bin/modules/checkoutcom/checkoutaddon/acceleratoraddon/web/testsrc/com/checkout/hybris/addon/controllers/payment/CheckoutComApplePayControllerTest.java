package com.checkout.hybris.addon.controllers.payment;

import com.checkout.hybris.facades.accelerator.CheckoutComCheckoutFlowFacade;
import com.checkout.hybris.facades.address.CheckoutComWalletAddressFacade;
import com.checkout.hybris.facades.beans.*;
import com.checkout.hybris.facades.customer.CheckoutComCustomerFacade;
import com.checkout.hybris.facades.payment.wallet.CheckoutComApplePayFacade;
import com.checkout.hybris.facades.payment.wallet.CheckoutComWalletOrderFacade;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.acceleratorstorefrontcommons.security.GUIDCookieStrategy;
import de.hybris.platform.commercefacades.user.UserFacade;
import de.hybris.platform.commerceservices.customer.DuplicateUidException;
import de.hybris.platform.order.InvalidCartException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.validation.Validator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.checkout.hybris.facades.enums.WalletPaymentType.APPLEPAY;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComApplePayControllerTest {

    private static final String SHIPPING_METHOD_IDENTIFIER = "shippingMethodIdentifier";

    @Spy
    @InjectMocks
    private CheckoutComApplePayController testObj;

    @Mock
    private UserFacade userFacadeMock;
    @Mock
    private GUIDCookieStrategy guidCookieStrategyMock;
    @Mock
    private Validator checkoutComPlaceOrderCartValidatorMock;
    @Mock
    private CheckoutComApplePayFacade checkoutComApplePayFacadeMock;
    @Mock
    private CheckoutComCustomerFacade checkoutComCustomerFacadeMock;
    @Mock
    private CheckoutComWalletOrderFacade checkoutComWalletOrderFacadeMock;
    @Mock
    private CheckoutComCheckoutFlowFacade checkoutComCheckoutFlowFacadeMock;
    @Mock
    private CheckoutComWalletAddressFacade checkoutComWalletAddressFacadeMock;

    @Mock
    private HttpServletRequest httpServletRequestMock;
    @Mock
    private HttpServletResponse httpServletResponseMock;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ApplePayAuthorisationRequest applePayRequestMock;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ApplePayShippingMethod applePayShippingMethodMock;
    @Mock
    private ApplePayValidateMerchantRequestData merchantDataMock;
    @Mock
    private ApplePayAdditionalAuthInfo applePayAdditionalAuthMock;
    @Mock
    private ApplePayPaymentContact billingContactMock, shippingContactMock;

    @Before
    public void setUp() throws InvalidCartException {
        when(applePayRequestMock.getToken().getPaymentData()).thenReturn(applePayAdditionalAuthMock);
        when(applePayRequestMock.getBillingContact()).thenReturn(billingContactMock);
        when(applePayRequestMock.getShippingContact()).thenReturn(shippingContactMock);
        doNothing().when(checkoutComWalletOrderFacadeMock).validateCartForPlaceOrder(checkoutComPlaceOrderCartValidatorMock);
    }

    @Test
    public void requestPaymentSession_ShouldCallFacade() {
        testObj.requestPaymentSession(merchantDataMock);

        verify(checkoutComApplePayFacadeMock).requestApplePayPaymentSession(merchantDataMock);
    }

    @Test
    public void placeApplePayOrder_ShouldPlaceOrder() throws InvalidCartException {
        testObj.placeApplePayOrder(applePayRequestMock);

        verify(checkoutComWalletAddressFacadeMock).handleAndSaveBillingAddress(billingContactMock);
        verify(checkoutComWalletOrderFacadeMock).placeWalletOrder(applePayAdditionalAuthMock, APPLEPAY);
    }

    @Test
    public void setDeliveryMode_WhenApplePayShippingMethod_ShouldSetTheShippingMethodAndReturnTheUpdatedCart() {
        when(applePayShippingMethodMock.getIdentifier()).thenReturn(SHIPPING_METHOD_IDENTIFIER);

        testObj.setDeliveryMode(applePayShippingMethodMock);

        verify(checkoutComCheckoutFlowFacadeMock).setDeliveryMode(SHIPPING_METHOD_IDENTIFIER);
        verify(checkoutComApplePayFacadeMock).getApplePayShippingMethodUpdate();
    }

    @Test
    public void setDeliveryAddress_WhenAnonymousUser_ShouldCreateAnonymousUserForExpressCheckout() throws DuplicateUidException {
        when(userFacadeMock.isAnonymousUser()).thenReturn(Boolean.TRUE);

        testObj.setDeliveryAddress(httpServletRequestMock, httpServletResponseMock, shippingContactMock);

        final InOrder inOrder = inOrder(checkoutComCustomerFacadeMock, guidCookieStrategyMock, checkoutComWalletAddressFacadeMock, checkoutComApplePayFacadeMock);
        inOrder.verify(checkoutComCustomerFacadeMock).createApplePayExpressCheckoutGuestUserForAnonymousCheckoutAndSetItOnSession();
        inOrder.verify(guidCookieStrategyMock).setCookie(httpServletRequestMock, httpServletResponseMock);
        inOrder.verify(checkoutComWalletAddressFacadeMock).handleAndSaveShippingAddress(shippingContactMock);
        inOrder.verify(checkoutComApplePayFacadeMock).getApplePayShippingContactUpdate();
    }

}
