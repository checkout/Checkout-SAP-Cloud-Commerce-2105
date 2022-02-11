package com.checkout.hybris.facades.payment.clienttoken.request.converters.populators;

import com.checkout.hybris.core.currency.services.CheckoutComCurrencyService;
import com.checkout.hybris.core.klarna.session.request.KlarnaProductRequestDto;
import com.checkout.hybris.core.klarna.session.request.KlarnaSessionRequestDto;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.c2l.C2LItemModel;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

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

        final Optional<String> currencyCode = Optional.ofNullable(source.getCurrency())
                .map(CurrencyModel::getIsocode)
                .filter(StringUtils::isNotBlank);
        currencyCode.ifPresent(target::setCurrency);

        target.setLocale(Optional.ofNullable(source.getSite().getLocale())
                .orElse(Locale.UK.toString())
                .replace("_", "-")
        );
        Optional.ofNullable(source.getPaymentAddress())
                .map(AddressModel::getCountry)
                .map(C2LItemModel::getIsocode)
                .ifPresent(target::setPurchaseCountry);

        currencyCode.ifPresent(currency -> target.setAmount(checkoutComCurrencyService.convertAmountIntoPennies(currency, source.getTotalPrice())));
        currencyCode.ifPresent(currency -> target.setTaxAmount(checkoutComCurrencyService.convertAmountIntoPennies(currency, source.getTotalTax())));
        target.setProducts(checkoutComKlarnaProductsRequestDtoConverter.convert(source));
    }
}
