package com.checkout.hybris.addon.controllers.payment;

import com.checkout.hybris.facades.address.CheckoutComWalletAddressFacade;
import com.checkout.hybris.facades.beans.GooglePayAuthorisationRequest;
import com.checkout.hybris.facades.beans.GooglePayPaymentContact;
import com.checkout.hybris.facades.beans.GooglePayPaymentToken;
import com.checkout.hybris.facades.enums.WalletPaymentType;
import com.checkout.hybris.facades.payment.wallet.CheckoutComWalletOrderFacade;
import de.hybris.bootstrap.annotations.UnitTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComGooglePayControllerTest {

    @InjectMocks
    private CheckoutComGooglePayController testObj;

    @Mock
    private CheckoutComWalletOrderFacade checkoutComWalletOrderFacadeMock;
    @Mock
    private CheckoutComWalletAddressFacade checkoutComWalletAddressFacadeMock;

    @Mock
    private GooglePayAuthorisationRequest googlePayRequestMock;
    @Mock
    private GooglePayPaymentToken googlePayPaymentTokenMock;
    @Mock
    private GooglePayPaymentContact billingContactMock;

    @Test
    public void authoriseOrder_ShouldPlaceOrder() {
        when(googlePayRequestMock.getToken()).thenReturn(googlePayPaymentTokenMock);
        when(googlePayRequestMock.getBillingAddress()).thenReturn(billingContactMock);

        testObj.authoriseOrder(googlePayRequestMock);

        verify(checkoutComWalletAddressFacadeMock).handleAndSaveAddresses(billingContactMock);
        verify(checkoutComWalletOrderFacadeMock).placeWalletOrder(googlePayPaymentTokenMock, WalletPaymentType.GOOGLEPAY);
    }
}
