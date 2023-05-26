package com.checkout.hybris.core.payment.request.strategies.impl;

import com.checkout.hybris.core.address.strategies.CheckoutComPhoneNumberStrategy;
import com.checkout.hybris.core.apm.services.CheckoutComAPMConfigurationService;
import com.checkout.hybris.core.currency.services.CheckoutComCurrencyService;
import com.checkout.hybris.core.merchant.services.CheckoutComMerchantConfigurationService;
import com.checkout.hybris.core.model.CheckoutComAPMConfigurationModel;
import com.checkout.hybris.core.model.CheckoutComFawryConfigurationModel;
import com.checkout.hybris.core.model.CheckoutComFawryPaymentInfoModel;
import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.core.payment.request.mappers.CheckoutComPaymentRequestStrategyMapper;
import com.checkout.hybris.core.payment.request.strategies.CheckoutComPaymentRequestStrategy;
import com.checkout.hybris.core.populators.payments.CheckoutComCartModelToPaymentL2AndL3Converter;
import com.checkout.hybris.core.url.services.CheckoutComUrlService;
import com.checkout.payments.AlternativePaymentSource;
import com.checkout.payments.PaymentRequest;
import com.checkout.payments.RequestSource;
import de.hybris.platform.cms2.servicelayer.services.CMSSiteService;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.CustomerModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.FAWRY;
import static com.google.common.base.Preconditions.checkArgument;

/**
 * specific {@link CheckoutComPaymentRequestStrategy} implementation for Fawry apm payments
 */
@SuppressWarnings("java:S107")
public class CheckoutComFawryPaymentRequestStrategy extends CheckoutComAbstractApmPaymentRequestStrategy {

    protected static final String MOBILE_NUMBER_KEY = "customer_mobile";
    protected static final String CUSTOMER_EMAIL_KEY = "customer_email";
    protected static final String PRODUCTS_PRODUCT_ID_KEY = "product_id";
    protected static final String PRODUCTS_QUANTITY_KEY = "quantity";
    protected static final String PRODUCTS_PRICE_KEY = "price";
    protected static final String DESCRIPTION_KEY = "description";
    protected static final String PRODUCTS_KEY = "products";

    protected final CheckoutComAPMConfigurationService checkoutComAPMConfigurationService;

    public CheckoutComFawryPaymentRequestStrategy(final CheckoutComUrlService checkoutComUrlService,
                                                  final CheckoutComPhoneNumberStrategy checkoutComPhoneNumberStrategy,
                                                  final CheckoutComCurrencyService checkoutComCurrencyService,
                                                  final CheckoutComPaymentRequestStrategyMapper checkoutComPaymentRequestStrategyMapper,
                                                  final CMSSiteService cmsSiteService,
                                                  final CheckoutComMerchantConfigurationService checkoutComMerchantConfigurationService,
                                                  final CheckoutComCartModelToPaymentL2AndL3Converter checkoutComCartModelToPaymentL2AndL3Converter,
                                                  final CheckoutComAPMConfigurationService checkoutComAPMConfigurationService) {
        super(checkoutComUrlService, checkoutComPhoneNumberStrategy, checkoutComCurrencyService,
              checkoutComPaymentRequestStrategyMapper, cmsSiteService, checkoutComMerchantConfigurationService,
              checkoutComCartModelToPaymentL2AndL3Converter);
        this.checkoutComAPMConfigurationService = checkoutComAPMConfigurationService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CheckoutComPaymentType getStrategyKey() {
        return FAWRY;
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

        source.put(MOBILE_NUMBER_KEY, ((CheckoutComFawryPaymentInfoModel) cart.getPaymentInfo()).getMobileNumber());
        source.put(CUSTOMER_EMAIL_KEY, customer.getContactEmail());
        source.put(DESCRIPTION_KEY, cmsSiteService.getCurrentSite().getName());
        source.put(PRODUCTS_KEY, populateProductsField(amount));

        return paymentRequest;
    }

    protected List<Map<String, Object>> populateProductsField(final Long amount) {
        final Map<String, Object> products = new HashMap<>();

        final Optional<CheckoutComAPMConfigurationModel> optionalFawryConfiguration =
                checkoutComAPMConfigurationService.getApmConfigurationByCode(
                        FAWRY.name());
        checkArgument(optionalFawryConfiguration.isPresent(), "Fawry configuration cannot be null");

        final CheckoutComFawryConfigurationModel fawryConfiguration =
                (CheckoutComFawryConfigurationModel) optionalFawryConfiguration.get();
        products.put(PRODUCTS_PRODUCT_ID_KEY, fawryConfiguration.getProductId());
        products.put(DESCRIPTION_KEY, fawryConfiguration.getProductDescription());
        products.put(PRODUCTS_QUANTITY_KEY, 1);
        products.put(PRODUCTS_PRICE_KEY, amount);

        return List.of(products);
    }
}
