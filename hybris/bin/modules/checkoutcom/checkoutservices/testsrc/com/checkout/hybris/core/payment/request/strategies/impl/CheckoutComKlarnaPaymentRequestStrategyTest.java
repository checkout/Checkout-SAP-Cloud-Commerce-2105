package com.checkout.hybris.core.payment.request.strategies.impl;

import com.checkout.hybris.core.currency.services.CheckoutComCurrencyService;
import com.checkout.hybris.core.klarna.request.KlarnaAddressDto;
import com.checkout.hybris.core.klarna.session.request.KlarnaProductRequestDto;
import com.checkout.hybris.core.model.CheckoutComKlarnaAPMPaymentInfoModel;
import com.checkout.payments.AlternativePaymentSource;
import com.checkout.payments.PaymentRequest;
import com.checkout.payments.RequestSource;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.core.model.c2l.CountryModel;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.KLARNA;
import static com.checkout.hybris.core.payment.request.strategies.impl.CheckoutComKlarnaPaymentRequestStrategy.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComKlarnaPaymentRequestStrategyTest {

    private static final String CURRENCY_ISO_CODE = "USD";
    private static final String KLARNA_AUTH_TOKEN_VALUE = "klarna_token";
    private static final Long CHECKOUT_COM_TOTAL_PRICE = 10000L;
    private static final Long CHECKOUTCOM_AMOUNT_LONG = 12312L;
    private static final double CHECKOUTCOM_AMOUNT_DOUBLE = 123.12d;
    private static final String CURRENCY_CODE = "GBP";
    private static final String COUNTRY_CODE = "GB";
    private static final String LOCALE_CODE = Locale.UK.toString().replace("_", "-");
    private static final String EMAIL_VALUE = "email@test.com";
    private static final String CHECKOUTCOM_PAYMENT_REFERENCE_VALUE = "checkout_payment_ref";

    @InjectMocks
    private CheckoutComKlarnaPaymentRequestStrategy testObj;

    @Mock
    private CheckoutComCurrencyService checkoutComCurrencyServiceMock;
    @Mock
    private Converter<CartModel, List<KlarnaProductRequestDto>> checkoutComKlarnaProductsRequestDtoPopulatorMock;
    @Mock
    private CartModel cartModelMock;
    @Mock
    private CurrencyModel currencyModelMock;
    @Mock
    private AddressModel addressMock;
    @Mock
    private CountryModel countryModelMock;
    @Mock
    private CMSSiteModel cmsSiteMock;
    @Mock
    private CheckoutComKlarnaAPMPaymentInfoModel klarnaPaymentInfoMock;
    @Mock
    private AbstractOrderEntryModel entry1Mock, entry2Mock;
    @Mock
    private KlarnaProductRequestDto product1Mock, product2Mock;
    @Mock
    private CustomerModel customerMock;

    @Before
    public void setUp() {
        when(cartModelMock.getPaymentInfo()).thenReturn(klarnaPaymentInfoMock);
        when(cartModelMock.getUser()).thenReturn(customerMock);
        when(customerMock.getContactEmail()).thenReturn(EMAIL_VALUE);
        when(klarnaPaymentInfoMock.getType()).thenReturn(KLARNA.name());
        when(klarnaPaymentInfoMock.getAuthorizationToken()).thenReturn(KLARNA_AUTH_TOKEN_VALUE);
        when(klarnaPaymentInfoMock.getBillingAddress()).thenReturn(addressMock);
        when(cartModelMock.getDeliveryAddress()).thenReturn(addressMock);
        when(checkoutComCurrencyServiceMock.convertAmountIntoPennies(CURRENCY_CODE, CHECKOUTCOM_AMOUNT_DOUBLE)).thenReturn(CHECKOUTCOM_AMOUNT_LONG);
        when(cartModelMock.getTotalPrice()).thenReturn(CHECKOUTCOM_AMOUNT_DOUBLE);
        when(cartModelMock.getTotalTax()).thenReturn(CHECKOUTCOM_AMOUNT_DOUBLE);
        when(cartModelMock.getCurrency()).thenReturn(currencyModelMock);
        when(currencyModelMock.getIsocode()).thenReturn(CURRENCY_CODE);
        when(cartModelMock.getSite()).thenReturn(cmsSiteMock);
        when(cmsSiteMock.getLocale()).thenReturn(LOCALE_CODE);
        when(cartModelMock.getPaymentAddress()).thenReturn(addressMock);
        when(addressMock.getCountry()).thenReturn(countryModelMock);
        when(countryModelMock.getIsocode()).thenReturn(COUNTRY_CODE);
        when(cartModelMock.getEntries()).thenReturn(Arrays.asList(entry1Mock, entry2Mock));
        when(cartModelMock.getDeliveryCost()).thenReturn(CHECKOUTCOM_AMOUNT_DOUBLE);
        when(cartModelMock.getCheckoutComPaymentReference()).thenReturn(CHECKOUTCOM_PAYMENT_REFERENCE_VALUE);
        when(checkoutComKlarnaProductsRequestDtoPopulatorMock.convert(cartModelMock)).thenReturn(Arrays.asList(product1Mock, product2Mock));

    }

    @Test
    public void getRequestSourcePaymentRequest_WhenEverythingIsCorrect_ShouldPopulateTheRequest() {
        final PaymentRequest<RequestSource> result = testObj.getRequestSourcePaymentRequest(cartModelMock, CURRENCY_ISO_CODE, CHECKOUT_COM_TOTAL_PRICE);

        assertEquals(KLARNA.name().toLowerCase(), result.getSource().getType());
        final AlternativePaymentSource source = (AlternativePaymentSource) result.getSource();
        assertEquals(KLARNA_AUTH_TOKEN_VALUE, source.get(AUTHORIZATION_TOKEN_KEY));
        assertEquals(LOCALE_CODE, source.get(LOCALE_KEY));
        assertEquals(COUNTRY_CODE, source.get(PURCHASE_COUNTRY_KEY));
        assertEquals(CHECKOUTCOM_AMOUNT_LONG, source.get(TAX_AMOUNT_KEY));
        assertEquals(CHECKOUTCOM_PAYMENT_REFERENCE_VALUE, source.get(MERCHANT_REFERENCE_KEY));
        final KlarnaAddressDto billingAddressDto = (KlarnaAddressDto) source.get(BILLING_ADDRESS_KEY);
        final KlarnaAddressDto deliveryAddressDto = (KlarnaAddressDto) source.get(SHIPPING_ADDRESS_KEY);

        assertEquals(COUNTRY_CODE, billingAddressDto.getCountry());
        assertEquals(COUNTRY_CODE, deliveryAddressDto.getCountry());

        final List<KlarnaProductRequestDto> products = (List<KlarnaProductRequestDto>) source.get(PRODUCTS_KEY);
        verify(checkoutComKlarnaProductsRequestDtoPopulatorMock).convert(cartModelMock);
        assertEquals(2, products.size());
        assertEquals(product1Mock, products.get(0));
        assertEquals(product2Mock, products.get(1));
    }

    @Test
    public void isCapture_WhenKlarna_ShouldReturnFalse() {
        assertFalse(testObj.isCapture().get());
    }

    @Test
    public void getStrategyKey_WhenKlarna_ShouldReturnKlarnaType() {
        assertEquals(KLARNA, testObj.getStrategyKey());
    }
}