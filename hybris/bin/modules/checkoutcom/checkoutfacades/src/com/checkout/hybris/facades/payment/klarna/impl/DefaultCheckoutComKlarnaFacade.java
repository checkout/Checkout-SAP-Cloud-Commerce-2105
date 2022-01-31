package com.checkout.hybris.facades.payment.klarna.impl;

import com.checkout.hybris.core.klarna.session.request.KlarnaSessionRequestDto;
import com.checkout.hybris.core.klarna.session.response.KlarnaPaymentMethodCategoryDto;
import com.checkout.hybris.core.klarna.session.response.KlarnaSessionResponseDto;
import com.checkout.hybris.core.merchant.services.CheckoutComMerchantConfigurationService;
import com.checkout.hybris.core.model.CheckoutComKlarnaConfigurationModel;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentIntegrationService;
import com.checkout.hybris.facades.beans.KlarnaClientTokenData;
import com.checkout.hybris.facades.payment.klarna.CheckoutComKlarnaFacade;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.order.CartService;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link CheckoutComKlarnaFacade}
 */
public class DefaultCheckoutComKlarnaFacade implements CheckoutComKlarnaFacade {

    protected final CheckoutComPaymentIntegrationService checkoutComPaymentIntegrationService;
    protected final Converter<CartModel, KlarnaSessionRequestDto> checkoutComKlarnaSessionRequestDtoConverter;
    protected final CartService cartService;
    protected final CheckoutComMerchantConfigurationService checkoutComMerchantConfigurationService;

    public DefaultCheckoutComKlarnaFacade(final CheckoutComPaymentIntegrationService checkoutComPaymentIntegrationService,
                                          final Converter<CartModel, KlarnaSessionRequestDto> checkoutComKlarnaSessionRequestDtoConverter,
                                          final CartService cartService,
                                          final CheckoutComMerchantConfigurationService checkoutComMerchantConfigurationService) {
        this.checkoutComPaymentIntegrationService = checkoutComPaymentIntegrationService;
        this.checkoutComKlarnaSessionRequestDtoConverter = checkoutComKlarnaSessionRequestDtoConverter;
        this.cartService = cartService;
        this.checkoutComMerchantConfigurationService = checkoutComMerchantConfigurationService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KlarnaClientTokenData getKlarnaClientToken() throws IllegalArgumentException, ExecutionException {
        if (cartService.hasSessionCart()) {
            final CartModel sessionCart = cartService.getSessionCart();

            final KlarnaClientTokenData klarnaClientTokenData = new KlarnaClientTokenData();
            final KlarnaSessionRequestDto klarnaSessionRequestDto = checkoutComKlarnaSessionRequestDtoConverter.convert(sessionCart);

            populateKlarnaTokenData(klarnaClientTokenData, checkoutComPaymentIntegrationService.createKlarnaSession(klarnaSessionRequestDto));
            return klarnaClientTokenData;
        }
        throw new IllegalArgumentException("No session cart found");
    }

    /**
     * Populates the data attribute from response and configuration
     *
     * @param klarnaClientTokenData the response to populate
     * @param klarnaSessionResponse the checkout.com create session response
     */
    protected void populateKlarnaTokenData(final KlarnaClientTokenData klarnaClientTokenData, final KlarnaSessionResponseDto klarnaSessionResponse) {
        final CheckoutComKlarnaConfigurationModel klarnaConfiguration = checkoutComMerchantConfigurationService.getKlarnaConfiguration();
        klarnaClientTokenData.setClientToken(klarnaSessionResponse.getClientToken());
        klarnaClientTokenData.setSuccess(Boolean.TRUE);
        klarnaClientTokenData.setInstanceId(klarnaConfiguration.getInstanceId());

        final Set<String> paymentMethodCategories = klarnaSessionResponse.getPaymentMethodCategories().stream()
                .map(KlarnaPaymentMethodCategoryDto::getIdentifier)
                .collect(Collectors.toSet());
        klarnaClientTokenData.setPaymentMethodCategories(paymentMethodCategories);
    }
}
