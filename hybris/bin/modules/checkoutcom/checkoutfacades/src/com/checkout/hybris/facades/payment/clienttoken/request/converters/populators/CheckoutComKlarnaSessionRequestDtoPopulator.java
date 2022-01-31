package com.checkout.hybris.facades.payment.clienttoken.request.converters.populators;

import com.checkout.hybris.core.currency.services.CheckoutComCurrencyService;
import com.checkout.hybris.core.klarna.session.request.KlarnaProductRequestDto;
import com.checkout.hybris.core.klarna.session.request.KlarnaSessionRequestDto;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import java.util.List;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

/**
 * Populates the KlarnaSessionRequestDto from the CartModel
 */
public class CheckoutComKlarnaSessionRequestDtoPopulator implements Populator<CartModel, KlarnaSessionRequestDto> {

    protected final CheckoutComCurrencyService checkoutComCurrencyService;
    protected final Converter<CartModel, List<KlarnaProductRequestDto>> checkoutComKlarnaProductsRequestDtoConverter;

    public CheckoutComKlarnaSessionRequestDtoPopulator(final CheckoutComCurrencyService checkoutComCurrencyService,
                                                       final Converter<CartModel, List<KlarnaProductRequestDto>> checkoutComKlarnaProductsRequestDtoConverter) {
        this.checkoutComCurrencyService = checkoutComCurrencyService;
        this.checkoutComKlarnaProductsRequestDtoConverter = checkoutComKlarnaProductsRequestDtoConverter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void populate(final CartModel source, final KlarnaSessionRequestDto target) throws ConversionException {
        validateParameterNotNull(source, "CartModel cannot be null.");
        validateParameterNotNull(target, "KlarnaSessionRequestDto cannot be null.");

        final String currencyCode = source.getCurrency() != null ? source.getCurrency().getIsocode() : null;
        target.setCurrency(currencyCode);
        final CMSSiteModel site = (CMSSiteModel) source.getSite();
        target.setLocale(site != null ? site.getLocale().replace("_", "-") : null);
        if (source.getPaymentAddress() != null && source.getPaymentAddress().getCountry() != null) {
            target.setPurchaseCountry(source.getPaymentAddress().getCountry().getIsocode());
        }
        target.setAmount(checkoutComCurrencyService.convertAmountIntoPennies(currencyCode, source.getTotalPrice()));
        target.setTaxAmount(checkoutComCurrencyService.convertAmountIntoPennies(currencyCode, source.getTotalTax()));
        target.setProducts(checkoutComKlarnaProductsRequestDtoConverter.convert(source));
    }
}