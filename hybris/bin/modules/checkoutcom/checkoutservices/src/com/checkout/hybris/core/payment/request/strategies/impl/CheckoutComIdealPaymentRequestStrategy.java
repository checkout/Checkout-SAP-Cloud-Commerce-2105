package com.checkout.hybris.core.payment.request.strategies.impl;

import com.checkout.hybris.core.address.strategies.CheckoutComPhoneNumberStrategy;
import com.checkout.hybris.core.currency.services.CheckoutComCurrencyService;
import com.checkout.hybris.core.merchant.services.CheckoutComMerchantConfigurationService;
import com.checkout.hybris.core.model.CheckoutComIdealPaymentInfoModel;
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
import org.apache.commons.lang.StringUtils;

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.IDEAL;
import static com.google.common.base.Preconditions.checkArgument;
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

/**
 * specific {@link CheckoutComPaymentRequestStrategy} implementation for Ideal apm payments
 */
public class CheckoutComIdealPaymentRequestStrategy extends CheckoutComAbstractApmPaymentRequestStrategy {

    protected static final String BIC_KEY = "bic";
    protected static final String DESCRIPTION_KEY = "description";

    public CheckoutComIdealPaymentRequestStrategy(final CheckoutComUrlService checkoutComUrlService,
                                                  final CheckoutComPhoneNumberStrategy checkoutComPhoneNumberStrategy,
                                                  final CheckoutComCurrencyService checkoutComCurrencyService,
                                                  final CheckoutComPaymentRequestStrategyMapper checkoutComPaymentRequestStrategyMapper,
                                                  final CMSSiteService cmsSiteService,
                                                  final CheckoutComMerchantConfigurationService checkoutComMerchantConfigurationService,
                                                  final CheckoutComCartModelToPaymentL2AndL3Converter checkoutComCartModelToPaymentL2AndL3Converter) {
        super(checkoutComUrlService, checkoutComPhoneNumberStrategy, checkoutComCurrencyService,
              checkoutComPaymentRequestStrategyMapper, cmsSiteService, checkoutComMerchantConfigurationService,
              checkoutComCartModelToPaymentL2AndL3Converter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CheckoutComPaymentType getStrategyKey() {
        return IDEAL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PaymentRequest<RequestSource> getRequestSourcePaymentRequest(final CartModel cart,
                                                                           final String currencyIsoCode, final Long amount) {
        final PaymentRequest<RequestSource> paymentRequest = super.getRequestSourcePaymentRequest(cart, currencyIsoCode, amount);
        final AlternativePaymentSource source = (AlternativePaymentSource) paymentRequest.getSource();

        validateParameterNotNull(cart, "Cart model cannot be null");
        checkArgument(StringUtils.isNotBlank(cart.getCheckoutComPaymentReference()), "Payment reference can not be blank or null");

        final CheckoutComIdealPaymentInfoModel idealPaymentInfo = (CheckoutComIdealPaymentInfoModel) cart.getPaymentInfo();
        source.put(DESCRIPTION_KEY, formatAndValidatePaymentReference(cart.getCheckoutComPaymentReference()));
        source.put(BIC_KEY, idealPaymentInfo.getBic());

        return paymentRequest;
    }

    /**
     * Formats and validates the payment reference for ideal payment request
     * Gets rid of the special characters and verifies that the length is less than 35 characters
     *
     * @param paymentReference to format
     * @return formatted payment reference with valid length
     */
    protected String formatAndValidatePaymentReference(final String paymentReference) {
        final String formattedPaymentReference = paymentReference.replaceAll("[^a-zA-Z0-9]", "");

        checkArgument(formattedPaymentReference.length() <= 35, "Formatted Payment reference can not be more than 35 characters");
        return formattedPaymentReference;
    }
}
