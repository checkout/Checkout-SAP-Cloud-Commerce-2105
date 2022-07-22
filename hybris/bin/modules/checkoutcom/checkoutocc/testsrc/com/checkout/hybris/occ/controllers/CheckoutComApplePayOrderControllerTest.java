package com.checkout.hybris.occ.controllers;

import com.checkout.dto.order.ApplePayValidateMerchantRequestWsDTO;
import com.checkout.dto.order.PlaceWalletOrderWsDTO;
import com.checkout.hybris.facades.accelerator.CheckoutComCheckoutFlowFacade;
import com.checkout.hybris.facades.address.CheckoutComWalletAddressFacade;
import com.checkout.hybris.facades.beans.*;
import com.checkout.hybris.facades.customer.CheckoutComCustomerFacade;
import com.checkout.hybris.facades.payment.wallet.CheckoutComApplePayFacade;
import com.checkout.hybris.facades.payment.wallet.CheckoutComWalletOrderFacade;
import com.checkout.hybris.occ.exceptions.NoCheckoutCartException;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commercefacades.order.CartFacade;
import de.hybris.platform.commercefacades.order.CheckoutFacade;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.user.UserFacade;
import de.hybris.platform.commerceservices.customer.DuplicateUidException;
import de.hybris.platform.commerceservices.order.CommerceCartModificationException;
import de.hybris.platform.webservicescommons.mapping.DataMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.validation.Validator;

