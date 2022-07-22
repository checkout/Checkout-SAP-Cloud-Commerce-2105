package com.checkout.hybris.occ.controllers;

import com.checkout.dto.order.PlaceWalletOrderWsDTO;
import com.checkout.hybris.facades.address.CheckoutComWalletAddressFacade;
import com.checkout.hybris.facades.beans.*;
import com.checkout.hybris.facades.customer.CheckoutComCustomerFacade;
import com.checkout.hybris.facades.payment.google.CheckoutComGooglePayFacade;
import com.checkout.hybris.facades.payment.wallet.CheckoutComWalletOrderFacade;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commercefacades.order.CartFacade;
import de.hybris.platform.commercefacades.order.CheckoutFacade;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.user.UserFacade;
import de.hybris.platform.commerceservices.customer.DuplicateUidException;
import de.hybris.platform.servicelayer.i18n.I18NService;
import de.hybris.platform.webservicescommons.mapping.DataMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.validation.Validator;

import java.util.Locale;

import static com.checkout.hybris.facades.enums.WalletPaymentType.GOOGLEPAY;
import static de.hybris.platform.webservicescommons.mapping.FieldSetLevelHelper.DEFAULT_LEVEL;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComGooglePayOrderControllerTest {

    private static final String EMAIL_ADDRESS = "emailAddress";
    private static final String GIVEN_NAME = "givenName";

    @InjectMocks
    private CheckoutComGooglePayOrderController testObj;

    @Mock
    private UserFacade userFacadeMock;
    @Mock
    private CheckoutComCustomerFacade defaultCheckoutComCustomerFacadeMock;
    @Mock
    private CheckoutFacade checkoutFacadeMock;
    @Mock
    private CartFacade cartFacadeMock;
    @Mock
    private CheckoutComWalletOrderFacade checkoutComWalletOrderFacadeMock;
    @Mock
    private CheckoutComWalletAddressFacade checkoutComWalletAddressFacadeMock;
    @Mock
    private DataMapper dataMapperMock;
    @Mock
    private CheckoutComGooglePayFacade checkoutComGooglePayFacadeMock;
    @Mock
    private Validator checkoutComPlaceOrderCartValidatorMock;
    @Mock
    private MessageSource messageSourceMock;
    @Mock
    private I18NService i18nServiceMock;

    @Mock
    private GooglePayAuthorisationRequest authorisationRequestMock;
    @Mock
    private GooglePayPaymentContact paymentContactMock;
    @Mock
    private GooglePayPaymentToken paymentTokenMock;
    @Mock
    private PlaceWalletOrderDataResponse placeWalletOrderDataResponseMock;
    @Mock
    private CartData cartDataMock;
    @Mock
    private GooglePayIntermediatePaymentData intermediatePaymentDataMock;
    @Mock
    private GooglePayIntermediateAddress intermediateAddressMock;
    @Mock
    private GooglePaySelectionOptionData selectionOptionDataMock;

    @Captor
    private ArgumentCaptor<PlaceWalletOrderDataResponse> orderDataResponseArgumentCaptor;

    @Before
    public void setUp() {
        when(userFacadeMock.isAnonymousUser()).thenReturn(Boolean.FALSE);
        when(authorisationRequestMock.getBillingAddress()).thenReturn(paymentContactMock);
        when(authorisationRequestMock.getShippingAddress()).thenReturn(paymentContactMock);
        when(authorisationRequestMock.getEmail()).thenReturn(EMAIL_ADDRESS);
        when(paymentContactMock.getEmail()).thenReturn(EMAIL_ADDRESS);
        when(paymentContactMock.getName()).thenReturn(GIVEN_NAME);
        when(authorisationRequestMock.getToken()).thenReturn(paymentTokenMock);
        when(checkoutComWalletOrderFacadeMock.placeWalletOrder(paymentTokenMock, GOOGLEPAY)).thenReturn(placeWalletOrderDataResponseMock);
        when(defaultCheckoutComCustomerFacadeMock.isGooglePayExpressGuestCustomer()).thenReturn(Boolean.TRUE);
        when(checkoutFacadeMock.hasCheckoutCart()).thenReturn(Boolean.TRUE);
        when(cartFacadeMock.getSessionCart()).thenReturn(cartDataMock);
        doNothing().when(checkoutComPlaceOrderCartValidatorMock).validate(eq(cartDataMock), any());
        when(i18nServiceMock.getCurrentLocale()).thenReturn(Locale.ENGLISH);
        when(messageSourceMock.getMessage("checkout.placeOrder.failed", null, Locale.ENGLISH)).thenReturn("error msg");

        when(intermediatePaymentDataMock.getShippingAddress()).thenReturn(intermediateAddressMock);
        when(intermediatePaymentDataMock.getShippingOptionData()).thenReturn(selectionOptionDataMock);
        when(intermediatePaymentDataMock.getCallbackTrigger()).thenReturn("callback_trigger_test");
    }

    @Test
    public void placeOrder_WhenNoCheckoutCart_ShouldAddErrorToResponse() {
        when(checkoutFacadeMock.hasCheckoutCart()).thenReturn(Boolean.FALSE);

        testObj.placeOrder(authorisationRequestMock, DEFAULT_LEVEL);

        verify(dataMapperMock).map(orderDataResponseArgumentCaptor.capture(), eq(PlaceWalletOrderWsDTO.class), eq(DEFAULT_LEVEL));
        assertThat(orderDataResponseArgumentCaptor.getValue().getErrorMessage()).isNotEmpty();
    }

    @Test
    public void getPaymentRequest_ShouldCallCheckoutComGooglePayFacade() {
        testObj.getMerchantConfiguration();

        verify(checkoutComGooglePayFacadeMock).getGooglePayMerchantConfiguration();
    }

    @Test
    public void placeOrder_ShouldPlaceOrderWithGooglePay() {
        testObj.placeOrder(authorisationRequestMock, DEFAULT_LEVEL);

        final InOrder inOrder = inOrder(defaultCheckoutComCustomerFacadeMock, checkoutComWalletAddressFacadeMock, checkoutComWalletOrderFacadeMock, dataMapperMock);
        inOrder.verify(defaultCheckoutComCustomerFacadeMock).updateExpressCheckoutUserEmail(EMAIL_ADDRESS, GIVEN_NAME);
        inOrder.verify(checkoutComWalletAddressFacadeMock).handleAndSaveBillingAddress(paymentContactMock);
        inOrder.verify(checkoutComWalletAddressFacadeMock).handleAndSaveShippingAddress(paymentContactMock);
        inOrder.verify(checkoutComWalletOrderFacadeMock).placeWalletOrder(paymentTokenMock, GOOGLEPAY);
        verify(dataMapperMock).map(placeWalletOrderDataResponseMock, PlaceWalletOrderWsDTO.class, DEFAULT_LEVEL);
    }

    @Test
    public void placeOrder_ShouldPlaceOrderWithGooglePay_WhenUserIsGuest() {
        when(userFacadeMock.isAnonymousUser()).thenReturn(Boolean.TRUE);

        testObj.placeOrder(authorisationRequestMock, DEFAULT_LEVEL);

        verify(dataMapperMock).map(placeWalletOrderDataResponseMock, PlaceWalletOrderWsDTO.class, DEFAULT_LEVEL);
    }

    @Test
    public void getGooglePayDeliveryInfo_WhenAnonymousUser_ShouldUpdateDeliveryModeAndDeliveryAddress() throws DuplicateUidException {
        when(userFacadeMock.isAnonymousUser()).thenReturn(Boolean.TRUE);

        testObj.getGooglePayDeliveryInfo(intermediatePaymentDataMock);

        verify(defaultCheckoutComCustomerFacadeMock).createGooglePayExpressCheckoutGuestUserForAnonymousCheckout();
        verify(checkoutComGooglePayFacadeMock).getGooglePayDeliveryInfo(intermediatePaymentDataMock);
    }

}
