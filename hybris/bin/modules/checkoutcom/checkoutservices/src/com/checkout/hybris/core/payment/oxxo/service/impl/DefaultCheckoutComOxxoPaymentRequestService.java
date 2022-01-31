package com.checkout.hybris.core.payment.oxxo.service.impl;

import com.checkout.hybris.core.apm.services.CheckoutComAPMConfigurationService;
import com.checkout.hybris.core.model.CheckoutComAPMConfigurationModel;
import com.checkout.hybris.core.oxxo.session.request.OxxoPayerRequestDto;
import com.checkout.hybris.core.payment.oxxo.service.CheckoutComOxxoPaymentRequestService;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import java.util.Optional;

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.OXXO;

/**
 * Default implementation of {@link CheckoutComOxxoPaymentRequestService}
 */
public class DefaultCheckoutComOxxoPaymentRequestService implements CheckoutComOxxoPaymentRequestService {

    protected final CheckoutComAPMConfigurationService checkoutComAPMConfigurationService;
    protected final Converter<CartModel, OxxoPayerRequestDto> checkoutComOxxoRequestPayerDtoConverter;

    public DefaultCheckoutComOxxoPaymentRequestService(final CheckoutComAPMConfigurationService checkoutComAPMConfigurationService,
                                                       final Converter<CartModel, OxxoPayerRequestDto> checkoutComOxxoRequestPayerDtoConverter) {
        this.checkoutComAPMConfigurationService = checkoutComAPMConfigurationService;
        this.checkoutComOxxoRequestPayerDtoConverter = checkoutComOxxoRequestPayerDtoConverter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<CheckoutComAPMConfigurationModel> getOxxoApmConfiguration() {
        return checkoutComAPMConfigurationService.getApmConfigurationByCode(OXXO.name());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OxxoPayerRequestDto getPayerDto(final CartModel cartModel) {
        return checkoutComOxxoRequestPayerDtoConverter.convert(cartModel);
    }
}
