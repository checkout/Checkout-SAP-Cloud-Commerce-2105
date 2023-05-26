package com.checkout.hybris.facades.accelerator.impl;

import com.checkout.hybris.core.address.services.CheckoutComAddressService;
import com.checkout.hybris.core.authorisation.AuthorizeResponse;
import com.checkout.hybris.core.model.CheckoutComAPMPaymentInfoModel;
import com.checkout.hybris.core.model.CheckoutComCreditCardPaymentInfoModel;
import com.checkout.hybris.core.payment.exception.CheckoutComPaymentIntegrationException;
import com.checkout.hybris.core.payment.request.CheckoutComRequestFactory;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentInfoService;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentIntegrationService;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentService;
import com.checkout.hybris.facades.beans.AuthorizeResponseData;
import com.checkout.hybris.facades.beans.CheckoutComPaymentInfoData;
import com.checkout.hybris.facades.constants.CheckoutFacadesConstants;
import com.checkout.payments.*;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.acceleratorfacades.flow.CheckoutFlowFacade;
import de.hybris.platform.commercefacades.order.CartFacade;
import de.hybris.platform.commercefacades.order.data.CCPaymentInfoData;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.order.CartService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultCheckoutComCheckoutFlowFacadeDecoratorTest {

    private static final String PAYMENT_ID = "paymentId";
    private static final String TOKEN = "TOKEN";
    private static final String APPROVED_RESPONSE_CODE = "10000";
    private static final String DECLINED_RESPONSE_CODE = "20005";
    private static final String CART_CODE = "cart-code";
    private static final String APM_TYPE_VALUE = "APM";

    @Spy
    @InjectMocks
    private DefaultCheckoutComCheckoutFlowFacadeDecorator testObj;

    @Mock
    private CheckoutFlowFacade checkoutFlowFacadeMock;
    @Mock
    private CartData cartDataMock;
    @Mock
    private CCPaymentInfoData ccPaymentInfoDataMock;
    @Mock
    private CheckoutComPaymentInfoData apmPaymentInfoDataMock;
    @Mock
    private Converter<AuthorizeResponse, AuthorizeResponseData> authorizeResponseConverterMock;
    @Mock
    private AddressModel addressModelMock, clonedAddressMock;
    @Mock
    private CartModel cartModelMock;
    @Mock
    private CartService cartServiceMock;
    @Mock
    private CartFacade cartFacadeMock;
    @Mock
    private CheckoutComAddressService addressServiceMock;
    @Mock
    private CheckoutComRequestFactory checkoutComRequestFactoryMock;
    @Mock
    private CheckoutComPaymentIntegrationService checkoutComPaymentIntegrationServiceMock;
    @Mock
    private CheckoutComCreditCardPaymentInfoModel checkoutComCreditCardPaymentInfoMock;
    @Mock
    private CheckoutComAPMPaymentInfoModel checkoutComAPMPaymentInfoMock;
    @Mock
    private PaymentRequest<RequestSource> requestMock;
    @Mock
    private PaymentResponse paymentResponseMock;
    @Mock
    private PaymentProcessed paymentMock;
    @Mock
    private CheckoutComPaymentInfoService paymentInfoServiceMock;
    @Mock
    private PaymentPending paymentPendingResponseMock;
    @Mock
    private ResponseSource paymentSourceMock;
    @Mock
    private CheckoutComPaymentService paymentServiceMock;
    @Mock
    private AuthorizeResponse authorizeResponseMock;
    @Mock
    private AuthorizeResponseData authorizeResponseDataMock;

    @Before
    public void setUp() {
        setUpPaymentInfo();
        setUpPaymentResponse();
        setUpTestObj();

        when(cartServiceMock.getSessionCart()).thenReturn(cartModelMock);
        when(cartServiceMock.hasSessionCart()).thenReturn(true);
        when(cartModelMock.getPaymentInfo()).thenReturn(checkoutComCreditCardPaymentInfoMock);
        when(cartModelMock.getCode()).thenReturn(CART_CODE);

        when(paymentServiceMock.handlePendingPaymentResponse(paymentPendingResponseMock, checkoutComAPMPaymentInfoMock)).thenReturn(authorizeResponseMock);
        ReflectionTestUtils.setField(testObj, "authorizeResponseConverter", authorizeResponseConverterMock);
        when(authorizeResponseConverterMock.convert(authorizeResponseMock)).thenReturn(authorizeResponseDataMock);
        when(paymentInfoServiceMock.isValidPaymentInfo(cartModelMock)).thenReturn(true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void authorizePayment_WhenCartIsNull_ShouldThrowException() {
        when(testObj.hasCheckoutCart()).thenReturn(false);

        testObj.authorizePayment();
    }

    @Test
    public void authorizePayment_WhenPaymentInfoIsNotValid_ShouldReturnFalse() {
        when(paymentInfoServiceMock.isValidPaymentInfo(cartModelMock)).thenReturn(false);

        final AuthorizeResponseData result = testObj.authorizePayment();

        assertFalse(result.getIsSuccess());
    }

    @Test
    public void authorizePayment_WhenThereIsPaymentIntegrationError_ShouldReturnSuccessFalse() {
        when(checkoutComPaymentIntegrationServiceMock.authorizePayment(requestMock)).thenThrow(new CheckoutComPaymentIntegrationException("Error"));

        final AuthorizeResponseData response = testObj.authorizePayment();

        assertFalse(response.getIsSuccess());
    }

    @Test
    public void authorizePayment_WhenPaymentResponseIsNull_ShouldReturnFalse() {
        when(paymentResponseMock.getPayment()).thenReturn(null);

        final AuthorizeResponseData result = testObj.authorizePayment();

        assertFalse(result.getIsSuccess());
    }

    @Test
    public void authorizePayment_WhenPaymentNotApproved_ShouldSavePaymentID_andReturnFalse() {
        when(paymentMock.isApproved()).thenReturn(false);

        final AuthorizeResponseData result = testObj.authorizePayment();

        assertFalse(result.getIsSuccess());
        verify(paymentInfoServiceMock).addPaymentId(PAYMENT_ID, checkoutComCreditCardPaymentInfoMock);
    }

    @Test
    public void authorizePayment_WhenPaymentResponseCodeIsNotApproved_ShouldReturnFalse() {
        when(paymentMock.getResponseCode()).thenReturn(DECLINED_RESPONSE_CODE);

        final AuthorizeResponseData result = testObj.authorizePayment();

        assertFalse(result.getIsSuccess());
    }

    @Test
    public void authorizePayment_WhenSessionUserDoNotMatchCartUser_ShouldReturnFalse() {
        doReturn(false).when(testObj).callSuperCheckIfCurrentUserIsTheCartUser();

        final AuthorizeResponseData result = testObj.authorizePayment();

        assertFalse(result.getIsSuccess());
    }

    @Test
    public void authorizePayment_WhenProcessWorkedProperlyNoThreeDS_ShouldReturnSuccessAndRedirectFalse() {
        final AuthorizeResponseData result = testObj.authorizePayment();

        assertTrue(result.getIsSuccess());
        assertFalse(result.getIsRedirect());
        assertTrue(result.getIsDataRequired());
        verify(paymentInfoServiceMock).addSubscriptionIdToUserPayment(checkoutComCreditCardPaymentInfoMock, paymentSourceMock);
        verify(paymentInfoServiceMock).addPaymentId(PAYMENT_ID, checkoutComCreditCardPaymentInfoMock);
    }

    @Test
    public void authorizePayment_WhenPaymentResponsePending_ShouldReturnTheCorrectAuthorizeResponseData() {
        when(cartModelMock.getPaymentInfo()).thenReturn(checkoutComAPMPaymentInfoMock);
        when(paymentResponseMock.isPending()).thenReturn(true);
        when(paymentResponseMock.getPending()).thenReturn(paymentPendingResponseMock);
        when(paymentResponseMock.getPayment()).thenReturn(null);

        final AuthorizeResponseData result = testObj.authorizePayment();

        assertEquals(authorizeResponseDataMock, result);
    }

    @Test
    public void removePaymentInfoFromSessionCart_WhenNoCart_ShouldDoNothing() {
        doReturn(false).when(testObj).hasCheckoutCart();

        testObj.removePaymentInfoFromSessionCart();

        verifyZeroInteractions(paymentInfoServiceMock);
    }

    @Test
    public void removePaymentInfoFromSessionCart_ShouldRemoveThePaymentInfo() {
        testObj.removePaymentInfoFromSessionCart();

        verify(paymentInfoServiceMock).removePaymentInfo(cartModelMock);
    }

    @Test
    public void setPaymentInfoBillingAddressOnSessionCart_WhenCartAndPaymentAddressArePresent_ShouldUpdateCart() {
        when(addressServiceMock.cloneAddress(addressModelMock)).thenReturn(clonedAddressMock);
        testObj.setPaymentInfoBillingAddressOnSessionCart();

        verify(cartServiceMock).getSessionCart();
        verify(addressServiceMock).setCartPaymentAddress(cartModelMock, clonedAddressMock);
    }

    @Test
    public void setPaymentInfoBillingAddressOnSessionCart_WhenNoSessionCart_ShouldDoNothing() {
        when(cartServiceMock.hasSessionCart()).thenReturn(false);

        testObj.setPaymentInfoBillingAddressOnSessionCart();

        verify(cartServiceMock, never()).getSessionCart();
        verifyZeroInteractions(addressServiceMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setPaymentInfoBillingAddressOnSessionCart_WhenPaymentInfoNull_ShouldThrowException() {
        when(cartModelMock.getPaymentInfo()).thenReturn(null);

        testObj.setPaymentInfoBillingAddressOnSessionCart();
    }

    @Test
    public void performExpressCheckout_WhenCalled_ShouldCreateTheExpressCheckoutResultAndSetTheBillingAddressOnCart() {
        testObj.performExpressCheckout();

        verify(testObj).performExpressCheckout();
        verify(testObj).setPaymentInfoBillingAddressOnSessionCart();
    }

    @Test
    public void hasNoPaymentInfo_WhenCartIsNull_shouldReturnTrue() {
        when(checkoutFlowFacadeMock.getCheckoutCart()).thenReturn(null);

        final boolean result = testObj.hasNoPaymentInfo();

        assertTrue(result);
    }

    @Test
    public void hasNoPaymentInfo_WhenCartIsNotNullAndHasNoPaymentInfo_shouldReturnTrue() {
        when(checkoutFlowFacadeMock.getCheckoutCart()).thenReturn(cartDataMock);
        when(cartDataMock.getPaymentInfo()).thenReturn(null);
        when(cartDataMock.getCheckoutComPaymentInfo()).thenReturn(null);

        final boolean result = testObj.hasNoPaymentInfo();

        assertTrue(result);
    }

    @Test
    public void hasNoPaymentInfo_WhenCartDataIsNotNullAndHasPaymentInfoAndAPMPaymentInfo_ShouldReturnFalse() {
        when(checkoutFlowFacadeMock.getCheckoutCart()).thenReturn(cartDataMock);
        when(cartDataMock.getPaymentInfo()).thenReturn(ccPaymentInfoDataMock);
        when(cartDataMock.getCheckoutComPaymentInfo()).thenReturn(apmPaymentInfoDataMock);

        final boolean result = testObj.hasNoPaymentInfo();

        assertFalse(result);
    }

    @Test
    public void hasNoPaymentInfo_WhenCartHasNoCCPaymentInfoAndHasAPMPaymentInfo_ShouldReturnFalse() {
        when(checkoutFlowFacadeMock.getCheckoutCart()).thenReturn(cartDataMock);
        when(cartDataMock.getPaymentInfo()).thenReturn(null);
        when(cartDataMock.getCheckoutComPaymentInfo()).thenReturn(apmPaymentInfoDataMock);

        final boolean result = testObj.hasNoPaymentInfo();

        assertFalse(result);
    }

    @Test
    public void hasNoPaymentInfo_WhenCartHasNoAPMPaymentInfo_ShouldReturnFalse() {
        when(checkoutFlowFacadeMock.getCheckoutCart()).thenReturn(cartDataMock);
        when(cartDataMock.getCheckoutComPaymentInfo()).thenReturn(null);
        when(cartDataMock.getPaymentInfo()).thenReturn(ccPaymentInfoDataMock);

        final boolean result = testObj.hasNoPaymentInfo();

        assertFalse(result);
    }

    @Test
    public void isUserDataRequiredApmPaymentMethod_ShouldCallService() {
        testObj.isUserDataRequiredApmPaymentMethod();

        verify(paymentInfoServiceMock).isUserDataRequiredApmPaymentMethod(cartModelMock);
    }

    @Test
    public void getCurrentPaymentMethodType_WhenCheckoutPaymentInfoNull_ShouldReturnCardType() {
        when(cartDataMock.getCheckoutComPaymentInfo()).thenReturn(null);

        final String result = testObj.getCurrentPaymentMethodType();

        assertEquals(CheckoutFacadesConstants.CARD_PAYMENT_METHOD, result);
    }

    @Test
    public void getCurrentPaymentMethodType_WhenCheckoutPaymentInfoNotNull_ShouldReturnThePaymentInfoType() {
        when(cartDataMock.getCheckoutComPaymentInfo()).thenReturn(apmPaymentInfoDataMock);
        when(apmPaymentInfoDataMock.getType()).thenReturn(APM_TYPE_VALUE);

        final String result = testObj.getCurrentPaymentMethodType();

        assertEquals(APM_TYPE_VALUE, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getCurrentPaymentMethodType_WhenCartIsNull_ShouldThrowException() {
        doReturn(null).when(testObj).getCheckoutCart();

        testObj.getCurrentPaymentMethodType();
    }

    @Test
    public void authorizePayment_WhenErrorDuringCreatePaymentRequest_ShouldReturnSuccessFalse() {
        doThrow(IllegalArgumentException.class).when(checkoutComRequestFactoryMock).createPaymentRequest(cartModelMock);

        final AuthorizeResponseData response = testObj.authorizePayment();

        assertFalse(response.getIsSuccess());
    }

    private void setUpTestObj() {
        doReturn(cartDataMock).when(testObj).getCheckoutCart();
        doReturn(true).when(testObj).hasCheckoutCart();
        doReturn(true).when(testObj).callSuperCheckIfCurrentUserIsTheCartUser();
        doReturn(null).when(testObj).callSuperExpressCheckoutResult();

        testObj.setCartFacade(cartFacadeMock);
        testObj.setCartService(cartServiceMock);
    }

    private void setUpPaymentInfo() {
        when(checkoutComCreditCardPaymentInfoMock.getCardToken()).thenReturn(TOKEN);
        when(checkoutComCreditCardPaymentInfoMock.getBillingAddress()).thenReturn(addressModelMock);
        when(checkoutComRequestFactoryMock.createPaymentRequest(cartModelMock)).thenReturn(requestMock);
        when(checkoutComPaymentIntegrationServiceMock.authorizePayment(requestMock)).thenReturn(paymentResponseMock);
    }

    private void setUpPaymentResponse() {
        when(paymentResponseMock.getPayment()).thenReturn(paymentMock);
        when(paymentResponseMock.getPending()).thenReturn(paymentPendingResponseMock);
        when(paymentPendingResponseMock.getId()).thenReturn(PAYMENT_ID);
        when(paymentMock.getId()).thenReturn(PAYMENT_ID);
        when(paymentMock.getSource()).thenReturn(paymentSourceMock);
        when(paymentMock.isApproved()).thenReturn(true);
        when(paymentMock.getResponseCode()).thenReturn(APPROVED_RESPONSE_CODE);
    }
}
