package com.checkout.hybris.core.populators.payments;

import com.checkout.common.Address;
import com.checkout.hybris.core.currency.services.CheckoutComCurrencyService;

import com.checkout.payments.*;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.util.DiscountValue;
import de.hybris.platform.util.TaxValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class DefaultCheckoutComCartModelToPaymentL2AndL3Converter implements CheckoutComCartModelToPaymentL2AndL3Converter {

    private static final Logger LOG = LogManager.getLogger(
        DefaultCheckoutComCartModelToPaymentL2AndL3Converter.class);

    protected final CheckoutComCurrencyService checkoutComCurrencyService;

    public DefaultCheckoutComCartModelToPaymentL2AndL3Converter(final CheckoutComCurrencyService checkoutComCurrencyService) {
        this.checkoutComCurrencyService = checkoutComCurrencyService;
    }


    @Override
    public void convert(final CartModel cartModel,
                        final PaymentRequest<RequestSource> requestSourcePaymentRequest) throws ConversionException {
        populateL2Fields(cartModel, requestSourcePaymentRequest);
        populateL3Fields(cartModel, requestSourcePaymentRequest);
    }


    protected void populateL2Fields(final CartModel cart, final PaymentRequest<RequestSource> request) {
        Processing processing = request.getProcessing();
        if (processing == null) {
            processing = new Processing();
        }
        processing.setOrderId(cart.getCheckoutComPaymentReference());
        processing.setTaxAmount(checkoutComCurrencyService.convertAmountIntoPennies(cart.getCurrency().getIsocode(),
                                                                                    cart.getTotalTax()));
        request.setProcessing(processing);

        CustomerRequest customerRequest = request.getCustomer();
        if (customerRequest == null) {
            customerRequest = new CustomerRequest();
        }
        final CustomerModel customerModel = (CustomerModel) cart.getUser();
        customerRequest.setTaxNumber(customerModel.getTaxNumber());
        request.setCustomer(customerRequest);
    }

    protected void populateL3Fields(final CartModel cart, final PaymentRequest<RequestSource> request) {
        Processing processing = request.getProcessing();
        if (processing == null) {
            processing = new Processing();
        }

        final String currencyIsocode = cart.getCurrency().getIsocode();
        if(cart.getTotalDiscounts() != null) {
            processing.setDiscountAmount(
                checkoutComCurrencyService.convertAmountIntoPennies(currencyIsocode,
                                                                    cart.getTotalDiscounts()));
        }
        processing.setShippingAmount(
            checkoutComCurrencyService.convertAmountIntoPennies(currencyIsocode, cart.getDeliveryCost()));
        processing.setShippingTaxAmount(populateShippingTaxAmount(cart));
        if(((CustomerModel) cart.getUser()).getDutyAmount() != null) {
            processing.setDutyAmount(checkoutComCurrencyService.convertAmountIntoPennies(currencyIsocode, ((CustomerModel) cart.getUser()).getDutyAmount()));
        }
        request.setProcessing(processing);

        ShippingDetails shippingDetails = request.getShipping();
        if (shippingDetails == null) {
            shippingDetails = new ShippingDetails();
        }
        Address address = shippingDetails.getAddress();
        if (address == null) {
            address = new Address();
        }
        final AddressModel deliveryAddress = cart.getDeliveryAddress();
        address.setCountry(deliveryAddress.getCountry().getIsocode());
        address.setZip(deliveryAddress.getPostalcode());
        shippingDetails.setAddress(address);
        shippingDetails.setFromAddressZip(populateFromAddress());
        request.setShipping(shippingDetails);
        populateItems(cart.getEntries(), request);
    }

    private Long populateShippingTaxAmount(final CartModel cart) {
        LOG.info("Populate shipping tax amount with your own implementation here");

        return null;
    }

    protected String populateFromAddress() {
        LOG.info("Populate from address with your own implementation here");
        return null;
    }

    protected void populateItems(final List<AbstractOrderEntryModel> entries,
                                 final PaymentRequest<RequestSource> request) {
        final List<Product> products = entries.stream().map(this::populateEntry).collect(Collectors.toList());
        request.setItems(products);
    }

    protected Product populateEntry(final AbstractOrderEntryModel entry) {
        final ProductModel product = entry.getProduct();
        String isocode = entry.getOrder().getCurrency().getIsocode();
        return Product.builder().commodityCode(product.getCode())
                      .name(product.getName())
                      .quantity(entry.getQuantity())
                      .reference(product.getCode())
                      .totalAmount(checkoutComCurrencyService
                                       .convertAmountIntoPennies(isocode,
                                                                     entry.getTotalPrice()))
                      .unitOfMeasure(entry.getUnit().getCode())
                      .taxAmount(checkoutComCurrencyService.convertAmountIntoPennies(isocode, getTaxEntryAmount(entry)))
                      .unitPrice(checkoutComCurrencyService
                                     .convertAmountIntoPennies(isocode,
                                                                   entry.getBasePrice()))
                      .discountAmount(checkoutComCurrencyService.convertAmountIntoPennies(isocode, populateDiscountValues(entry))).build();
    }

    private Double populateDiscountValues(AbstractOrderEntryModel entry) {
        List<DiscountValue> discountValues = entry.getDiscountValues();
        if (discountValues != null && !discountValues.isEmpty()) {
            return discountValues.stream().mapToDouble(DiscountValue::getValue).sum();
        }

        return 0.0;
    }

    protected Double getTaxEntryAmount(final AbstractOrderEntryModel entry) {
        Collection<TaxValue> taxValues = entry.getTaxValues();
        if(taxValues != null && !taxValues.isEmpty()) {
            return taxValues.stream().mapToDouble(TaxValue::getValue).sum();
        }
        return 0.0;
    }
}
