package com.checkout.hybris.facades.payment.clienttoken.request.converters.populators;

import com.checkout.hybris.core.currency.services.CheckoutComCurrencyService;
import com.checkout.hybris.core.klarna.session.request.KlarnaProductRequestDto;
import com.checkout.hybris.core.klarna.session.request.KlarnaSessionRequestDto;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.core.model.c2l.CountryModel;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.AddressModel;
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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComKlarnaSessionRequestDtoPopulatorTest {

    private static final Long CHECKOUTCOM_AMOUNT_LONG = 12312L;
    private static final double CHECKOUTCOM_AMOUNT_DOUBLE = 123.12d;
    private static final String CURRENCY_CODE = "GBP";
    private static final String COUNTRY_CODE = "GB";
    private static final String LOCALE_CODE = Locale.UK.toString().replace("_", "-");

    @InjectMocks
    private CheckoutComKlarnaSessionRequestDtoPopulator testObj;

    @Mock
    private CheckoutComCurrencyService checkoutComCurrencyServiceMock;
    @Mock
    private Converter<CartModel, List<KlarnaProductRequestDto>> checkoutComKlarnaProductsRequestDtoConverterMock;
    @Mock
    private CartModel sourceMock;
    @Mock
    private CurrencyModel currencyModelMock;
    @Mock
    private AddressModel billingAddressMock;
    @Mock
    private CountryModel countryModelMock;
    @Mock
    private CMSSiteModel cmsSiteMock;
    @Mock
    private AbstractOrderEntryModel entry1Mock, entry2Mock;
    @Mock
    private KlarnaProductRequestDto product1Mock, product2Mock;

    private KlarnaSessionRequestDto target = new KlarnaSessionRequestDto();

    @Before
    public void setUp() {
        when(checkoutComCurrencyServiceMock.convertAmountIntoPennies(CURRENCY_CODE, CHECKOUTCOM_AMOUNT_DOUBLE)).thenReturn(CHECKOUTCOM_AMOUNT_LONG);
        when(sourceMock.getTotalPrice()).thenReturn(CHECKOUTCOM_AMOUNT_DOUBLE);
        when(sourceMock.getTotalTax()).thenReturn(CHECKOUTCOM_AMOUNT_DOUBLE);
        when(sourceMock.getCurrency()).thenReturn(currencyModelMock);
        when(currencyModelMock.getIsocode()).thenReturn(CURRENCY_CODE);
        when(sourceMock.getSite()).thenReturn(cmsSiteMock);
        when(cmsSiteMock.getLocale()).thenReturn(LOCALE_CODE);
        when(sourceMock.getPaymentAddress()).thenReturn(billingAddressMock);
        when(billingAddressMock.getCountry()).thenReturn(countryModelMock);
        when(countryModelMock.getIsocode()).thenReturn(COUNTRY_CODE);
        when(sourceMock.getEntries()).thenReturn(Arrays.asList(entry1Mock, entry2Mock));
        when(checkoutComKlarnaProductsRequestDtoConverterMock.convert(sourceMock)).thenReturn(Arrays.asList(product1Mock, product2Mock));
    }

    @Test
    public void populate_WhenEverythingIsFine_ShouldPopulateTheRequest() {
        testObj.populate(sourceMock, target);

        assertEquals(CHECKOUTCOM_AMOUNT_LONG, target.getAmount());
        assertEquals(CHECKOUTCOM_AMOUNT_LONG, target.getTaxAmount());
        assertEquals(CURRENCY_CODE, target.getCurrency());
        assertEquals(COUNTRY_CODE, target.getPurchaseCountry());
        assertEquals(LOCALE_CODE, target.getLocale());
        verify(checkoutComKlarnaProductsRequestDtoConverterMock).convert(sourceMock);
        assertEquals(2, target.getProducts().size());
        assertEquals(product1Mock, target.getProducts().get(0));
        assertEquals(product2Mock, target.getProducts().get(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void populate_WhenSourceNull_ShouldThrowException() {
        testObj.populate(null, target);
    }

    @Test(expected = IllegalArgumentException.class)
    public void populate_WhenTargetNull_ShouldThrowException() {
        testObj.populate(sourceMock, null);
    }
}