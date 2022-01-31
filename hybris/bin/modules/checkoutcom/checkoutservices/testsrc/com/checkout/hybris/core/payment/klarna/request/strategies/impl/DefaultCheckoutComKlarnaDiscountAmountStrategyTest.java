package com.checkout.hybris.core.payment.klarna.request.strategies.impl;

import com.checkout.hybris.core.currency.services.CheckoutComCurrencyService;
import com.checkout.hybris.core.klarna.session.request.KlarnaProductRequestDto;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.CartModel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultCheckoutComKlarnaDiscountAmountStrategyTest {

    private static final String CURRENCY_CODE = "GBP";
    private static final double TOTAL_ORDER_DISCOUNT = 100d;
    private static final long TOTAL_DISCOUNT_PENNIES = 10000l;

    @InjectMocks
    private DefaultCheckoutComKlarnaDiscountAmountStrategy testObj;

    @Mock
    private CheckoutComCurrencyService checkoutComCurrencyServiceMock;
    @Mock
    private CurrencyModel currencyMock;
    @Mock
    private CartModel cartMock;
    @Mock
    private AbstractOrderEntryModel entry1Mock, entry2Mock, entry3Mock, entry4Mock, entry5Mock, entry6Mock;
    @Mock
    private KlarnaProductRequestDto product1DtoMock, product2DtoMock, product3DtoMock, product4DtoMock, product5DtoMock, product6DtoMock;

    @Before
    public void setUp() {
        when(cartMock.getCurrency()).thenReturn(currencyMock);
        when(currencyMock.getIsocode()).thenReturn(CURRENCY_CODE);
        when(cartMock.getEntries()).thenReturn(Arrays.asList(entry1Mock, entry2Mock, entry3Mock, entry4Mock, entry5Mock, entry6Mock));
        when(checkoutComCurrencyServiceMock.convertAmountIntoPennies(CURRENCY_CODE, TOTAL_ORDER_DISCOUNT)).thenReturn(TOTAL_DISCOUNT_PENNIES);
        when(cartMock.getTotalDiscounts()).thenReturn(TOTAL_ORDER_DISCOUNT);
    }

    @Test
    public void applyDiscountsToKlarnaOrderLines_ShouldSplitTheDiscountCorrectly() {
        testObj.applyDiscountsToKlarnaOrderLines(cartMock, Arrays.asList(product1DtoMock, product2DtoMock, product3DtoMock, product4DtoMock, product5DtoMock, product6DtoMock));

        verify(checkoutComCurrencyServiceMock).convertAmountIntoPennies(CURRENCY_CODE, TOTAL_ORDER_DISCOUNT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void populate_WhenCartNull_ShouldThrowException() {
        testObj.applyDiscountsToKlarnaOrderLines(null, Arrays.asList(product1DtoMock, product2DtoMock, product3DtoMock, product4DtoMock, product5DtoMock, product6DtoMock));
    }

    @Test(expected = IllegalArgumentException.class)
    public void populate_WhenOrderLinesEmpty_ShouldThrowException() {
        testObj.applyDiscountsToKlarnaOrderLines(cartMock, Collections.emptyList());
    }

    @Test(expected = IllegalArgumentException.class)
    public void populate_WhenCartEntriesEmpty_ShouldThrowException() {
        when(cartMock.getEntries()).thenReturn(Collections.emptyList());

        testObj.applyDiscountsToKlarnaOrderLines(cartMock, Arrays.asList(product1DtoMock, product2DtoMock, product3DtoMock, product4DtoMock, product5DtoMock, product6DtoMock));
    }
}