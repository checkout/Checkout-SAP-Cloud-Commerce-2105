package com.checkout.hybris.core.payment.request.strategies.impl;

import com.checkout.hybris.core.model.CheckoutComAPMPaymentInfoModel;
import com.checkout.payments.PaymentRequest;
import com.checkout.payments.RequestSource;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.c2l.CountryModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.AddressModel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.GIROPAY;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComGiropayPaymentRequestStrategyTest {

    private static final String CURRENCY_ISO_CODE = "USD";
    private static final Long CHECKOUT_COM_TOTAL_PRICE = 10000L;
    private static final String CITY_MOCK = "city";
    private static final String ZIP_MOCK = "zip";
    private static final String COUNTRY_ISO_CODE_MOCK = "en";
    private static final String PAYMENT_REFERENCE_VALUE = "payment-reference";
    private static final String PAYMENT_REFERENCE_VALUE_TOO_LONG = "string-with-more-than-27-characters";

    @InjectMocks
    private CheckoutComGiropayPaymentRequestStrategy testObj;

    @Mock
    private CartModel cartMock;
    @Mock
    private AddressModel deliveryAddressMock;
    @Mock
    private CountryModel countryMock;
    @Mock
    private CheckoutComAPMPaymentInfoModel checkoutComAPMPaymentInfoMock;

    @Before
    public void setUp() {
        when(cartMock.getPaymentInfo()).thenReturn(checkoutComAPMPaymentInfoMock);
        when(cartMock.getCheckoutComPaymentReference()).thenReturn(PAYMENT_REFERENCE_VALUE);
        when(cartMock.getDeliveryAddress()).thenReturn(deliveryAddressMock);
        when(checkoutComAPMPaymentInfoMock.getType()).thenReturn(GIROPAY.name());
        when(deliveryAddressMock.getTown()).thenReturn(CITY_MOCK);
        when(deliveryAddressMock.getPostalcode()).thenReturn(ZIP_MOCK);
        when(deliveryAddressMock.getCountry()).thenReturn(countryMock);
        when(countryMock.getIsocode()).thenReturn(COUNTRY_ISO_CODE_MOCK);
    }

    @Test
    public void getStrategyKey_WhenGiropay_ShouldReturnGiropayType() {
        assertEquals(GIROPAY, testObj.getStrategyKey());
    }

    @Test
    public void getRequestSourcePaymentRequest_WhenGiropayPayment_ShouldCreateAlternativePaymentRequestWithTypeAndValues() {
        final PaymentRequest<RequestSource> result = testObj.getRequestSourcePaymentRequest(cartMock, CURRENCY_ISO_CODE, CHECKOUT_COM_TOTAL_PRICE);

        assertEquals(GIROPAY.name().toLowerCase(), result.getSource().getType());
        assertEquals(PAYMENT_REFERENCE_VALUE, result.getDescription());
        assertEquals(CITY_MOCK, result.getShipping().getAddress().getCity());
        assertEquals(ZIP_MOCK, result.getShipping().getAddress().getZip());
        assertEquals(COUNTRY_ISO_CODE_MOCK, result.getShipping().getAddress().getCountry());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getRequestSourcePaymentRequest_WhenGiropayPaymentButPaymentReferenceIsBlank_ShouldThrowException() {
        when(cartMock.getCheckoutComPaymentReference()).thenReturn("");

        testObj.getRequestSourcePaymentRequest(cartMock, CURRENCY_ISO_CODE, CHECKOUT_COM_TOTAL_PRICE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getRequestSourcePaymentRequest_WhenGiropayPaymentButPaymentReferenceIsTooLong_ShouldThrowException() {
        when(cartMock.getCheckoutComPaymentReference()).thenReturn(PAYMENT_REFERENCE_VALUE_TOO_LONG);
        final PaymentRequest<RequestSource> result = testObj.getRequestSourcePaymentRequest(cartMock, CURRENCY_ISO_CODE, CHECKOUT_COM_TOTAL_PRICE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getRequestSourcePaymentRequest_WhenGiropayPaymentButCityIsBlank_ShouldThrowException() {
        when(deliveryAddressMock.getTown()).thenReturn("");
        final PaymentRequest<RequestSource> result = testObj.getRequestSourcePaymentRequest(cartMock, CURRENCY_ISO_CODE, CHECKOUT_COM_TOTAL_PRICE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getRequestSourcePaymentRequest_WhenGiropayPaymentButZipIsBlank_ShouldThrowException() {
        when(deliveryAddressMock.getPostalcode()).thenReturn("");
        final PaymentRequest<RequestSource> result = testObj.getRequestSourcePaymentRequest(cartMock, CURRENCY_ISO_CODE, CHECKOUT_COM_TOTAL_PRICE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getRequestSourcePaymentRequest_WhenGiropayPaymentButCountryISOCodeIsBlank_ShouldThrowException() {
        when(countryMock.getIsocode()).thenReturn("");
        final PaymentRequest<RequestSource> result = testObj.getRequestSourcePaymentRequest(cartMock, CURRENCY_ISO_CODE, CHECKOUT_COM_TOTAL_PRICE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getRequestSourcePaymentRequest_WhenGiropayPaymentButCountryISOCodeIsTooLong_ShouldThrowException() {
        when(countryMock.getIsocode()).thenReturn("abc");
        final PaymentRequest<RequestSource> result = testObj.getRequestSourcePaymentRequest(cartMock, CURRENCY_ISO_CODE, CHECKOUT_COM_TOTAL_PRICE);
    }
}
