package com.checkout.hybris.core.payment.klarna.request.converters.populators;

import com.checkout.hybris.core.currency.services.CheckoutComCurrencyService;
import com.checkout.hybris.core.klarna.session.request.KlarnaProductRequestDto;
import com.checkout.hybris.core.payment.klarna.request.strategies.CheckoutComKlarnaDiscountAmountStrategy;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.delivery.DeliveryModeModel;
import de.hybris.platform.core.model.order.price.DiscountModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.util.TaxValue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
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
    private static final Long CHECKOUTCOM_DISCOUNT_AMOUNT_LONG = 1000L;
    private static final Long CHECKOUTCOM_CONVERTED_DISCOUNT_AMOUNT_LONG = -1000L;
    private static final double CHECKOUTCOM_DISCOUNT_AMOUNT_DOUBLE = 10.00d;
    private static final double CHECKOUTCOM_TOTAL_PRICE_AMOUNT_DOUBLE = 800.00d;
    private static final Long CHECKOUTCOM_TOTAL_PRICE_AMOUNT_LONG = 80000L;
    private static final double CHECKOUTCOM_TAX_RATE_DOUBLE = 0.1539d;
    private static final double CHECKOUTCOM_PAYMENT_COST_AMOUNT_DOUBLE = 3.99d;
    private static final Long CHECKOUTCOM_PAYMENT_COST_AMOUNT_LONG = 399L;
    private static final String CURRENCY_CODE = "GBP";
    private static final String PRODUCT_NAME = "product name";
    private static final String DELIVERY_NAME = "Delivery name";
    private static final String DISCOUNT_NAME = "Order total discount";
    private static final String PAYMENT_COST_NAME = "Order payment cost";
    private static final double CHECKOUTCOM_TAXPERCENT_AMOUNT_DOUBLE = 18.189339321593195D;
    private static final Long CHECKOUTCOM_TAXPERCENT_AMOUNT_LONG = 1819L;
    private static final double CHECKOUTCOM_TAXAMOUNT_DOUBLE = 18.948168000000003D;
    private static final Long CHECKOUTCOM_TAXAMOUNT_LONG = 1895L;

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
    private DiscountModel discountModelMock;
    @Mock
    private KlarnaProductRequestDto product1Mock, product2Mock;

    private List<KlarnaProductRequestDto> target = new ArrayList<>();

    @Before
    public void setUp() {
        when(sourceMock.getCurrency()).thenReturn(currencyModelMock);
        when(currencyModelMock.getIsocode()).thenReturn(CURRENCY_CODE);
        when(checkoutComCurrencyServiceMock.convertAmountIntoPennies(CURRENCY_CODE, CHECKOUTCOM_AMOUNT_DOUBLE)).thenReturn(CHECKOUTCOM_AMOUNT_LONG);
        when(checkoutComCurrencyServiceMock.convertAmountIntoPennies(CURRENCY_CODE, CHECKOUTCOM_DISCOUNT_AMOUNT_DOUBLE)).thenReturn(CHECKOUTCOM_DISCOUNT_AMOUNT_LONG);
        when(checkoutComCurrencyServiceMock.convertAmountIntoPennies(CURRENCY_CODE, CHECKOUTCOM_TOTAL_PRICE_AMOUNT_DOUBLE)).thenReturn(CHECKOUTCOM_TOTAL_PRICE_AMOUNT_LONG);
        when(checkoutComCurrencyServiceMock.convertAmountIntoPennies(CURRENCY_CODE, CHECKOUTCOM_PAYMENT_COST_AMOUNT_DOUBLE)).thenReturn(CHECKOUTCOM_PAYMENT_COST_AMOUNT_LONG);
        when(checkoutComCurrencyServiceMock.convertAmountIntoPennies(CURRENCY_CODE, CHECKOUTCOM_TAXPERCENT_AMOUNT_DOUBLE)).thenReturn(0L).thenReturn(CHECKOUTCOM_TAXPERCENT_AMOUNT_LONG);
        when(checkoutComCurrencyServiceMock.convertAmountIntoPennies(CURRENCY_CODE, CHECKOUTCOM_TAXAMOUNT_DOUBLE)).thenReturn(0L).thenReturn(CHECKOUTCOM_TAXAMOUNT_LONG);

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
        when(discountModelMock.getName()).thenReturn(DISCOUNT_NAME);
        when(discountModelMock.getValue()).thenReturn(CHECKOUTCOM_DISCOUNT_AMOUNT_DOUBLE);
        when(sourceMock.getTotalDiscounts()).thenReturn(CHECKOUTCOM_DISCOUNT_AMOUNT_DOUBLE);
        when(sourceMock.getDiscounts()).thenReturn(Collections.singletonList(discountModelMock));
        when(sourceMock.getTotalPrice()).thenReturn(CHECKOUTCOM_TOTAL_PRICE_AMOUNT_DOUBLE);
        when(sourceMock.getPaymentCost()).thenReturn(CHECKOUTCOM_PAYMENT_COST_AMOUNT_DOUBLE);
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
        assertEquals(CHECKOUTCOM_TAXPERCENT_AMOUNT_LONG, target.get(1).getTaxRate());
        assertEquals(CHECKOUTCOM_TAXAMOUNT_LONG, target.get(1).getTotalTaxAmount());
        assertEquals(CHECKOUTCOM_AMOUNT_LONG, target.get(1).getUnitPrice());
        assertEquals(CHECKOUTCOM_AMOUNT_LONG, target.get(1).getTotalAmount());

        assertEquals(DELIVERY_NAME, target.get(2).getName());
        assertEquals(Long.valueOf(1), target.get(2).getQuantity());
        assertEquals(Long.valueOf(0), target.get(2).getTaxRate());
        assertEquals(Long.valueOf(0), target.get(2).getTotalTaxAmount());
        assertEquals(CHECKOUTCOM_AMOUNT_LONG, target.get(2).getUnitPrice());
        assertEquals(CHECKOUTCOM_AMOUNT_LONG, target.get(2).getTotalAmount());

        assertEquals(DISCOUNT_NAME, target.get(3).getName());
        assertEquals(Long.valueOf(1), target.get(3).getQuantity());
        assertEquals(Long.valueOf(0), target.get(3).getTaxRate());
        assertEquals(Long.valueOf(0), target.get(3).getTotalTaxAmount());
        assertEquals(CHECKOUTCOM_CONVERTED_DISCOUNT_AMOUNT_LONG, target.get(3).getUnitPrice());
        assertEquals(CHECKOUTCOM_CONVERTED_DISCOUNT_AMOUNT_LONG, target.get(3).getTotalAmount());

        assertEquals(PAYMENT_COST_NAME, target.get(4).getName());
        assertEquals(Long.valueOf(1), target.get(4).getQuantity());
        assertEquals(Long.valueOf(0), target.get(4).getTaxRate());
        assertEquals(Long.valueOf(0), target.get(4).getTotalTaxAmount());
        assertEquals(CHECKOUTCOM_PAYMENT_COST_AMOUNT_LONG, target.get(4).getUnitPrice());
        assertEquals(CHECKOUTCOM_PAYMENT_COST_AMOUNT_LONG, target.get(4).getTotalAmount());

        verify(checkoutComKlarnaDiscountAmountStrategyMock).applyDiscountsToKlarnaOrderLines(sourceMock, target);
    }

    @Test
    public void populateShippingLine_ShouldDoNothing_WhenShippingAmountIsZero() {
        final List<KlarnaProductRequestDto> productRequestDtos = new ArrayList<>();

        when(sourceMock.getDeliveryCost()).thenReturn(0d);

        testObj.populateShippingLine(sourceMock, CURRENCY_CODE, productRequestDtos, 0.00d);

        assertThat(productRequestDtos).isEmpty();
    }

    @Test
    public void populateShippingLine_ShouldNotSetTaxRate_WhenTaxIsZero() {
        final List<KlarnaProductRequestDto> productRequestDtos = new ArrayList<>();

        testObj.populateShippingLine(sourceMock, CURRENCY_CODE, productRequestDtos, 0.00d);

        assertThat(productRequestDtos).isNotEmpty();

        final KlarnaProductRequestDto shippingRequestDto = productRequestDtos.get(0);

        assertThat(shippingRequestDto.getTaxRate()).isZero();
    }

    @Test
    public void populateShippingLine_ShouldNotSetTaxRateIfTax_WhenLessThanZero() {
        final List<KlarnaProductRequestDto> productRequestDtos = new ArrayList<>();

        when(sourceMock.getTotalTax()).thenReturn(100.00d * -1);

        testObj.populateShippingLine(sourceMock, CURRENCY_CODE, productRequestDtos, 0.00d);

        assertThat(productRequestDtos).isNotEmpty();

        final KlarnaProductRequestDto shippingRequestDto = productRequestDtos.get(0);

        assertThat(shippingRequestDto.getTaxRate()).isZero();
    }

    @Test
    public void populateShippingLine_ShouldSetTaxRate_WhenIfTaxIsGreaterThanZero() {
        final List<KlarnaProductRequestDto> productRequestDtos = new ArrayList<>();

        doReturn(50L).when(checkoutComCurrencyServiceMock).convertAmountIntoPennies(CURRENCY_CODE, CHECKOUTCOM_TAX_RATE_DOUBLE);
        when(sourceMock.getTotalTaxValues()).thenReturn(List.of(taxValueMock));

        testObj.populateShippingLine(sourceMock, CURRENCY_CODE, productRequestDtos, CHECKOUTCOM_TAX_RATE_DOUBLE);

        assertThat(productRequestDtos).isNotEmpty();

        final KlarnaProductRequestDto shippingRequestDto = productRequestDtos.get(0);

        assertThat(shippingRequestDto.getTaxRate()).isNotZero();
    }

    @Test
    public void populateOrderDiscount_ShouldDoNothing_WhenTotalDiscountIsZero() {
        final List<KlarnaProductRequestDto> productRequestDtos = new ArrayList<>();

        when(sourceMock.getTotalDiscounts()).thenReturn(0d);

        testObj.populateOrderDiscount(sourceMock, CURRENCY_CODE, productRequestDtos, 0.00d);

        assertThat(productRequestDtos).isEmpty();
    }

    @Test
    public void populateOrderDiscount_ShouldNotSetTaxRate_WhenTaxIsZero() {
        final List<KlarnaProductRequestDto> productRequestDtos = new ArrayList<>();

        testObj.populateOrderDiscount(sourceMock, CURRENCY_CODE, productRequestDtos, 0.00d);

        assertThat(productRequestDtos).isNotEmpty();

        final KlarnaProductRequestDto discountRequestDto = productRequestDtos.get(0);

        assertThat(discountRequestDto.getTaxRate()).isZero();
    }

    @Test
    public void populateOrderDiscount_ShouldSetTaxRate_WhenIfTaxIsGreaterThanZero() {
        final List<KlarnaProductRequestDto> productRequestDtos = new ArrayList<>();

        doReturn(50L).when(checkoutComCurrencyServiceMock).convertAmountIntoPennies(CURRENCY_CODE, CHECKOUTCOM_TAX_RATE_DOUBLE);

        when(sourceMock.getTotalTaxValues()).thenReturn(List.of(taxValueMock));
        testObj.populateOrderDiscount(sourceMock, CURRENCY_CODE, productRequestDtos, CHECKOUTCOM_TAX_RATE_DOUBLE);

        assertThat(productRequestDtos).isNotEmpty();

        final KlarnaProductRequestDto discountRequestDto = productRequestDtos.get(0);

        assertThat(discountRequestDto.getTaxRate()).isNotZero();
    }

    @Test
    public void populateOrderDiscount_ShouldNotSetTaxRateIfTax_WhenLessThanZero() {
        final List<KlarnaProductRequestDto> productRequestDtos = new ArrayList<>();

        when(sourceMock.getTotalTax()).thenReturn(100.00d * -1);

        testObj.populateOrderDiscount(sourceMock, CURRENCY_CODE, productRequestDtos, 0.00d);

        assertThat(productRequestDtos).isNotEmpty();

        final KlarnaProductRequestDto discountRequestDto = productRequestDtos.get(0);

        assertThat(discountRequestDto.getTaxRate()).isZero();
    }

    @Test
    public void populatePaymentCost_ShouldDoNothing_WhenPaymentCostIsZero() {
        final List<KlarnaProductRequestDto> productRequestDtos = new ArrayList<>();

        when(sourceMock.getPaymentCost()).thenReturn(0d);

        testObj.populatePaymentCost(sourceMock, CURRENCY_CODE, productRequestDtos, 0.00d);

        assertThat(productRequestDtos).isEmpty();
    }

    @Test
    public void populatePaymentCost_ShouldNotSetTaxRate_WhenTaxIsZero() {
        final List<KlarnaProductRequestDto> productRequestDtos = new ArrayList<>();

        testObj.populatePaymentCost(sourceMock, CURRENCY_CODE, productRequestDtos, 0.00d);

        assertThat(productRequestDtos).isNotEmpty();

        final KlarnaProductRequestDto paymentCostRequestDto = productRequestDtos.get(0);

        assertThat(paymentCostRequestDto.getTaxRate()).isZero();
    }

    @Test
    public void populatePaymentCost_ShouldSetTaxRate_WhenIfTaxIsGreaterThanZero() {
        final List<KlarnaProductRequestDto> productRequestDtos = new ArrayList<>();

        doReturn(50L).when(checkoutComCurrencyServiceMock).convertAmountIntoPennies(CURRENCY_CODE, CHECKOUTCOM_TAX_RATE_DOUBLE);
        when(sourceMock.getTotalTaxValues()).thenReturn(List.of(taxValueMock));

        testObj.populatePaymentCost(sourceMock, CURRENCY_CODE, productRequestDtos, CHECKOUTCOM_TAX_RATE_DOUBLE);

        assertThat(productRequestDtos).isNotEmpty();
        final KlarnaProductRequestDto paymentCostRequestDto = productRequestDtos.get(0);
        assertThat(paymentCostRequestDto.getTaxRate()).isNotZero();
    }

    @Test
    public void populatePaymentCost_ShouldNotSetTaxRateIfTax_WhenLessThanZero() {
        final List<KlarnaProductRequestDto> productRequestDtos = new ArrayList<>();

        when(sourceMock.getTotalTax()).thenReturn(100.00d * -1);

        testObj.populatePaymentCost(sourceMock, CURRENCY_CODE, productRequestDtos, 0.00d);

        assertThat(productRequestDtos).isNotEmpty();

        final KlarnaProductRequestDto paymentCostRequestDto = productRequestDtos.get(0);

        assertThat(paymentCostRequestDto.getTaxRate()).isZero();
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