import static com.checkout.hybris.facades.enums.WalletPaymentType.APPLEPAY;
import static de.hybris.platform.webservicescommons.mapping.FieldSetLevelHelper.DEFAULT_LEVEL;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComApplePayOrderControllerTest {

    private static final String VALIDATION_URL = "validationURL";
    private static final String SHIPPING_METHOD_IDENTIFIER = "shippingMethodIdentifier";
    private static final String EMAIL_ADDRESS = "emailAddress";
    private static final String GIVEN_NAME = "givenName";

    @InjectMocks
    private CheckoutComApplePayOrderController testObj;

    @Mock
    private UserFacade userFacadeMock;
    @Mock
    private CheckoutComCustomerFacade defaultCheckoutComCustomerFacadeMock;
    @Mock
    private CheckoutComWalletOrderFacade checkoutComWalletOrderFacadeMock;
    @Mock
    private CheckoutComWalletAddressFacade checkoutComWalletAddressFacadeMock;
    @Mock
    private CheckoutComApplePayFacade checkoutComApplePayFacadeMock;
    @Mock
    private CheckoutComCheckoutFlowFacade checkoutComCheckoutFlowFacadeMock;
    @Mock
    private CheckoutFacade checkoutFacadeMock;
    @Mock
    private CartFacade cartFacadeMock;
    @Mock
    private Validator checkoutComPlaceOrderCartValidatorMock;
    @Mock
    private DataMapper dataMapperMock;

    @Mock
    private CartData cartDataMock;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ApplePayAuthorisationRequest authorisationRequestMock;
    @Mock
    private ApplePayPaymentContact paymentContactMock;
    @Mock
    private ApplePayAdditionalAuthInfo paymentDataMock;
    @Mock
    private PlaceWalletOrderDataResponse placeWalletOrderDataResponseMock;
    @Mock
    private ApplePayPaymentContact applePayPaymentContactMock;
    @Mock
    private ApplePayShippingContactUpdate applePayShippingContactUpdateMock;
    @Mock
    private ApplePayShippingMethod applePayShippingMethodMock;

    private ApplePayValidateMerchantRequestWsDTO validateMerchantRequestWsDTOStub = new ApplePayValidateMerchantRequestWsDTO();

    @Before
    public void setUp() {
        when(userFacadeMock.isAnonymousUser()).thenReturn(Boolean.FALSE);
        when(authorisationRequestMock.getBillingContact()).thenReturn(paymentContactMock);
        when(authorisationRequestMock.getShippingContact()).thenReturn(paymentContactMock);
        when(paymentContactMock.getEmailAddress()).thenReturn(EMAIL_ADDRESS);
        when(paymentContactMock.getGivenName()).thenReturn(GIVEN_NAME);
        when(checkoutComApplePayFacadeMock.getApplePayShippingContactUpdate()).thenReturn(applePayShippingContactUpdateMock);
        when(authorisationRequestMock.getToken().getPaymentData()).thenReturn(paymentDataMock);
        when(checkoutComWalletOrderFacadeMock.placeWalletOrder(paymentDataMock, APPLEPAY)).thenReturn(placeWalletOrderDataResponseMock);
        when(checkoutFacadeMock.hasCheckoutCart()).thenReturn(Boolean.TRUE);
        when(cartFacadeMock.getSessionCart()).thenReturn(cartDataMock);
        doNothing().when(checkoutComPlaceOrderCartValidatorMock).validate(eq(cartDataMock), any());
    }

    @Test
    public void placeOrder_ShouldPlaceOrderWithApplePay() throws NoCheckoutCartException {
        when(defaultCheckoutComCustomerFacadeMock.isApplePayExpressGuestCustomer()).thenReturn(Boolean.TRUE);

        testObj.placeOrder(authorisationRequestMock, DEFAULT_LEVEL);

        final InOrder inOrder = inOrder(defaultCheckoutComCustomerFacadeMock, checkoutComWalletAddressFacadeMock, checkoutComWalletOrderFacadeMock, dataMapperMock);
        inOrder.verify(defaultCheckoutComCustomerFacadeMock).updateExpressCheckoutUserEmail(EMAIL_ADDRESS, GIVEN_NAME);
        inOrder.verify(checkoutComWalletAddressFacadeMock).handleAndSaveBillingAddress(paymentContactMock);
        inOrder.verify(checkoutComWalletAddressFacadeMock).handleAndSaveShippingAddress(paymentContactMock);
        inOrder.verify(checkoutComWalletOrderFacadeMock).placeWalletOrder(paymentDataMock, APPLEPAY);
        inOrder.verify(dataMapperMock).map(placeWalletOrderDataResponseMock, PlaceWalletOrderWsDTO.class, DEFAULT_LEVEL);
    }

    @Test(expected = NoCheckoutCartException.class)
    public void placeOrder_WhenNoCheckoutCart_ShouldThrowException() throws NoCheckoutCartException {
        when(checkoutFacadeMock.hasCheckoutCart()).thenReturn(Boolean.FALSE);

        testObj.placeOrder(authorisationRequestMock, DEFAULT_LEVEL);
    }

    @Test
    public void requestPaymentSession_ShouldCallWalletOrderFacade() {
        validateMerchantRequestWsDTOStub.setValidationURL(VALIDATION_URL);

        testObj.requestPaymentSession(validateMerchantRequestWsDTOStub);

        verify(checkoutComApplePayFacadeMock).requestApplePayPaymentSession(any(ApplePayValidateMerchantRequestData.class));
    }

    @Test
    public void getPaymentRequest_WhenApplePaySettingPresent_ShouldReturnThem() throws CommerceCartModificationException {
        testObj.getPaymentRequest(null);

        verify(checkoutComApplePayFacadeMock).getApplePayPaymentRequest();
    }

    @Test
    public void setDeliveryAddress_WhenUserIsRegistered_shouldOnlySaveShippingAddressAndReturnTheApplePaySHippingContactUpdate() throws DuplicateUidException {
        testObj.setDeliveryAddress(applePayPaymentContactMock);

        verify(checkoutComWalletAddressFacadeMock).handleAndSaveShippingAddress(applePayPaymentContactMock);
        verify(checkoutComApplePayFacadeMock).getApplePayShippingContactUpdate();
    }

    @Test
    public void ShouldCreateApplePayExpressGuestContact() throws DuplicateUidException {
        when(userFacadeMock.isAnonymousUser()).thenReturn(Boolean.TRUE);

        testObj.setDeliveryAddress(applePayPaymentContactMock);

        final InOrder inOrder = inOrder(defaultCheckoutComCustomerFacadeMock, checkoutComWalletAddressFacadeMock, checkoutComApplePayFacadeMock);
        inOrder.verify(defaultCheckoutComCustomerFacadeMock).createApplePayExpressCheckoutGuestUserForAnonymousCheckout();
        inOrder.verify(checkoutComWalletAddressFacadeMock).handleAndSaveShippingAddress(applePayPaymentContactMock);
        inOrder.verify(checkoutComApplePayFacadeMock).getApplePayShippingContactUpdate();
    }

    @Test
    public void setDeliveryMethod_WhenUserSelectADeliveryMethodOnApplePayPopUp_shouldUpdateCartAccordingly() {
        when(applePayShippingMethodMock.getIdentifier()).thenReturn(SHIPPING_METHOD_IDENTIFIER);

        testObj.setDeliveryMode(applePayShippingMethodMock);

        verify(checkoutComCheckoutFlowFacadeMock).setDeliveryMode(SHIPPING_METHOD_IDENTIFIER);
        verify(checkoutComApplePayFacadeMock).getApplePayShippingMethodUpdate();
    }
}
