package com.checkout.hybris.core.payment.resolvers;

import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;

/**
 * Resolves the payment type base on the payment info or method
 */
public interface CheckoutComPaymentTypeResolver {

    /**
     * Gets the payment type based on the payment info parameter
     *
     * @param paymentInfo the payment info model
     * @return the payment type
     */
    CheckoutComPaymentType resolvePaymentType(PaymentInfoModel paymentInfo);

    /**
     * Gets the payment type based on the payment method
     *
     * @param paymentMethod the payment method
     * @return the payment type
     */
    CheckoutComPaymentType resolvePaymentMethod(String paymentMethod);

    /**
     * Returns true if the card bin of the payment info matches any value in the MadaBin enumeration, false otherwise.
     *
     * @param cardBin card bin number
     * @return true if is a mada card, false otherwise
     */
    boolean isMadaCard(String cardBin);
}
