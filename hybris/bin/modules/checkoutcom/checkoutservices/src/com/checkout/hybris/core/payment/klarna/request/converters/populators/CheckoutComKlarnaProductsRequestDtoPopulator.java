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

        populateShippingLine(source, currencyCode, target, totalProductTaxes.get());

        checkoutComKlarnaDiscountAmountStrategy.applyDiscountsToKlarnaOrderLines(source, target);
    }

    /**
     * Populates the order line related to the delivery mode
     *
     * @param cart                     the cart model
     * @param currencyCode             the currency code
     * @param klarnaProductRequestDtos the request to populate
     * @param totalProductTaxes        the total tax amount
     */
    protected void populateShippingLine(final CartModel cart,
                                        final String currencyCode,
                                        final List<KlarnaProductRequestDto> klarnaProductRequestDtos,
                                        final long totalProductTaxes) {
        if (cart.getDeliveryMode() != null && cart.getDeliveryCost() > 0) {
            final KlarnaProductRequestDto shippingLine = new KlarnaProductRequestDto();
            shippingLine.setName(cart.getDeliveryMode().getName());
            shippingLine.setQuantity(NumberUtils.LONG_ONE);

            final long totalTaxAmount = checkoutComCurrencyService.convertAmountIntoPennies(currencyCode, cart.getTotalTax()) - totalProductTaxes;
            shippingLine.setTotalTaxAmount(totalTaxAmount);

            final long totalAmount = checkoutComCurrencyService.convertAmountIntoPennies(currencyCode, cart.getDeliveryCost());
            shippingLine.setTotalAmount(totalAmount);

            long taxRate = 0;
            if ((totalAmount > totalTaxAmount)){
                taxRate = ((totalAmount * 10000) / (totalAmount - totalTaxAmount)) - 10000;
            }
            shippingLine.setTaxRate(taxRate);

            shippingLine.setUnitPrice(checkoutComCurrencyService.convertAmountIntoPennies(currencyCode, cart.getDeliveryCost()));

            shippingLine.setType(SHIPPING_FEE_VALUE);
            shippingLine.setTotalDiscountAmount(NumberUtils.LONG_ZERO);
            klarnaProductRequestDtos.add(shippingLine);
        }
    }
}
