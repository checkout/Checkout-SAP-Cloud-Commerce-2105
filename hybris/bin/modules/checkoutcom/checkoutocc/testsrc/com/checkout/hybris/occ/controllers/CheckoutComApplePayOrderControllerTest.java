package com.checkout.hybris.occ.controllers;

import com.checkout.dto.order.ApplePayValidateMerchantRequestWsDTO;
import com.checkout.dto.order.PlaceWalletOrderWsDTO;
import com.checkout.hybris.facades.address.CheckoutComWalletAddressFacade;
import com.checkout.hybris.facades.beans.*;
import com.checkout.hybris.facades.payment.wallet.CheckoutComApplePayFacade;
import com.checkout.hybris.facades.payment.wallet.CheckoutComWalletOrderFacade;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.webservicescommons.mapping.DataMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.checkout.hybris.facades.enums.WalletPaymentType.APPLEPAY;
import static de.hybris.platform.webservicescommons.mapping.FieldSetLevelHelper.DEFAULT_LEVEL;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComApplePayOrderControllerTest {

    public static final String VALIDATION_URL = "validationURL";
    @InjectMocks
    private CheckoutComApplePayOrderController testObj;

    @Mock
    private CheckoutComWalletOrderFacade checkoutComWalletOrderFacadeMock;
    @Mock
    private CheckoutComWalletAddressFacade checkoutComWalletAddressFacadeMock;
    @Mock
    private CheckoutComApplePayFacade checkoutComApplePayFacadeMock;
    @Mock
    private DataMapper dataMapperMock;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ApplePayAuthorisationRequest authorisationRequestMock;
    @Mock
    private ApplePayPaymentContact paymentContactMock;
    @Mock
    private ApplePayAdditionalAuthInfo paymentDataMock;
    @Mock
    private PlaceWalletOrderDataResponse placeWalletOrderDataResponseMock;

    private ApplePayValidateMerchantRequestWsDTO validateMerchantRequestWsDTOStub = new ApplePayValidateMerchantRequestWsDTO();

    @Test
    public void placeOrder_ShouldPlaceOrderWithApplePay() {
        doNothing().when(checkoutComWalletAddressFacadeMock).handleAndSaveAddresses(paymentContactMock);
        when(authorisationRequestMock.getBillingContact()).thenReturn(paymentContactMock);
        when(authorisationRequestMock.getToken().getPaymentData()).thenReturn(paymentDataMock);
        when(checkoutComWalletOrderFacadeMock.placeWalletOrder(paymentDataMock, APPLEPAY)).thenReturn(placeWalletOrderDataResponseMock);

        testObj.placeOrder(authorisationRequestMock, DEFAULT_LEVEL);

        verify(dataMapperMock).map(placeWalletOrderDataResponseMock, PlaceWalletOrderWsDTO.class, DEFAULT_LEVEL);
    }

    @Test
    public void requestPaymentSession_ShouldCallWalletOrderFacade() {
        validateMerchantRequestWsDTOStub.setValidationURL(VALIDATION_URL);

        testObj.requestPaymentSession(validateMerchantRequestWsDTOStub);

        verify(checkoutComApplePayFacadeMock).requestApplePayPaymentSession(any(ApplePayValidateMerchantRequestData.class));
    }

    @Test
    public void getPaymentRequest_WhenApplePaySettingPresent_ShouldReturnThem() {
        testObj.getPaymentRequest();

        verify(checkoutComApplePayFacadeMock).getApplePayPaymentRequest();
    }

}
