package com.checkout.hybris.occ.controllers;

import com.checkout.dto.plaidlink.PlaidLinkCreationResponseDTO;
import com.checkout.hybris.facades.accelerator.CheckoutComCheckoutFlowFacade;
import com.checkout.hybris.facades.beans.AchBankInfoDetailsData;
import com.checkout.hybris.facades.beans.AuthorizeResponseData;
import com.checkout.hybris.facades.beans.PlaidLinkCreationResponse;
import com.checkout.hybris.facades.payment.CheckoutComACHConsentFacade;
import com.checkout.hybris.facades.payment.ach.CheckoutComAchFacade;
import com.checkout.hybris.facades.payment.ach.consent.exceptions.CustomerConsentException;
import com.checkout.hybris.facades.payment.plaidlink.CheckoutComPlaidLinkFacade;
import com.checkout.hybris.occ.exceptions.PlaceOrderException;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.acceleratorfacades.order.AcceleratorCheckoutFacade;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.commercewebservicescommons.dto.order.OrderWsDTO;
import de.hybris.platform.commercewebservicescommons.errors.exceptions.PaymentAuthorizationException;
import de.hybris.platform.order.InvalidCartException;
import de.hybris.platform.webservicescommons.mapping.DataMapper;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@UnitTest
public class CheckoutComAchPlaidLinkOrderControllerTest {
    private static final String PUBLIC_TOKEN = "public-token";

    @Spy
    @InjectMocks
    private CheckoutComAchPlaidLinkOrderController testObj;

    @Mock
    private DataMapper dataMapperMock;
    @Mock
    private CheckoutComAchFacade checkoutComACHFacadeMock;
    @Mock
    private AcceleratorCheckoutFacade acceleratorCheckoutFacadeMock;
    @Mock
    private CheckoutComPlaidLinkFacade checkoutComPlaidLinkFacadeMock;
    @Mock
    private CheckoutComACHConsentFacade checkoutComACHConsentFacadeMock;
    @Mock
    private CheckoutComCheckoutFlowFacade checkoutComCheckoutFlowFacadeMock;
    private final PlaidLinkCreationResponse plaidLinkCreationResponse = new PlaidLinkCreationResponse();

    @Test
    public void linkTokenCreate_ShouldCallPlaidLinkFacade() throws IOException {
        testObj.linkTokenCreate();

        verify(checkoutComPlaidLinkFacadeMock).linkTokenCreate();
    }

    @Test
    public void itemPublicTokenExchange_shouldCallPlaidLinkAttachPaymentToCartAndReturnACartWsDTO() throws PaymentAuthorizationException, PlaceOrderException, IOException, CustomerConsentException {
        final PlaidLinkCreationResponseDTO plaidLinkCreationResponseDTO = createPlaidLinkCreationResponseDTO();
        final AchBankInfoDetailsData achBankInfoDetailsData =
            ensureBankAccountDetailsAreReturnedForPlaidLinkCreationResponseDTO(
                plaidLinkCreationResponseDTO);
        setPaymentInfoAchToCart(achBankInfoDetailsData);
        final OrderWsDTO orderWsDTO = authorisePlaceOrderAndRedirectToResultPageReturnsADTO();

        final OrderWsDTO result = testObj.itemPublicTokenExchange(plaidLinkCreationResponseDTO);

        assertThat(result).isSameAs(orderWsDTO);
        final InOrder inOrder = inOrder(checkoutComPlaidLinkFacadeMock, checkoutComACHFacadeMock, checkoutComACHConsentFacadeMock);
        inOrder.verify(checkoutComPlaidLinkFacadeMock).getBankAccountDetailsData(plaidLinkCreationResponse);
        inOrder.verify(checkoutComACHFacadeMock).setPaymentInfoAchToCart(achBankInfoDetailsData);
        inOrder.verify(checkoutComACHConsentFacadeMock).createCheckoutComACHConsent(achBankInfoDetailsData, true);
    }

    @Test
    public void authorisePlaceOrderAndRedirectToResultPage_shouldReturnOrderWsDTOWhenSuccessfulAuth_WhenAuthorizationIsSuccessful() throws PlaceOrderException, PaymentAuthorizationException {
        callAuthorizePaymentWithSuccessAs(true);
        final OrderData orderData = placeOrderSuccessfully();
        final OrderWsDTO convertedDTO = convertOrderDataToDTO(orderData);

        final OrderWsDTO result = testObj.authorisePlaceOrderAndRedirectToResultPage();

        assertThat(convertedDTO).isSameAs(result);
    }

    @Test
    public void authorisePlaceOrderAndRedirectToResultPage_shouldThrowPaymentAuthorizationExceptionWhenAuthorizeIsNotSuccessful() throws PlaceOrderException, PaymentAuthorizationException {
        callAuthorizePaymentWithSuccessAs(false);

        assertThatThrownBy(() -> testObj.authorisePlaceOrderAndRedirectToResultPage()).isInstanceOf(
            PaymentAuthorizationException.class);

    }

