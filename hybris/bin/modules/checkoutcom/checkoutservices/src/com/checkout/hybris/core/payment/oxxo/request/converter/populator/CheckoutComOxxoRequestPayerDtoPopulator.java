package com.checkout.hybris.core.payment.oxxo.request.converter.populator;

import com.checkout.hybris.core.model.CheckoutComOxxoPaymentInfoModel;
import com.checkout.hybris.core.oxxo.session.request.OxxoPayerRequestDto;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.springframework.util.Assert;

import java.util.Optional;

/**
 * Populates the payer of {@link OxxoPayerRequestDto} from the cart model
 */
public class CheckoutComOxxoRequestPayerDtoPopulator implements Populator<CartModel, OxxoPayerRequestDto> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void populate(final CartModel source, final OxxoPayerRequestDto target) throws ConversionException {
        Assert.notNull(source, "CartModel cannot be null.");
        Assert.notNull(target, "OxxoPayerRequestDto cannot be null.");

        final CustomerModel customerModel = Optional.ofNullable(source.getUser())
                .filter(CustomerModel.class::isInstance)
                .map(CustomerModel.class::cast)
                .orElseThrow(IllegalArgumentException::new);

        final CheckoutComOxxoPaymentInfoModel checkoutComOxxoPaymentInfoModel = Optional.ofNullable(source.getPaymentInfo())
                .filter(CheckoutComOxxoPaymentInfoModel.class::isInstance)
                .map(CheckoutComOxxoPaymentInfoModel.class::cast)
                .orElseThrow(IllegalArgumentException::new);

        target.setDocument(checkoutComOxxoPaymentInfoModel.getDocument());
        target.setEmail(customerModel.getContactEmail());
        target.setName(customerModel.getName());
    }
}
