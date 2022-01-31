package com.checkout.hybris.core.payment.oxxo.service;

import com.checkout.hybris.core.model.CheckoutComAPMConfigurationModel;
import com.checkout.hybris.core.oxxo.session.request.OxxoPayerRequestDto;
import de.hybris.platform.core.model.order.CartModel;

import java.util.Optional;

/**
 * Used to Oxxo APM configuration and payer
 */
public interface CheckoutComOxxoPaymentRequestService {

    /**
     * Finds the apm configuration for oxxo
     *
     * @return an optional of {@link CheckoutComAPMConfigurationModel} for oxxo, optional empty if not found
     */
    Optional<CheckoutComAPMConfigurationModel> getOxxoApmConfiguration();

    /**
     * Return Oxxo payer data from given {@link CartModel}
     *
     * @param cartModel the cart model
     * @return OxxoPayerRequestDto
     */
    OxxoPayerRequestDto getPayerDto(CartModel cartModel);
}
