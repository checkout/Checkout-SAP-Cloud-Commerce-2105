package com.checkout.hybris.core.payment.request.strategies.impl;

import com.checkout.hybris.core.address.services.CheckoutComAddressService;
import com.checkout.hybris.core.model.CheckoutComAPMPaymentInfoModel;
import com.checkout.payments.AlternativePaymentSource;
import com.checkout.payments.PaymentRequest;
import com.checkout.payments.RequestSource;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.BANCONTACT;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComBancontactPaymentRequestStrategyTest {

    private static final String CURRENCY_ISO_CODE = "USD";
    private static final Long CHECKOUT_COM_TOTAL_PRICE = 10000L;
    private static final String PAYMENT_COUNTRY_KEY = "payment_country";
    private static final String ACCOUNT_HOLDER_NAME_KEY = "account_holder_name";
    private static final String TITLE_NAME = "Mrs";
    private static final String CUSTOMER_NAME = "Mr John Snow";
    private static final String COUNTRY_CODE = "PT";

    @InjectMocks
    private CheckoutComBancontactPaymentRequestStrategy testObj;

    @Mock
    private CartModel cartMock;
    @Mock
    private CheckoutComAPMPaymentInfoModel checkoutComAPMPaymentInfoMock;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private CustomerModel customerMock;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private AddressModel addressMock;
    @Mock
    private CheckoutComAddressService addressServiceMock;


    @Before
    public void setUp() {
        when(cartMock.getPaymentInfo()).thenReturn(checkoutComAPMPaymentInfoMock);
        when(checkoutComAPMPaymentInfoMock.getBillingAddress()).thenReturn(addressMock);
        when(addressMock.getCountry().getIsocode()).thenReturn(COUNTRY_CODE);
        when(checkoutComAPMPaymentInfoMock.getType()).thenReturn(BANCONTACT.name());
        when(addressServiceMock.getCustomerFullNameFromAddress(addressMock)).thenReturn(CUSTOMER_NAME);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getRequestSourcePaymentRequest_WhenBancontactPaymentButBillingAddressIsNull_ShouldThrowException() {
        when(checkoutComAPMPaymentInfoMock.getBillingAddress()).thenReturn(null);

        testObj.getRequestSourcePaymentRequest(cartMock, CURRENCY_ISO_CODE, CHECKOUT_COM_TOTAL_PRICE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getRequestSourcePaymentRequest_WhenBancontactPaymentButCountryIsNull_ShouldThrowException() {
        when(addressMock.getCountry().getIsocode()).thenReturn(null);

        testObj.getRequestSourcePaymentRequest(cartMock, CURRENCY_ISO_CODE, CHECKOUT_COM_TOTAL_PRICE);
    }

    @Test
    public void getRequestSourcePaymentRequest_WhenBancontactPayment_ShouldCreateAlternativePaymentRequestWithTypeAndAdditionalInfo() {
        final PaymentRequest<RequestSource> result = testObj.getRequestSourcePaymentRequest(cartMock, CURRENCY_ISO_CODE, CHECKOUT_COM_TOTAL_PRICE);

        assertEquals(BANCONTACT.name().toLowerCase(), result.getSource().getType());
        assertEquals(COUNTRY_CODE, ((AlternativePaymentSource) result.getSource()).get(PAYMENT_COUNTRY_KEY));
        assertEquals(CUSTOMER_NAME, ((AlternativePaymentSource) result.getSource()).get(ACCOUNT_HOLDER_NAME_KEY));
    }

    @Test
    public void getStrategyKey_WhenBancontact_ShouldReturnBancontactType() {
        assertEquals(BANCONTACT, testObj.getStrategyKey());
    }
}
