package com.checkout.hybris.core.payment.klarna.request.converters.populators;

import com.checkout.hybris.core.currency.services.CheckoutComCurrencyService;
import com.checkout.hybris.core.klarna.session.request.KlarnaProductRequestDto;
import com.checkout.hybris.core.payment.klarna.request.strategies.CheckoutComKlarnaDiscountAmountStrategy;
import com.google.common.util.concurrent.AtomicDouble;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.apache.commons.lang.math.NumberUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

/**
 * Populates the list of KlarnaProductRequestDto from the cart model
 */
public class CheckoutComKlarnaProductsRequestDtoPopulator implements Populator<CartModel, List<KlarnaProductRequestDto>> {

    protected static final String SHIPPING_FEE_VALUE = "shipping_fee";
    protected static final String ORDER_DISCOUNT = "discount";
    protected static final String SURCHARGE_VALUE = "surcharge";

    protected final CheckoutComCurrencyService checkoutComCurrencyService;
    protected final CheckoutComKlarnaDiscountAmountStrategy checkoutComKlarnaDiscountAmountStrategy;

    public CheckoutComKlarnaProductsRequestDtoPopulator(final CheckoutComCurrencyService checkoutComCurrencyService,
                                                        final CheckoutComKlarnaDiscountAmountStrategy checkoutComKlarnaDiscountAmountStrategy) {
        this.checkoutComCurrencyService = checkoutComCurrencyService;
        this.checkoutComKlarnaDiscountAmountStrategy = checkoutComKlarnaDiscountAmountStrategy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void populate(final CartModel source, final List<KlarnaProductRequestDto> target) throws ConversionException {
        validateParameterNotNull(source, "CartModel cannot be null.");
        validateParameterNotNull(target, "List of KlarnaProductRequestDto cannot be null.");
        final String currencyCode = source.getCurrency() != null ? source.getCurrency().getIsocode() : null;

        final AtomicLong totalProductTaxes = new AtomicLong(NumberUtils.LONG_ZERO);
        if (isNotEmpty(source.getEntries())) {
            for (AbstractOrderEntryModel cartEntry : source.getEntries()) {
                final KlarnaProductRequestDto productRequestDto = new KlarnaProductRequestDto();

                final ProductModel product = cartEntry.getProduct();
                productRequestDto.setName(product != null ? product.getName() : null);
                productRequestDto.setQuantity(cartEntry.getQuantity());

                final long totalTaxAmount = isNotEmpty(cartEntry.getTaxValues()) ? checkoutComCurrencyService.convertAmountIntoPennies(currencyCode, cartEntry.getTaxValues().iterator().next().getAppliedValue()) : NumberUtils.LONG_ZERO;
                productRequestDto.setTotalTaxAmount(totalTaxAmount);

                final long taxRate = isNotEmpty(cartEntry.getTaxValues()) ? checkoutComCurrencyService.convertAmountIntoPennies(currencyCode, cartEntry.getTaxValues().iterator().next().getValue()) : NumberUtils.LONG_ZERO;
                productRequestDto.setTaxRate(taxRate);

                productRequestDto.setUnitPrice(checkoutComCurrencyService.convertAmountIntoPennies(currencyCode, cartEntry.getBasePrice()));

                final AtomicDouble doubleDiscountAmount = new AtomicDouble(NumberUtils.DOUBLE_ZERO);
                cartEntry.getDiscountValues().forEach(discountValue -> doubleDiscountAmount.addAndGet(discountValue.getAppliedValue()));
                final long totalDiscount = checkoutComCurrencyService.convertAmountIntoPennies(currencyCode, doubleDiscountAmount.get());
                productRequestDto.setTotalDiscountAmount(totalDiscount);

                final long totalAmount = checkoutComCurrencyService.convertAmountIntoPennies(currencyCode, cartEntry.getBasePrice()) * cartEntry.getQuantity() - totalDiscount;
                productRequestDto.setTotalAmount(totalAmount);

                totalProductTaxes.addAndGet(totalTaxAmount);
                target.add(productRequestDto);
            }
        }

        final double taxRate = calculateTaxRate(source);
        populateShippingLine(source, currencyCode, target, taxRate);
        populateOrderDiscount(source, currencyCode, target, taxRate);
        populatePaymentCost(source, currencyCode, target, taxRate);

        checkoutComKlarnaDiscountAmountStrategy.applyDiscountsToKlarnaOrderLines(source, target);
    }

    /**
     * Populates the order line related to the delivery mode
     *
     * @param cart                     the cart model
     * @param currencyCode             the currency code
     * @param klarnaProductRequestDtos the request to populate
     * @param taxRate                  the tax rate amount
     */
    protected void populateShippingLine(final CartModel cart,
                                        final String currencyCode,
                                        final List<KlarnaProductRequestDto> klarnaProductRequestDtos,
                                        final double taxRate) {
        if (cart.getDeliveryMode() != null && cart.getDeliveryCost() > 0) {
            final KlarnaProductRequestDto shippingLine = new KlarnaProductRequestDto();
            shippingLine.setName(cart.getDeliveryMode().getName());
            shippingLine.setQuantity(NumberUtils.LONG_ONE);

            shippingLine.setTaxRate(checkoutComCurrencyService.convertAmountIntoPennies(currencyCode, taxRate) * 100);

            final double totalShippingTaxes = taxRate * cart.getDeliveryCost();
            shippingLine.setTotalTaxAmount(checkoutComCurrencyService.convertAmountIntoPennies(currencyCode, totalShippingTaxes));

            final long totalAmount = checkoutComCurrencyService.convertAmountIntoPennies(currencyCode, cart.getDeliveryCost());
            shippingLine.setTotalAmount(totalAmount);

            shippingLine.setUnitPrice(checkoutComCurrencyService.convertAmountIntoPennies(currencyCode, cart.getDeliveryCost()));

            shippingLine.setType(SHIPPING_FEE_VALUE);
            shippingLine.setTotalDiscountAmount(NumberUtils.LONG_ZERO);
            klarnaProductRequestDtos.add(shippingLine);
        }
    }

    /**
     * Populates the order line related to the cart discounts
     *
     * @param cart                     the cart model
     * @param klarnaProductRequestDtos the request to populate
     * @param currencyCode             the currency code
     * @param taxRate                  tha tax rate amount
     */
    protected void populateOrderDiscount(final CartModel cart,
                                         final String currencyCode,
                                         final List<KlarnaProductRequestDto> klarnaProductRequestDtos,
                                         final double taxRate) {
        if (cart.getDiscounts() != null && cart.getTotalDiscounts() > 0) {
            final KlarnaProductRequestDto orderDiscount = new KlarnaProductRequestDto();

            orderDiscount.setName("Order total discount");
            orderDiscount.setQuantity(NumberUtils.LONG_ONE);
            final long totalDiscount = checkoutComCurrencyService.convertAmountIntoPennies(currencyCode, cart.getTotalDiscounts()) * -1;
            orderDiscount.setTotalAmount(totalDiscount);
            orderDiscount.setUnitPrice(totalDiscount);
            orderDiscount.setType(ORDER_DISCOUNT);

            orderDiscount.setTaxRate(checkoutComCurrencyService.convertAmountIntoPennies(currencyCode, taxRate) * 100);

            final double totalDiscountTaxes = taxRate * cart.getTotalDiscounts() * -1;
            orderDiscount.setTotalTaxAmount(checkoutComCurrencyService.convertAmountIntoPennies(currencyCode, totalDiscountTaxes));

            klarnaProductRequestDtos.add(orderDiscount);
        }

    }

    /**
     * Populate the order line related to the payment cost
     *
     * @param cart                     the cart model
     * @param currencyCode             the currency code
     * @param klarnaProductRequestDtos the request to populate
     * @param taxRate                  the tax rate amount
     */
    protected void populatePaymentCost(CartModel cart, String currencyCode, List<KlarnaProductRequestDto> klarnaProductRequestDtos, double taxRate) {
        if (cart.getPaymentCost() > 0) {
            final KlarnaProductRequestDto paymentCost = new KlarnaProductRequestDto();

            paymentCost.setName("Order payment cost");
            paymentCost.setQuantity(NumberUtils.LONG_ONE);
            final long orderPaymentCost = checkoutComCurrencyService.convertAmountIntoPennies(currencyCode, cart.getPaymentCost());
            paymentCost.setTotalAmount(orderPaymentCost);
            paymentCost.setUnitPrice(orderPaymentCost);
            paymentCost.setType(SURCHARGE_VALUE);

            paymentCost.setTaxRate(checkoutComCurrencyService.convertAmountIntoPennies(currencyCode, taxRate) * 100);

            final double paymentCostTaxes = taxRate * cart.getPaymentCost();
            paymentCost.setTotalTaxAmount(checkoutComCurrencyService.convertAmountIntoPennies(currencyCode, paymentCostTaxes));

            klarnaProductRequestDtos.add(paymentCost);
        }
    }

    /**
     * Calculate discount & shipping tax rate amount.
     *
     * @param cart the cart model
     * @return tha tax rate amount
     */
    private double calculateTaxRate(final CartModel cart) {
        return cart.getTotalTax() >= 0 ? cart.getTotalTax() / cart.getTotalPrice() : NumberUtils.DOUBLE_ZERO;
    }
}
