package com.checkout.hybris.facades.payment.wallet.impl;

import com.checkout.hybris.core.payment.exception.CheckoutComPaymentIntegrationException;
import com.checkout.hybris.facades.accelerator.CheckoutComCheckoutFlowFacade;
import com.checkout.hybris.facades.beans.AuthorizeResponseData;
import com.checkout.hybris.facades.beans.PlaceWalletOrderDataResponse;
import com.checkout.hybris.facades.beans.WalletPaymentAdditionalAuthInfo;
import com.checkout.hybris.facades.beans.WalletPaymentInfoData;
import com.checkout.hybris.facades.payment.CheckoutComPaymentFacade;
import com.checkout.hybris.facades.payment.CheckoutComPaymentInfoFacade;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commercefacades.order.CartFacade;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.order.InvalidCartException;
import de.hybris.platform.servicelayer.i18n.I18NService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.context.MessageSource;

import static com.checkout.hybris.facades.enums.PlaceWalletOrderStatus.FAILURE;
import static com.checkout.hybris.facades.enums.PlaceWalletOrderStatus.SUCCESS;
import static com.checkout.hybris.facades.enums.WalletPaymentType.GOOGLEPAY;
import static java.util.Locale.ENGLISH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultCheckoutComWalletOrderFacadeTest {

    private static final String AUTHORIZATION_FAILED_ERROR_MSG = "checkout.error.authorization.failed";
    private static final String PLACE_ORDER_FAILED_ERROR_MSG = "checkout.placeOrder.failed";
    private static final String REDIRECT_URL = "/redirecturl";

    @InjectMocks
    private DefaultCheckoutComWalletOrderFacade testObj;

    @Mock
    private CheckoutComPaymentFacade checkoutComPaymentFacadeMock;
    @Mock
    private MessageSource messageSourceMock;
    @Mock
    private CheckoutComPaymentInfoFacade checkoutComPaymentInfoFacadeMock;
    @Mock
    private CheckoutComCheckoutFlowFacade checkoutFlowFacadeMock;
    @Mock
    private I18NService i18nServiceMock;
    @Mock
    private CartFacade cartFacadeMock;
    @Mock
    private Validator checkoutComPlaceOrderCartValidatorMock;

    @Mock
    private WalletPaymentAdditionalAuthInfo walletAdditionalInfoMock;
    @Mock
    private WalletPaymentInfoData paymentInfoDataMock;
    @Mock
    private AuthorizeResponseData authorizeResponseDataMock;
    @Mock
    private OrderData orderDataMock;
    @Mock
    private CartData cartDataMock;

    @Before
    public void setUp() throws Exception {
        doNothing().when(checkoutComPaymentInfoFacadeMock).addPaymentInfoToCart(paymentInfoDataMock);
        when(checkoutComPaymentFacadeMock.createCheckoutComWalletPaymentToken(walletAdditionalInfoMock, GOOGLEPAY)).thenReturn(paymentInfoDataMock);
        when(checkoutFlowFacadeMock.authorizePayment()).thenReturn(authorizeResponseDataMock);
        when(authorizeResponseDataMock.getIsSuccess()).thenReturn(true);
        when(checkoutFlowFacadeMock.placeOrder()).thenReturn(orderDataMock);
        when(i18nServiceMock.getCurrentLocale()).thenReturn(ENGLISH);
        when(messageSourceMock.getMessage("checkout.error.authorization.failed", null, ENGLISH)).thenReturn(AUTHORIZATION_FAILED_ERROR_MSG);
        when(messageSourceMock.getMessage("checkout.placeOrder.failed", null, ENGLISH)).thenReturn(PLACE_ORDER_FAILED_ERROR_MSG);
        doNothing().when(checkoutComPlaceOrderCartValidatorMock).validate(eq(cartDataMock), any());
    }

    @Test
    public void placeWalletOrder_WhenAuthorizationSuccess_ShouldPlaceOrder() {
        final PlaceWalletOrderDataResponse result = testObj.placeWalletOrder(walletAdditionalInfoMock, GOOGLEPAY);

        assertThat(result.getStatus()).isEqualTo(SUCCESS);
        assertThat(result.getOrderData()).isEqualTo(orderDataMock);
        assertThat(result.getErrorMessage()).isNull();
    }

    @Test
    public void placeWalletOrder_WhenAuthorizationRedirectUrl_ShouldPlaceOrder() throws InvalidCartException {
        when(authorizeResponseDataMock.getIsRedirect()).thenReturn(Boolean.TRUE);
        when(authorizeResponseDataMock.getRedirectUrl()).thenReturn(REDIRECT_URL);

        final PlaceWalletOrderDataResponse result = testObj.placeWalletOrder(walletAdditionalInfoMock, GOOGLEPAY);

        assertThat(result.getRedirectUrl()).isEqualTo(REDIRECT_URL);
        verify(checkoutFlowFacadeMock, never()).placeOrder();
    }

    @Test
    public void placeWalletOrder_WhenAuthorizationNotSuccess_ShouldNotPlaceOrder() {
        when(authorizeResponseDataMock.getIsSuccess()).thenReturn(false);

        final PlaceWalletOrderDataResponse result = testObj.placeWalletOrder(walletAdditionalInfoMock, GOOGLEPAY);

        verify(checkoutFlowFacadeMock).removePaymentInfoFromSessionCart();
        assertThat(result.getStatus()).isEqualTo(FAILURE);
        assertThat(result.getErrorMessage()).isEqualTo(AUTHORIZATION_FAILED_ERROR_MSG);
        assertThat(result.getOrderData()).isNull();
    }

    @Test
    public void placeWalletOrder_WhenRequestTokenThrowsError_ShouldNotPlaceOrder() {
        doThrow(CheckoutComPaymentIntegrationException.class).when(checkoutComPaymentFacadeMock).createCheckoutComWalletPaymentToken(walletAdditionalInfoMock, GOOGLEPAY);

        final PlaceWalletOrderDataResponse result = testObj.placeWalletOrder(walletAdditionalInfoMock, GOOGLEPAY);

        verify(checkoutFlowFacadeMock).removePaymentInfoFromSessionCart();
        assertThat(result.getStatus()).isEqualTo(FAILURE);
        assertThat(result.getErrorMessage()).isEqualTo(AUTHORIZATION_FAILED_ERROR_MSG);
        assertThat(result.getOrderData()).isNull();
    }

    @Test
    public void placeWalletOrder_WhenPlaceOrderFails_ShouldNotAddOrderData() throws InvalidCartException {
        doThrow(InvalidCartException.class).when(checkoutFlowFacadeMock).placeOrder();

        final PlaceWalletOrderDataResponse result = testObj.placeWalletOrder(walletAdditionalInfoMock, GOOGLEPAY);

        verify(checkoutFlowFacadeMock).removePaymentInfoFromSessionCart();
        assertThat(result.getStatus()).isEqualTo(FAILURE);
        assertThat(result.getErrorMessage()).isEqualTo(PLACE_ORDER_FAILED_ERROR_MSG);
        assertThat(result.getOrderData()).isNull();
    }

    @Test
    public void validateCartForPlaceOrder_WhenCartValid_ShouldNotAddError() throws InvalidCartException {
        final Errors errors = new BeanPropertyBindingResult(cartDataMock, "sessionCart");

        testObj.validateCartForPlaceOrder(checkoutComPlaceOrderCartValidatorMock);

        assertThat(errors.getAllErrors()).isEmpty();
    }
}
