package com.checkout.hybris.core.payment.request.strategies.impl;

import com.checkout.hybris.core.address.strategies.CheckoutComPhoneNumberStrategy;
import com.checkout.hybris.core.currency.services.CheckoutComCurrencyService;
import com.checkout.hybris.core.klarna.request.KlarnaAddressDto;
import com.checkout.hybris.core.klarna.session.request.KlarnaProductRequestDto;
import com.checkout.hybris.core.merchant.services.CheckoutComMerchantConfigurationService;
import com.checkout.hybris.core.model.CheckoutComKlarnaAPMPaymentInfoModel;
import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.core.payment.request.mappers.CheckoutComPaymentRequestStrategyMapper;
import com.checkout.hybris.core.payment.request.strategies.CheckoutComPaymentRequestStrategy;
import com.checkout.hybris.core.populators.payments.CheckoutComCartModelToPaymentL2AndL3Converter;
import com.checkout.hybris.core.url.services.CheckoutComUrlService;
import com.checkout.payments.AlternativePaymentSource;
import com.checkout.payments.PaymentRequest;
import com.checkout.payments.RequestSource;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.cms2.servicelayer.services.CMSSiteService;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import java.util.List;
import java.util.Optional;

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.KLARNA;
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

/**
 * specific {@link CheckoutComPaymentRequestStrategy} implementation for Klarna apm payments
 */
@SuppressWarnings("java:S107")
public class CheckoutComKlarnaPaymentRequestStrategy extends CheckoutComAbstractApmPaymentRequestStrategy {

    protected static final String AUTHORIZATION_TOKEN_KEY = "authorization_token";
    protected static final String LOCALE_KEY = "locale";
    protected static final String PURCHASE_COUNTRY_KEY = "purchase_country";
    protected static final String TAX_AMOUNT_KEY = "tax_amount";
    protected static final String PRODUCTS_KEY = "products";
    protected static final String BILLING_ADDRESS_KEY = "billing_address";
    protected static final String SHIPPING_ADDRESS_KEY = "shipping_address";
    protected static final String MERCHANT_REFERENCE_KEY = "merchant_reference1";

    protected final Converter<CartModel, List<KlarnaProductRequestDto>> checkoutComKlarnaProductsRequestDtoConverter;

    public CheckoutComKlarnaPaymentRequestStrategy(final CheckoutComUrlService checkoutComUrlService,
                                                   final CheckoutComPhoneNumberStrategy checkoutComPhoneNumberStrategy,
                                                   final CheckoutComCurrencyService checkoutComCurrencyService,
                                                   final CheckoutComPaymentRequestStrategyMapper checkoutComPaymentRequestStrategyMapper,
                                                   final CMSSiteService cmsSiteService,
                                                   final CheckoutComMerchantConfigurationService checkoutComMerchantConfigurationService,
                                                   final CheckoutComCartModelToPaymentL2AndL3Converter checkoutComCartModelToPaymentL2AndL3Converter,
                                                   final Converter<CartModel, List<KlarnaProductRequestDto>> checkoutComKlarnaProductsRequestDtoConverter) {
        super(checkoutComUrlService, checkoutComPhoneNumberStrategy, checkoutComCurrencyService,
              checkoutComPaymentRequestStrategyMapper, cmsSiteService, checkoutComMerchantConfigurationService,
              checkoutComCartModelToPaymentL2AndL3Converter);
        this.checkoutComKlarnaProductsRequestDtoConverter = checkoutComKlarnaProductsRequestDtoConverter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CheckoutComPaymentType getStrategyKey() {
        return KLARNA;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PaymentRequest<RequestSource> getRequestSourcePaymentRequest(final CartModel cart,
                                                                           final String currencyIsoCode, final Long amount) {
        final PaymentRequest<RequestSource> paymentRequest = super.getRequestSourcePaymentRequest(cart, currencyIsoCode, amount);
        final AlternativePaymentSource source = (AlternativePaymentSource) paymentRequest.getSource();
        final CustomerModel customer = (CustomerModel) cart.getUser();

        final CheckoutComKlarnaAPMPaymentInfoModel klarnaPaymentInfo = (CheckoutComKlarnaAPMPaymentInfoModel) cart.getPaymentInfo();
        source.put(AUTHORIZATION_TOKEN_KEY, klarnaPaymentInfo.getAuthorizationToken());

        final String currencyCode = cart.getCurrency() != null ? cart.getCurrency().getIsocode() : null;
        final CMSSiteModel site = (CMSSiteModel) cart.getSite();
        validateParameterNotNull(site, "CMSSite model cannot be null");
        source.put(LOCALE_KEY, site.getLocale().replace("_", "-"));

        final AddressModel billingAddress = klarnaPaymentInfo.getBillingAddress();
        validateParameterNotNull(billingAddress, "Billing address model cannot be null");
        validateParameterNotNull(cart.getDeliveryAddress(), "Delivery address model cannot be null");
        validateParameterNotNull(billingAddress.getCountry(), "Billing address country cannot be null");
        source.put(PURCHASE_COUNTRY_KEY, billingAddress.getCountry().getIsocode());
        source.put(BILLING_ADDRESS_KEY, createAddressDto(billingAddress, customer.getContactEmail()));
        source.put(SHIPPING_ADDRESS_KEY, createAddressDto(cart.getDeliveryAddress(), customer.getContactEmail()));

        source.put(TAX_AMOUNT_KEY, checkoutComCurrencyService.convertAmountIntoPennies(currencyCode, cart.getTotalTax()));
        source.put(MERCHANT_REFERENCE_KEY, cart.getCheckoutComPaymentReference());

        source.put(PRODUCTS_KEY, checkoutComKlarnaProductsRequestDtoConverter.convert(cart));

        return paymentRequest;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Optional<Boolean> isCapture() {
        return Optional.of(Boolean.FALSE);
    }

    /**
     * Populates the address dto from the given address model
     *
     * @param address the source address
     * @param email   the customer email
     * @return the klarna address dto
     */
    protected KlarnaAddressDto createAddressDto(final AddressModel address, final String email) {
        final KlarnaAddressDto klarnaAddressDto = new KlarnaAddressDto();
        klarnaAddressDto.setTitle(address.getTitle() != null ? address.getTitle().getName() : null);
        klarnaAddressDto.setGivenName(address.getFirstname());
        klarnaAddressDto.setFamilyName(address.getLastname());
        klarnaAddressDto.setStreetAddress(address.getLine1());
        klarnaAddressDto.setStreetAddress2(address.getLine2());
        klarnaAddressDto.setCity(address.getTown());
        klarnaAddressDto.setPostalCode(address.getPostalcode());
        klarnaAddressDto.setRegion(address.getRegion() != null ? address.getRegion().getName() : null);
        klarnaAddressDto.setCountry(address.getCountry() != null ? address.getCountry().getIsocode() : null);
        klarnaAddressDto.setEmail(email);
        klarnaAddressDto.setPhone(address.getPhone1());
        return klarnaAddressDto;
    }
}
