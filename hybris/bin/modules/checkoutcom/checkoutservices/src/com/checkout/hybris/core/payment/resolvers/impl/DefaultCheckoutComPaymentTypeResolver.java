package com.checkout.hybris.core.payment.resolvers.impl;

import com.checkout.hybris.core.enums.MadaBin;
import com.checkout.hybris.core.model.CheckoutComAPMPaymentInfoModel;
import com.checkout.hybris.core.model.CheckoutComCreditCardPaymentInfoModel;
import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.core.payment.resolvers.CheckoutComPaymentTypeResolver;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import de.hybris.platform.enumeration.EnumerationService;

import java.util.List;

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.CARD;
import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.MADA;

/**
 * Default implementation of the {@link CheckoutComPaymentTypeResolver}
 */
public class DefaultCheckoutComPaymentTypeResolver implements CheckoutComPaymentTypeResolver {

    protected final EnumerationService enumerationService;

    public DefaultCheckoutComPaymentTypeResolver(final EnumerationService enumerationService) {
        this.enumerationService = enumerationService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CheckoutComPaymentType resolvePaymentType(final PaymentInfoModel paymentInfo) {

        if (paymentInfo instanceof CheckoutComCreditCardPaymentInfoModel) {
            final CheckoutComCreditCardPaymentInfoModel checkoutComCreditCardPaymentInfoModel = (CheckoutComCreditCardPaymentInfoModel) paymentInfo;
            if (isMadaCard(checkoutComCreditCardPaymentInfoModel.getCardBin())) {
                return MADA;
            } else {
                return CARD;
            }
        } else if (paymentInfo instanceof CheckoutComAPMPaymentInfoModel) {
            return CheckoutComPaymentType.valueOf(((CheckoutComAPMPaymentInfoModel) paymentInfo).getType());
        } else {
            throw new IllegalArgumentException(String.format("Resolver called with unsupported paymentInfo with code [%s] and type [%s].", paymentInfo.getCode(), paymentInfo.getItemtype()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CheckoutComPaymentType resolvePaymentMethod(final String paymentMethod) {
        return CheckoutComPaymentType.valueOf(paymentMethod);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isMadaCard(final String cardBin) {
        final List<MadaBin> madaBins = enumerationService.getEnumerationValues(MadaBin.class);
        return madaBins.stream().anyMatch(madaBin -> madaBin.getCode().equalsIgnoreCase(cardBin));
    }
}
