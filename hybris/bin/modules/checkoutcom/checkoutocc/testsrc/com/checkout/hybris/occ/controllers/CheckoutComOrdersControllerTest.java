package com.checkout.hybris.occ.controllers;

import com.checkout.dto.order.CheckoutPlaceOrderDto;
import com.checkout.hybris.core.payment.exception.CheckoutComPaymentIntegrationException;
import com.checkout.hybris.facades.accelerator.CheckoutComCheckoutFlowFacade;
import com.checkout.hybris.facades.beans.AuthorizeResponseData;
import com.checkout.hybris.facades.payment.CheckoutComPaymentFacade;
import com.checkout.hybris.facades.payment.CheckoutComPaymentInfoFacade;
import com.checkout.hybris.occ.exceptions.NoCheckoutCartException;
import com.checkout.hybris.occ.exceptions.PlaceOrderException;
import com.checkout.hybris.occ.validators.impl.CheckoutComPlaceOrderCartValidator;
import com.checkout.payments.GetPaymentResponse;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.acceleratorfacades.order.AcceleratorCheckoutFacade;
import de.hybris.platform.acceleratorservices.enums.CheckoutPciOptionEnum;
import de.hybris.platform.commercefacades.order.CartFacade;
import de.hybris.platform.commercefacades.order.CheckoutFacade;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.CartModificationData;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.commerceservices.order.CommerceCartModificationException;
import de.hybris.platform.commercewebservicescommons.dto.order.OrderWsDTO;
import de.hybris.platform.commercewebservicescommons.errors.exceptions.PaymentAuthorizationException;
import de.hybris.platform.order.InvalidCartException;
import de.hybris.platform.webservicescommons.errors.exceptions.WebserviceValidationException;
import de.hybris.platform.webservicescommons.mapping.DataMapper;
import de.hybris.platform.webservicescommons.mapping.FieldSetLevelHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComOrdersControllerTest {

    private static final String REDIRECT_URL = "/";
    private static final String DEFAULT_FIELD_SET = FieldSetLevelHelper.DEFAULT_LEVEL;
    private static final String MOCK_CKO_SESSION_ID = "mockCkoSessionId";
    private static final String CKO_SESSION_ID_JSON = "{\"cko-session-id\" : \"mockCkoSessionId\"}";

    @Spy
    @InjectMocks
    private CheckoutComOrdersController testObj;

    @Mock
    private DataMapper dataMapperMock;
    @Mock
    private CartFacade cartFacadeMock;
    @Mock
    private CheckoutFacade checkoutFacadeMock;
    @Mock
    private CheckoutComCheckoutFlowFacade checkoutFlowFacadeMock;
    @Mock
    private CheckoutComPaymentFacade checkoutComPaymentFacadeMock;
    @Mock
    private CheckoutComPaymentInfoFacade checkoutComPaymentInfoFacadeMock;
    @Mock
    private AcceleratorCheckoutFacade acceleratorCheckoutFacadeCheckoutFacadeMock;
    @Mock
    private CheckoutComPlaceOrderCartValidator checkoutComPlaceOrderCartValidatorMock;

    @Mock
    private CartData cartDataMock;
    @Mock
    private OrderData orderDataMock;
    @Mock
    private OrderWsDTO orderWsDTOMock;
    @Mock
    private GetPaymentResponse paymentResponseMock;
    @Mock
    private AuthorizeResponseData authorizeResponseDataMock;
    @Mock
    private CartModificationData commerceCartModificationMock;
    @Mock
    private CheckoutPlaceOrderDto checkoutPlaceOrderDtoMock;

    @Before
    public void setUp() throws Exception {
        doReturn(checkoutPlaceOrderDtoMock).when(testObj).convertObjectToPlaceOrderDto(CKO_SESSION_ID_JSON);
        when(checkoutPlaceOrderDtoMock.getCkoSessionId()).thenReturn(MOCK_CKO_SESSION_ID);
        when(checkoutFacadeMock.hasCheckoutCart()).thenReturn(Boolean.TRUE);
        when(cartFacadeMock.validateCartData()).thenReturn(Collections.emptyList());
        when(cartFacadeMock.getSessionCart()).thenReturn(cartDataMock);
        when(checkoutFlowFacadeMock.authorizePayment()).thenReturn(authorizeResponseDataMock);
        when(checkoutFlowFacadeMock.getCurrentPaymentMethodType()).thenReturn("CARD");
        when(authorizeResponseDataMock.getIsSuccess()).thenReturn(Boolean.TRUE);
        when(authorizeResponseDataMock.getIsRedirect()).thenReturn(Boolean.FALSE);
        when(acceleratorCheckoutFacadeCheckoutFacadeMock.placeOrder()).thenReturn(orderDataMock);
        when(dataMapperMock.map(orderDataMock, OrderWsDTO.class, DEFAULT_FIELD_SET)).thenReturn(orderWsDTOMock);
    }

    @Test(expected = NoCheckoutCartException.class)
    public void placeDirectOrder_WhenThereIsNoCheckoutCart_ShouldThrowException() throws InvalidCartException,
            PaymentAuthorizationException, PlaceOrderException, NoCheckoutCartException {
        when(checkoutFacadeMock.hasCheckoutCart()).thenReturn(Boolean.FALSE);

        testObj.placeDirectOrder(DEFAULT_FIELD_SET);

        verify(acceleratorCheckoutFacadeCheckoutFacadeMock, never()).placeOrder();
    }

    @Test(expected = InvalidCartException.class)
    public void placeDirectOrder_WhenCheckoutCartCanNotBeCalculated_ShouldThrowException() throws InvalidCartException,
            PaymentAuthorizationException, PlaceOrderException, NoCheckoutCartException, CommerceCartModificationException {
        doThrow(CommerceCartModificationException.class).when(cartFacadeMock).validateCartData();

        testObj.placeDirectOrder(DEFAULT_FIELD_SET);

        verify(acceleratorCheckoutFacadeCheckoutFacadeMock, never()).placeOrder();
    }

    @Test(expected = WebserviceValidationException.class)
    public void placeDirectOrder_WhenCheckoutCartHasModifications_ShouldThrowException() throws InvalidCartException,
            PaymentAuthorizationException, PlaceOrderException, NoCheckoutCartException, CommerceCartModificationException {
        when(cartFacadeMock.validateCartData()).thenReturn(List.of(commerceCartModificationMock));

        testObj.placeDirectOrder(DEFAULT_FIELD_SET);

        verify(acceleratorCheckoutFacadeCheckoutFacadeMock, never()).placeOrder();
    }

    @Test(expected = PaymentAuthorizationException.class)
    public void placeDirectOrder_WhenAuthorizationIsNotSuccessAndIsNotRedirect_ShouldThrowException() throws InvalidCartException,
            PaymentAuthorizationException, PlaceOrderException, NoCheckoutCartException {
        when(authorizeResponseDataMock.getIsSuccess()).thenReturn(Boolean.FALSE);

        testObj.placeDirectOrder(DEFAULT_FIELD_SET);

        verify(acceleratorCheckoutFacadeCheckoutFacadeMock, never()).placeOrder();
    }

    @Test
    public void placeDirectOrder_WhenAuthorizationIsNotSuccessAndIsRedirect_ShouldReturnRedirect() throws InvalidCartException,
            PaymentAuthorizationException, PlaceOrderException, NoCheckoutCartException {
        when(authorizeResponseDataMock.getIsSuccess()).thenReturn(Boolean.FALSE);
        when(authorizeResponseDataMock.getIsRedirect()).thenReturn(Boolean.TRUE);
        when(authorizeResponseDataMock.getRedirectUrl()).thenReturn(REDIRECT_URL);

        final OrderWsDTO result = testObj.placeDirectOrder(DEFAULT_FIELD_SET);

        assertThat(result.getRedirectUrl()).isEqualTo(REDIRECT_URL);
        verify(acceleratorCheckoutFacadeCheckoutFacadeMock, never()).placeOrder();
    }

    @Test
    public void placeDirectOrder_WhenAuthorizationIsSuccessAndIsNotRedirect_ShouldPlaceOrder() throws InvalidCartException,
            PaymentAuthorizationException, PlaceOrderException, NoCheckoutCartException {

        final OrderWsDTO result = testObj.placeDirectOrder(DEFAULT_FIELD_SET);

        assertThat(result).isEqualTo(orderWsDTOMock);
        verify(acceleratorCheckoutFacadeCheckoutFacadeMock).placeOrder();
    }

    @Test
    public void placeDirectOrder_WhenSecurityCodeIsPopulatedAndRequired__ShouldPlaceOrder() throws InvalidCartException,
            PaymentAuthorizationException, PlaceOrderException, NoCheckoutCartException {
        when(checkoutFlowFacadeMock.getSubscriptionPciOption()).thenReturn(CheckoutPciOptionEnum.DEFAULT);

        final OrderWsDTO result = testObj.placeDirectOrder(DEFAULT_FIELD_SET);

        assertThat(result).isEqualTo(orderWsDTOMock);
        verify(acceleratorCheckoutFacadeCheckoutFacadeMock).placeOrder();
    }

    @Test(expected = PlaceOrderException.class)
    public void placeDirectOrder_WhenPlaceOrderThrowsException_ShouldThrowException() throws InvalidCartException,
            PaymentAuthorizationException, PlaceOrderException, NoCheckoutCartException {
        when(checkoutFlowFacadeMock.getSubscriptionPciOption()).thenReturn(CheckoutPciOptionEnum.DEFAULT);
        doThrow(InvalidCartException.class).when(acceleratorCheckoutFacadeCheckoutFacadeMock).placeOrder();

        testObj.placeDirectOrder(DEFAULT_FIELD_SET);

        verify(checkoutFlowFacadeMock).removePaymentInfoFromSessionCart();
        verify(acceleratorCheckoutFacadeCheckoutFacadeMock, never()).placeOrder();
    }

    @Test
    public void placeDirectOrder_WhenKlarna_ShouldNotAuthorize() throws InvalidCartException, PaymentAuthorizationException, PlaceOrderException, NoCheckoutCartException {
        when(checkoutFlowFacadeMock.getCurrentPaymentMethodType()).thenReturn("KLARNA");

        final OrderWsDTO result = testObj.placeDirectOrder(DEFAULT_FIELD_SET);

        assertThat(result).isEqualTo(orderWsDTOMock);
        verify(acceleratorCheckoutFacadeCheckoutFacadeMock).placeOrder();
        verify(checkoutFlowFacadeMock, never()).authorizePayment();
    }

    @Test
    public void placeOrderSuccessRedirect_ShouldThrowException_WhenCkoSessionIdIsBlank() throws InvalidCartException {
        when(checkoutPlaceOrderDtoMock.getCkoSessionId()).thenReturn(EMPTY);

        assertThatThrownBy(() -> testObj.placeRedirectOrder(CKO_SESSION_ID_JSON, DEFAULT_FIELD_SET)).isInstanceOf(PlaceOrderException.class);

        verify(checkoutComPaymentInfoFacadeMock, never()).processPaymentDetails(paymentResponseMock);
        verify(checkoutFacadeMock, never()).placeOrder();
    }

    @Test
    public void placeOrderSuccessRedirect_ShouldThrowException_WhenCanNotGetPaymentFromSessionIdFromCheckoutCom() throws InvalidCartException {
        doThrow(CheckoutComPaymentIntegrationException.class).when(checkoutComPaymentFacadeMock).getPaymentDetailsByCkoSessionId(MOCK_CKO_SESSION_ID);

        assertThatThrownBy(() -> testObj.placeRedirectOrder(CKO_SESSION_ID_JSON, DEFAULT_FIELD_SET)).isInstanceOf(PlaceOrderException.class);

        verify(checkoutFlowFacadeMock).removePaymentInfoFromSessionCart();
        verify(checkoutComPaymentInfoFacadeMock, never()).processPaymentDetails(paymentResponseMock);
        verify(checkoutFacadeMock, never()).placeOrder();
    }

    @Test
    public void placeOrderSuccessRedirect_ShouldThrowException_WhenCkoPaymentDetailsAreEmpty() throws InvalidCartException {
        when(checkoutComPaymentFacadeMock.getPaymentDetailsByCkoSessionId(MOCK_CKO_SESSION_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> testObj.placeRedirectOrder(CKO_SESSION_ID_JSON, DEFAULT_FIELD_SET)).isInstanceOf(PlaceOrderException.class);

        verify(checkoutComPaymentInfoFacadeMock, never()).processPaymentDetails(paymentResponseMock);
        verify(checkoutFacadeMock, never()).placeOrder();
    }

    @Test
    public void placeOrderSuccessRedirect_ShouldThrowException_WhenSessionCartPaymentDetailsDoesNotMatchWithCkoPaymentDetails() throws InvalidCartException {
        when(checkoutComPaymentFacadeMock.getPaymentDetailsByCkoSessionId(MOCK_CKO_SESSION_ID)).thenReturn(Optional.of(paymentResponseMock));
        when(checkoutComPaymentFacadeMock.doesSessionCartMatchAuthorizedCart(paymentResponseMock)).thenReturn(Boolean.FALSE);

        assertThatThrownBy(() -> testObj.placeRedirectOrder(CKO_SESSION_ID_JSON, DEFAULT_FIELD_SET)).isInstanceOf(PlaceOrderException.class);

        verify(checkoutComPaymentInfoFacadeMock, never()).processPaymentDetails(paymentResponseMock);
        verify(checkoutFacadeMock, never()).placeOrder();
    }

    @Test
    public void placeRedirectOrder_WhenPlaceOrder_ShouldThrowException() throws InvalidCartException {
        when(checkoutComPaymentFacadeMock.getPaymentDetailsByCkoSessionId(MOCK_CKO_SESSION_ID)).thenReturn(Optional.of(paymentResponseMock));
        when(checkoutComPaymentFacadeMock.doesSessionCartMatchAuthorizedCart(paymentResponseMock)).thenReturn(Boolean.TRUE);
        doThrow(InvalidCartException.class).when(checkoutFacadeMock).placeOrder();

        assertThatThrownBy(() -> testObj.placeRedirectOrder(CKO_SESSION_ID_JSON, DEFAULT_FIELD_SET)).isInstanceOf(PlaceOrderException.class);

        verify(checkoutComPaymentInfoFacadeMock).processPaymentDetails(paymentResponseMock);
        verify(checkoutFlowFacadeMock).removePaymentInfoFromSessionCart();
    }

    @Test
    public void placeRedirectOrder_WhenEverythingIsCorrect_ShouldPlaceOrder() throws PlaceOrderException, InvalidCartException {
        when(checkoutComPaymentFacadeMock.getPaymentDetailsByCkoSessionId(MOCK_CKO_SESSION_ID)).thenReturn(Optional.of(paymentResponseMock));
        when(checkoutComPaymentFacadeMock.doesSessionCartMatchAuthorizedCart(paymentResponseMock)).thenReturn(Boolean.TRUE);

        testObj.placeRedirectOrder(CKO_SESSION_ID_JSON, DEFAULT_FIELD_SET);

        verify(checkoutComPaymentInfoFacadeMock).processPaymentDetails(paymentResponseMock);
        verify(checkoutFacadeMock).placeOrder();
    }

    @Test
    public void convertObjectToPlaceOrderDto_WhenObjectIsValid_ShouldConvertItToWsDto() throws PlaceOrderException {
        final CheckoutPlaceOrderDto result = testObj.convertObjectToPlaceOrderDto(CKO_SESSION_ID_JSON);

        assertThat(result.getCkoSessionId()).isEqualTo(MOCK_CKO_SESSION_ID);
    }

    @Test
    public void convertObjectToPlaceOrderDto_WhenObjectIsValid_ShouldThrowException() {
        assertThatThrownBy(() -> testObj.convertObjectToPlaceOrderDto("{}")).isInstanceOf(PlaceOrderException.class);
    }
}
