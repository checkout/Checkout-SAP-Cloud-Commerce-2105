package com.checkout.hybris.occ.controllers;

import com.checkout.dto.order.PlaceWalletOrderWsDTO;
import com.checkout.hybris.facades.address.CheckoutComWalletAddressFacade;
import com.checkout.hybris.facades.beans.GooglePayAuthorisationRequest;
import com.checkout.hybris.facades.beans.GooglePayPaymentContact;
import com.checkout.hybris.facades.beans.GooglePayPaymentToken;
import com.checkout.hybris.facades.beans.PlaceWalletOrderDataResponse;
import com.checkout.hybris.facades.payment.google.CheckoutComGooglePayFacade;
import com.checkout.hybris.facades.payment.wallet.CheckoutComWalletOrderFacade;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.webservicescommons.mapping.DataMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.checkout.hybris.facades.enums.WalletPaymentType.GOOGLEPAY;
import static de.hybris.platform.webservicescommons.mapping.FieldSetLevelHelper.DEFAULT_LEVEL;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComGooglePayOrderControllerTest {

    @InjectMocks
    private CheckoutComGooglePayOrderController testObj;

    @Mock
    private CheckoutComWalletOrderFacade checkoutComWalletOrderFacadeMock;
    @Mock
    private CheckoutComWalletAddressFacade checkoutComWalletAddressFacadeMock;
    @Mock
    private DataMapper dataMapperMock;
    @Mock
    private CheckoutComGooglePayFacade checkoutComGooglePayFacadeMock;

    @Mock
    private GooglePayAuthorisationRequest authorisationRequestMock;
    @Mock
    private GooglePayPaymentContact paymentContactMock;
    @Mock
    private GooglePayPaymentToken paymentTokenMock;
    @Mock
    private PlaceWalletOrderDataResponse placeWalletOrderDataResponseMock;

    @Test
    public void getPaymentRequest_ShouldCallCheckoutComGooglePayFacade() {
        testObj.getMerchantConfiguration();

        verify(checkoutComGooglePayFacadeMock).getGooglePayMerchantConfiguration();
    }

    @Test
    public void placeOrder_ShouldPlaceOrderWithGooglePay() {
        doNothing().when(checkoutComWalletAddressFacadeMock).handleAndSaveAddresses(paymentContactMock);
        when(authorisationRequestMock.getBillingAddress()).thenReturn(paymentContactMock);
        when(authorisationRequestMock.getToken()).thenReturn(paymentTokenMock);
        when(checkoutComWalletOrderFacadeMock.placeWalletOrder(paymentTokenMock, GOOGLEPAY)).thenReturn(placeWalletOrderDataResponseMock);

        testObj.placeOrder(authorisationRequestMock, DEFAULT_LEVEL);

        verify(dataMapperMock).map(placeWalletOrderDataResponseMock, PlaceWalletOrderWsDTO.class, DEFAULT_LEVEL);
    }
}
