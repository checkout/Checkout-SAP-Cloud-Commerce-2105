package com.checkout.hybris.addon.controllers.payment;

import com.checkout.hybris.facades.address.CheckoutComWalletAddressFacade;
import com.checkout.hybris.facades.beans.ApplePayAdditionalAuthInfo;
import com.checkout.hybris.facades.beans.ApplePayAuthorisationRequest;
import com.checkout.hybris.facades.beans.ApplePayPaymentContact;
import com.checkout.hybris.facades.beans.ApplePayValidateMerchantRequestData;
import com.checkout.hybris.facades.enums.WalletPaymentType;
import com.checkout.hybris.facades.payment.wallet.CheckoutComApplePayFacade;
import com.checkout.hybris.facades.payment.wallet.CheckoutComWalletOrderFacade;
import de.hybris.bootstrap.annotations.UnitTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComApplePayControllerTest {

    @InjectMocks
    private CheckoutComApplePayController testObj;

    @Mock
    private CheckoutComApplePayFacade checkoutComApplePayFacadeMock;
    @Mock
    private CheckoutComWalletOrderFacade checkoutComWalletOrderFacadeMock;
    @Mock
    private CheckoutComWalletAddressFacade checkoutComWalletAddressFacadeMock;

    @Mock
    private ApplePayValidateMerchantRequestData merchantDataMock;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ApplePayAuthorisationRequest applePayRequestMock;
    @Mock
    private ApplePayAdditionalAuthInfo applePayAdditionalAuthMock;
    @Mock
    private ApplePayPaymentContact billingContactMock;

    @Test
    public void requestPaymentSession_ShouldCallFacade() {
        testObj.requestPaymentSession(merchantDataMock);

        verify(checkoutComApplePayFacadeMock).requestApplePayPaymentSession(merchantDataMock);
    }

    @Test
    public void placeApplePayOrder_ShouldPlaceOrder() {
        when(applePayRequestMock.getToken().getPaymentData()).thenReturn(applePayAdditionalAuthMock);
        when(applePayRequestMock.getBillingContact()).thenReturn(billingContactMock);

        testObj.placeApplePayOrder(applePayRequestMock);

        verify(checkoutComWalletAddressFacadeMock).handleAndSaveAddresses(billingContactMock);
        verify(checkoutComWalletOrderFacadeMock).placeWalletOrder(applePayAdditionalAuthMock, WalletPaymentType.APPLEPAY);
    }
}
