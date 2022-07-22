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
import de.hybris.platform.util.DiscountValue;
import de.hybris.platform.util.TaxValue;
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
        final double taxRate = calculateTaxRate(source);
        if (isNotEmpty(source.getEntries())) {
            for (AbstractOrderEntryModel cartEntry : source.getEntries()) {
                final KlarnaProductRequestDto productRequestDto = new KlarnaProductRequestDto();

                final double discountAmount = cartEntry.getDiscountValues().stream()
                        .map(DiscountValue::getAppliedValue)
                        .reduce(Double::sum)
                        .orElse(NumberUtils.DOUBLE_ZERO);

                final double totalAmount =  cartEntry.getBasePrice() * cartEntry.getQuantity() - discountAmount;
                //TaxRate calculated for Gross tax mode
                final double totalTaxAmount = totalAmount * taxRate;
                final double cartEntryTaxRate = totalTaxAmount / (totalAmount - totalTaxAmount);
                final ProductModel product = cartEntry.getProduct();

                productRequestDto.setName(product != null ? product.getName() : null);
                productRequestDto.setQuantity(cartEntry.getQuantity());
                productRequestDto.setTotalDiscountAmount(checkoutComCurrencyService.convertAmountIntoPennies(currencyCode, discountAmount));
                productRequestDto.setTotalAmount(checkoutComCurrencyService.convertAmountIntoPennies(currencyCode, totalAmount));
                productRequestDto.setTotalTaxAmount(checkoutComCurrencyService.convertAmountIntoPennies(currencyCode,totalTaxAmount));
                productRequestDto.setTaxRate(checkoutComCurrencyService.convertAmountIntoPennies(currencyCode,cartEntryTaxRate * 100));
                productRequestDto.setUnitPrice(checkoutComCurrencyService.convertAmountIntoPennies(currencyCode, cartEntry.getBasePrice()));

                target.add(productRequestDto);
            }
        }

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

            if (doesCartContainsTaxes(cart)) {
                shippingLine.setTaxRate(checkoutComCurrencyService.convertAmountIntoPennies(currencyCode, getTotalTaxPercent(cart)));
                final double totalShippingTaxes = taxRate * cart.getDeliveryCost();
                shippingLine.setTotalTaxAmount(checkoutComCurrencyService.convertAmountIntoPennies(currencyCode, totalShippingTaxes));
            } else {
                shippingLine.setTaxRate(0L);
                shippingLine.setTotalTaxAmount(0L);
            }
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

            if (doesCartContainsTaxes(cart)) {
                orderDiscount.setTaxRate(checkoutComCurrencyService.convertAmountIntoPennies(currencyCode, getTotalTaxPercent(cart)));
                final double totalDiscountTaxes = taxRate * cart.getTotalDiscounts() * -1;
                orderDiscount.setTotalTaxAmount(checkoutComCurrencyService.convertAmountIntoPennies(currencyCode, totalDiscountTaxes));
            } else {
                orderDiscount.setTaxRate(0L);
                orderDiscount.setTotalTaxAmount(0L);
            }
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

            if (doesCartContainsTaxes(cart)) {
                paymentCost.setTaxRate(checkoutComCurrencyService.convertAmountIntoPennies(currencyCode, getTotalTaxPercent(cart)));
                final double paymentCostTaxes = taxRate * cart.getPaymentCost();
                paymentCost.setTotalTaxAmount(checkoutComCurrencyService.convertAmountIntoPennies(currencyCode, paymentCostTaxes));
            } else {
                paymentCost.setTaxRate(0L);
                paymentCost.setTotalTaxAmount(0L);
            }
            klarnaProductRequestDtos.add(paymentCost);
        }
    }

    /**
     * Calculate discount & shipping tax rate amount.
     *
     * @param cart the cart model
     * @return tha tax rate amount
     */
    protected double calculateTaxRate(final CartModel cart) {
        return cart.getTotalTax() >= 0 ? cart.getTotalTax() / cart.getTotalPrice() : NumberUtils.DOUBLE_ZERO;
    }

    /**
     * Checks if there is any tax applied on the cart
     *
     * @param cart
     * @return
     */
    protected boolean doesCartContainsTaxes(final CartModel cart) {
        return !cart.getTotalTaxValues().isEmpty();
    }

    /**
     * Gets all the taxes applied on the cart
     *
     * @param cart
     * @return
     */
    protected Double getTotalTaxPercent(final CartModel cart) {
        return cart.getTotalTaxValues().stream().map(TaxValue::getValue).reduce(Double::sum).orElse(0D);
    }
}
