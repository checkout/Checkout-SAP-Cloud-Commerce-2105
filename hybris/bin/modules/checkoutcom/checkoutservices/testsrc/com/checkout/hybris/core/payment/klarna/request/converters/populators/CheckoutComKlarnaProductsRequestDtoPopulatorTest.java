package com.checkout.hybris.core.payment.klarna.request.converters.populators;

import com.checkout.hybris.core.currency.services.CheckoutComCurrencyService;
import com.checkout.hybris.core.klarna.session.request.KlarnaProductRequestDto;
import com.checkout.hybris.core.payment.klarna.request.strategies.CheckoutComKlarnaDiscountAmountStrategy;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.delivery.DeliveryModeModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.util.TaxValue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComKlarnaProductsRequestDtoPopulatorTest {

    private static final Long CHECKOUTCOM_AMOUNT_LONG = 12312L;
    private static final double CHECKOUTCOM_AMOUNT_DOUBLE = 123.12d;
    private static final String CURRENCY_CODE = "GBP";
    private static final String PRODUCT_NAME = "product name";
    private static final String DELIVERY_NAME = "Delivery name";

    @InjectMocks
    private CheckoutComKlarnaProductsRequestDtoPopulator testObj;

    @Mock
    private CheckoutComCurrencyService checkoutComCurrencyServiceMock;
    @Mock
    private CheckoutComKlarnaDiscountAmountStrategy checkoutComKlarnaDiscountAmountStrategyMock;
    @Mock
    private CartModel sourceMock;
    @Mock
    private CurrencyModel currencyModelMock;
    @Mock
    private AbstractOrderEntryModel entry1Mock, entry2Mock;
    @Mock
    private DeliveryModeModel deliveryModeMock;
    @Mock
    private ProductModel productModelMock;
    @Mock
    private TaxValue taxValueMock;
    @Mock
    private KlarnaProductRequestDto product1Mock, product2Mock;

    private List<KlarnaProductRequestDto> target = new ArrayList<>();

    @Before
    public void setUp() {
        when(sourceMock.getCurrency()).thenReturn(currencyModelMock);
        when(currencyModelMock.getIsocode()).thenReturn(CURRENCY_CODE);
        when(checkoutComCurrencyServiceMock.convertAmountIntoPennies(CURRENCY_CODE, CHECKOUTCOM_AMOUNT_DOUBLE)).thenReturn(CHECKOUTCOM_AMOUNT_LONG);
        when(sourceMock.getTotalTax()).thenReturn(CHECKOUTCOM_AMOUNT_DOUBLE);
        when(sourceMock.getEntries()).thenReturn(Arrays.asList(entry1Mock, entry2Mock));
        when(entry1Mock.getBasePrice()).thenReturn(CHECKOUTCOM_AMOUNT_DOUBLE);
        when(entry2Mock.getBasePrice()).thenReturn(CHECKOUTCOM_AMOUNT_DOUBLE);
        when(entry1Mock.getQuantity()).thenReturn(1l);
        when(entry2Mock.getQuantity()).thenReturn(1l);
        when(entry1Mock.getTotalPrice()).thenReturn(CHECKOUTCOM_AMOUNT_DOUBLE);
        when(entry2Mock.getTotalPrice()).thenReturn(CHECKOUTCOM_AMOUNT_DOUBLE);
        when(entry2Mock.getTaxValues()).thenReturn(Collections.singletonList(taxValueMock));
        when(taxValueMock.getValue()).thenReturn(CHECKOUTCOM_AMOUNT_DOUBLE);
        when(taxValueMock.getAppliedValue()).thenReturn(CHECKOUTCOM_AMOUNT_DOUBLE);
        when(entry1Mock.getProduct()).thenReturn(productModelMock);
        when(entry2Mock.getProduct()).thenReturn(productModelMock);
        when(productModelMock.getName()).thenReturn(PRODUCT_NAME);
        when(sourceMock.getDeliveryCost()).thenReturn(CHECKOUTCOM_AMOUNT_DOUBLE);
        when(sourceMock.getDeliveryMode()).thenReturn(deliveryModeMock);
        when(deliveryModeMock.getName()).thenReturn(DELIVERY_NAME);
        doNothing().when(checkoutComKlarnaDiscountAmountStrategyMock).applyDiscountsToKlarnaOrderLines(sourceMock, Arrays.asList(product1Mock, product2Mock));
    }

    @Test
    public void populate_ShouldPopulateTheProductsProperly() {
        testObj.populate(sourceMock, target);

        assertEquals(PRODUCT_NAME, target.get(0).getName());
        assertEquals(Long.valueOf(1), target.get(0).getQuantity());
        assertEquals(Long.valueOf(0), target.get(0).getTaxRate());
        assertEquals(Long.valueOf(0), target.get(0).getTotalTaxAmount());
        assertEquals(CHECKOUTCOM_AMOUNT_LONG, target.get(0).getUnitPrice());
        assertEquals(CHECKOUTCOM_AMOUNT_LONG, target.get(0).getTotalAmount());

        assertEquals(PRODUCT_NAME, target.get(1).getName());
        assertEquals(Long.valueOf(1), target.get(1).getQuantity());
        assertEquals(CHECKOUTCOM_AMOUNT_LONG, target.get(1).getTaxRate());
        assertEquals(CHECKOUTCOM_AMOUNT_LONG, target.get(1).getTotalTaxAmount());
        assertEquals(CHECKOUTCOM_AMOUNT_LONG, target.get(1).getUnitPrice());
        assertEquals(CHECKOUTCOM_AMOUNT_LONG, target.get(1).getTotalAmount());

        assertEquals(DELIVERY_NAME, target.get(2).getName());
        assertEquals(Long.valueOf(1), target.get(2).getQuantity());
        assertEquals(Long.valueOf(0), target.get(2).getTaxRate());
        assertEquals(Long.valueOf(0), target.get(2).getTotalTaxAmount());
        assertEquals(CHECKOUTCOM_AMOUNT_LONG, target.get(2).getUnitPrice());
        assertEquals(CHECKOUTCOM_AMOUNT_LONG, target.get(2).getTotalAmount());

        verify(checkoutComKlarnaDiscountAmountStrategyMock).applyDiscountsToKlarnaOrderLines(sourceMock, target);
    }

    @Test
    public void populateShippingLine_ShouldDoNothing_WhenShippingAmountIsZero() {
        final List<KlarnaProductRequestDto> productRequestDtos = new ArrayList<>();

        when(sourceMock.getDeliveryCost()).thenReturn(0d);

        testObj.populateShippingLine(sourceMock, CURRENCY_CODE, productRequestDtos, 0);

        assertThat(productRequestDtos).isEmpty();
    }

    @Test
    public void populateShippingLine_ShouldNotSetTaxRate_WhenTaxIsZero() {
        final List<KlarnaProductRequestDto> productRequestDtos = new ArrayList<>();

        when(checkoutComCurrencyServiceMock.convertAmountIntoPennies(CURRENCY_CODE, CHECKOUTCOM_AMOUNT_DOUBLE))
                .thenReturn(50L)
                .thenReturn(50L);

        testObj.populateShippingLine(sourceMock, CURRENCY_CODE, productRequestDtos, 0);

        assertThat(productRequestDtos).hasSize(1);

        final KlarnaProductRequestDto shippingRequestDto = productRequestDtos.get(0);

        assertThat(shippingRequestDto.getTaxRate()).isZero();
    }

    @Test
    public void populateShippingLine_ShouldNotSetTaxRateIfTax_WhenLessThanZero() {
        final List<KlarnaProductRequestDto> productRequestDtos = new ArrayList<>();

        when(checkoutComCurrencyServiceMock.convertAmountIntoPennies(CURRENCY_CODE, CHECKOUTCOM_AMOUNT_DOUBLE))
                .thenReturn(200L)
                .thenReturn(50L);

        testObj.populateShippingLine(sourceMock, CURRENCY_CODE, productRequestDtos, 0);

        assertThat(productRequestDtos).hasSize(1);

        final KlarnaProductRequestDto shippingRequestDto = productRequestDtos.get(0);

        assertThat(shippingRequestDto.getTaxRate()).isZero();
    }

    @Test
    public void populateShippingLine_ShouldSetTaxRate_WhenIfTaxIsGreaterThanZero() {
        final List<KlarnaProductRequestDto> productRequestDtos = new ArrayList<>();

        when(checkoutComCurrencyServiceMock.convertAmountIntoPennies(CURRENCY_CODE, CHECKOUTCOM_AMOUNT_DOUBLE))
                .thenReturn(50L)
                .thenReturn(200L);

        testObj.populateShippingLine(sourceMock, CURRENCY_CODE, productRequestDtos, 0);

        assertThat(productRequestDtos).hasSize(1);

        final KlarnaProductRequestDto shippingRequestDto = productRequestDtos.get(0);

        assertThat(shippingRequestDto.getTaxRate()).isNotZero();
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