    @Test
    public void placeOrder_shouldCallPlaceOrderAndReturnedOrderDataShouldBeTheSameAsTheFacadeCalls() throws InvalidCartException, PlaceOrderException {
        final OrderData order = callAcceleratorFacadePlaceOrder();

        final OrderData result = testObj.placeOrder();

        verify(acceleratorCheckoutFacadeMock).placeOrder();
        assertThat(result).isSameAs(order);
    }

    @Test
    public void placeOrder_shouldThrowPlaceOrderExceptionAndRemovePaymentFromCartWhenFacadePlaceOrderThrowsAnException() throws InvalidCartException {
        callAuthorizePaymentWithSuccessAs(true);
        doThrow(new InvalidCartException("Invalid Cart")).when(acceleratorCheckoutFacadeMock).placeOrder();

        assertThatThrownBy(() -> testObj.placeOrder()).isInstanceOf(PlaceOrderException.class);

        verify(checkoutComCheckoutFlowFacadeMock).removePaymentInfoFromSessionCart();
    }

    @Test
    public void convertOrderDataToDTO_shouldCallDataMapperToTransformTheOrderDataInDTO() {
        final OrderData orderData = new OrderData();
        final OrderWsDTO orderWsDTO = new OrderWsDTO();
        when(dataMapperMock.map(orderData, OrderWsDTO.class)).thenReturn(orderWsDTO);

        final OrderWsDTO result = testObj.convertOrderDataToDTO(orderData);

        verify(dataMapperMock).map(orderData, OrderWsDTO.class);
        assertThat(result).isSameAs(orderWsDTO);
    }

    @NotNull
    private OrderData callAcceleratorFacadePlaceOrder() throws InvalidCartException {
        final OrderData order = new OrderData();
        when(acceleratorCheckoutFacadeMock.placeOrder()).thenReturn(order);
        return order;
    }

    private OrderWsDTO convertOrderDataToDTO(final OrderData orderData) {
        final OrderWsDTO orderWsDTO = new OrderWsDTO();
        doReturn(orderWsDTO).when(testObj).convertOrderDataToDTO(orderData);

        return orderWsDTO;
    }

    @NotNull
    private OrderData placeOrderSuccessfully() throws PlaceOrderException {
        final OrderData orderData = new OrderData();
        doReturn(orderData).when(testObj).placeOrder();
        return orderData;
    }

    private void callAuthorizePaymentWithSuccessAs(final boolean successful) {
        final AuthorizeResponseData authorizeResponseData = new AuthorizeResponseData();
        authorizeResponseData.setIsSuccess(successful);
        when(checkoutComCheckoutFlowFacadeMock.authorizePayment()).thenReturn(authorizeResponseData);
    }

    private OrderWsDTO authorisePlaceOrderAndRedirectToResultPageReturnsADTO() throws PlaceOrderException,
        PaymentAuthorizationException {
        final OrderWsDTO orderWsDTO = new OrderWsDTO();

        doReturn(orderWsDTO).when(testObj).authorisePlaceOrderAndRedirectToResultPage();
        return orderWsDTO;
    }

    private void setPaymentInfoAchToCart(final AchBankInfoDetailsData achBankInfoDetailsData) throws IOException {
        doNothing().when(checkoutComACHFacadeMock).setPaymentInfoAchToCart(achBankInfoDetailsData);
    }

    private PlaidLinkCreationResponseDTO createPlaidLinkCreationResponseDTO() {
        final PlaidLinkCreationResponseDTO plaidLinkLinkCreationResponseDTO =
            new PlaidLinkCreationResponseDTO();
        plaidLinkLinkCreationResponseDTO.setPublicToken(PUBLIC_TOKEN);
        plaidLinkLinkCreationResponseDTO.setCustomerConsents(true);
        return plaidLinkLinkCreationResponseDTO;
    }

    private AchBankInfoDetailsData ensureBankAccountDetailsAreReturnedForPlaidLinkCreationResponseDTO(final PlaidLinkCreationResponseDTO plaidLinkLinkCreationResponseDTO) throws IOException {
        ensureDataMapperConvertsFromPlaidLinkCreationResponseDTOToPlaidLinkCreationResponse(
            plaidLinkLinkCreationResponseDTO, plaidLinkCreationResponse);
        final AchBankInfoDetailsData bankAccountDetailsData = new AchBankInfoDetailsData();
        when(checkoutComPlaidLinkFacadeMock.getBankAccountDetailsData(
            plaidLinkCreationResponse)).thenReturn(bankAccountDetailsData);

        return bankAccountDetailsData;
    }

    private void ensureDataMapperConvertsFromPlaidLinkCreationResponseDTOToPlaidLinkCreationResponse(final PlaidLinkCreationResponseDTO plaidLinkLinkCreationResponseDTO, final PlaidLinkCreationResponse plaidLinkCreationResponse) {
        when(dataMapperMock.map(plaidLinkLinkCreationResponseDTO, PlaidLinkCreationResponse.class)).thenReturn(
            plaidLinkCreationResponse);
    }

}
