import { PaymentDetails, TranslationService } from '@spartacus/core';

/**
 * give payment details translation for an order
 */
export const getPaymentDetailsLineTranslation = (translation: TranslationService, paymentDetails: PaymentDetails, paymentType: string) => {
  let paymentDetailsTranslation;
  if (paymentDetails.expiryMonth) {
    paymentDetailsTranslation = translation.translate('paymentCard.expires', {
      month: paymentDetails.expiryMonth,
      year: paymentDetails.expiryYear,
    });
  } else {
    paymentDetailsTranslation =
      translation.translate(
        'paymentCard.apm', {
          apm: paymentType
        }
      );
  }
  return paymentDetailsTranslation;
};
