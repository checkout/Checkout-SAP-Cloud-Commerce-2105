package com.checkout.hybris.occ.strategies;

import com.checkout.hybris.core.url.services.CheckoutComUrlService;
import com.checkout.payments.PaymentRequest;
import com.checkout.payments.RequestSource;
import de.hybris.bootstrap.annotations.UnitTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComOccAbstractPaymentRequestStrategyTest {

    private static final String CHECKOUT_COM_OCC_PAYMENT_REDIRECT_PAYMENT_SUCCESS = "/order-confirmation?authorized=true";
    private static final String CHECKOUT_COM_OCC_PAYMENT_REDIRECT_PAYMENT_FAILURE = "/order-confirmation?authorized=false";
    private static final String SITE_URL = "https://localhost:4200";
    private static final String CHECKOUT_COM_OCC_FULL_REDIRECT_SUCCESS = SITE_URL + CHECKOUT_COM_OCC_PAYMENT_REDIRECT_PAYMENT_SUCCESS;
    private static final String CHECKOUT_COM_OCC_FULL_REDIRECT_FAILURE = SITE_URL + CHECKOUT_COM_OCC_PAYMENT_REDIRECT_PAYMENT_FAILURE;

    @Spy
    private CheckoutComOccAbstractPaymentRequestStrategy testObj;

    @Mock
    private CheckoutComUrlService checkoutComUrlServiceMock;
    @Mock
    private PaymentRequest<RequestSource> requestMock;

    @Before
    public void setUp() {
        Whitebox.setInternalState(testObj, "checkoutComUrlService", checkoutComUrlServiceMock);
    }

    @Test
    public void populateRedirectUrls_ShouldSetOccRedirectUrls() {
        when(checkoutComUrlServiceMock.getFullUrl(CHECKOUT_COM_OCC_PAYMENT_REDIRECT_PAYMENT_SUCCESS, true)).thenReturn(CHECKOUT_COM_OCC_FULL_REDIRECT_SUCCESS);
        when(checkoutComUrlServiceMock.getFullUrl(CHECKOUT_COM_OCC_PAYMENT_REDIRECT_PAYMENT_FAILURE, true)).thenReturn(CHECKOUT_COM_OCC_FULL_REDIRECT_FAILURE);

        testObj.populateRedirectUrls(requestMock);

        verify(requestMock).setSuccessUrl(CHECKOUT_COM_OCC_FULL_REDIRECT_SUCCESS);
        verify(requestMock).setFailureUrl(CHECKOUT_COM_OCC_FULL_REDIRECT_FAILURE);
    }
}
