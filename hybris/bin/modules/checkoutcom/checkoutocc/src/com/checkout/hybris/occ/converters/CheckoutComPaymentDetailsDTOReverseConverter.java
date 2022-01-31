package com.checkout.hybris.occ.converters;

import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import de.hybris.platform.commercewebservicescommons.dto.order.PaymentDetailsWsDTO;

/**
 * Converts a paymentDetailsWsDTO into a specific payment info data based on the payment type
 */
public interface CheckoutComPaymentDetailsDTOReverseConverter {

    /**
     * Converts the given paymentDetailsWsDTO into a specific payment data object based on the payment type.
     * If there is no converter for the payment type, will use the default converter
     *
     * @param paymentDetailsWsDTO the payment details WS DTO
     * @param paymentType         the payment type
     * @return a specific payment info data for the type
     */
    Object convertPaymentDetailsWsDTO(PaymentDetailsWsDTO paymentDetailsWsDTO, CheckoutComPaymentType paymentType);
}
