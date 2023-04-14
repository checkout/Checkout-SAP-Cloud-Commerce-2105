package com.checkout.hybris.core.payment.request.strategies.impl;

import com.checkout.hybris.core.address.strategies.CheckoutComPhoneNumberStrategy;
import com.checkout.hybris.core.currency.services.CheckoutComCurrencyService;
import com.checkout.hybris.core.merchant.services.CheckoutComMerchantConfigurationService;
import com.checkout.hybris.core.model.CheckoutComAPMConfigurationModel;
import com.checkout.hybris.core.model.CheckoutComOxxoConfigurationModel;
import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.core.payment.oxxo.service.CheckoutComOxxoPaymentRequestService;
import com.checkout.hybris.core.payment.request.mappers.CheckoutComPaymentRequestStrategyMapper;
import com.checkout.hybris.core.payment.request.strategies.CheckoutComPaymentRequestStrategy;
import com.checkout.hybris.core.populators.payments.CheckoutComCartModelToPaymentL2AndL3Converter;
import com.checkout.hybris.core.url.services.CheckoutComUrlService;
import com.checkout.payments.AlternativePaymentSource;
import com.checkout.payments.PaymentRequest;
import com.checkout.payments.RequestSource;
import de.hybris.platform.cms2.servicelayer.services.CMSSiteService;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import de.hybris.platform.core.model.user.AddressModel;
import org.apache.commons.lang.StringUtils;

import java.util.Optional;

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.OXXO;
import static com.google.common.base.Preconditions.checkArgument;
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

/**
 * specific {@link CheckoutComPaymentRequestStrategy} implementation for Oxxo apm payments
 */
@SuppressWarnings("java:S107")
public class CheckoutComOxxoPaymentRequestStrategy extends CheckoutComAbstractApmPaymentRequestStrategy {

    protected static final String INTEGRATION_TYPE_KEY = "integration_type";
    protected static final String COUNTRY = "country";
    protected static final String DESCRIPTION_KEY = "description";
    protected static final String PAYER_KEY = "payer";

    protected final CheckoutComOxxoPaymentRequestService checkoutComOxxoPaymentRequestService;

    public CheckoutComOxxoPaymentRequestStrategy(final CheckoutComUrlService checkoutComUrlService,
                                                 final CheckoutComPhoneNumberStrategy checkoutComPhoneNumberStrategy,
                                                 final CheckoutComCurrencyService checkoutComCurrencyService,
                                                 final CheckoutComPaymentRequestStrategyMapper checkoutComPaymentRequestStrategyMapper,
                                                 final CMSSiteService cmsSiteService,
                                                 final CheckoutComMerchantConfigurationService checkoutComMerchantConfigurationService,
                                                 final CheckoutComCartModelToPaymentL2AndL3Converter checkoutComCartModelToPaymentL2AndL3Converter,
                                                 final CheckoutComOxxoPaymentRequestService checkoutComOxxoPaymentRequestService) {
        super(checkoutComUrlService, checkoutComPhoneNumberStrategy, checkoutComCurrencyService,
              checkoutComPaymentRequestStrategyMapper, cmsSiteService, checkoutComMerchantConfigurationService,
              checkoutComCartModelToPaymentL2AndL3Converter);
        this.checkoutComOxxoPaymentRequestService = checkoutComOxxoPaymentRequestService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CheckoutComPaymentType getStrategyKey() {
        return OXXO;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PaymentRequest<RequestSource> getRequestSourcePaymentRequest(final CartModel cart,
                                                                           final String currencyIsoCode, final Long amount) {
        final PaymentRequest<RequestSource> paymentRequest = super.getRequestSourcePaymentRequest(cart, currencyIsoCode, amount);
        final AlternativePaymentSource source = (AlternativePaymentSource) paymentRequest.getSource();

        source.put(PAYER_KEY, checkoutComOxxoPaymentRequestService.getPayerDto(cart));
        setIntegrationTypeAndDescription(source);
        setCountry(cart, source);

        return paymentRequest;
    }

    /**
     * Set country from billingAddress
     *
     * @param cart   the source cart
     * @param source the payment request
     */
    protected void setCountry(final CartModel cart, final AlternativePaymentSource source) {
        final PaymentInfoModel paymentInfo = cart.getPaymentInfo();

        validateParameterNotNull(paymentInfo, "paymentInfo cannot be null");

        final AddressModel billingAddress = paymentInfo.getBillingAddress();

        validateParameterNotNull(billingAddress, "billingAddress cannot be null");
        validateParameterNotNull(billingAddress.getCountry(), "billingAddress.country cannot be null");

        final String isocode = billingAddress.getCountry().getIsocode();

        checkArgument(StringUtils.isNotBlank(isocode), "billingAddress.country.isocode cannot be null");

        source.put(COUNTRY, isocode);
    }

    /**
     * Set integration type and description from checkoutComAPMConfigurationService
     *
     * @param source the payment request
     */
    protected void setIntegrationTypeAndDescription(final AlternativePaymentSource source) {
        final Optional<CheckoutComAPMConfigurationModel> optionalOxxoConfiguration = checkoutComOxxoPaymentRequestService.getOxxoApmConfiguration();

        checkArgument(optionalOxxoConfiguration.isPresent(), "Oxxo configuration cannot be null");

        final CheckoutComOxxoConfigurationModel oxxoConfiguration = (CheckoutComOxxoConfigurationModel) optionalOxxoConfiguration.get();
        source.put(INTEGRATION_TYPE_KEY, oxxoConfiguration.getIntegrationType().getCode());
        source.put(DESCRIPTION_KEY, oxxoConfiguration.getDescription());
    }
}
