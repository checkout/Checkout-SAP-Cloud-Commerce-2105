export const checkoutComTranslations = {
  en: {
    payment: {
      paymentForm: {
        saveCard: 'Save this card',
        validationErrors: {
          cardNumberInvalid: 'Please enter a valid card number',
          expiryDateInvalid: 'Please enter a valid expiry date',
          cvvInvalid: 'Please enter a valid security code',
        },

        oxxo: {
          document: 'Document ID',
          pattern: 'Document ID must have 18 alphanumeric characters',
          required: 'This field is required',
        },

        googlepay: {
          authorisationFailed: 'Your GooglePay payment could not be processed',
          help:
            'When you press the Google Pay button you can pay your order in an instance.',
          setDeliveryInfoFailed: 'Your GooglePay address is not valid',
        },

        applePay: {
          title: 'Apple Pay',
          introduction:
            'Pay directly with Apple Pay and skip filling the payment details.',
          merchantValidationFailed:
            'Your Apple Pay Payment could not be processed. Please contact customer service at 555....',
          authorisationFailed:
            'Your payment could be authorized. Please try another time.',
          cancelled: 'Your payment was cancelled by Apple Pay',
          setDeliveryAdddresFailed: 'An error occurred while processing your delivery address',
          setDeliveryMethodFailed: 'An error occurred while processing your selected delivery method',
        },

        klarna: {
          paymentMethodCategory: {
            payNow: 'Pay Now',
            payLater: 'Pay Later',
            payOverTime: 'Slice it',
          },
          initializationFailed: 'Klarna initialization failed',
          countryIsRequired: 'Klarna can not be initialized - country is not selected',
        },

        fawry: {
          mobileNumber: 'Mobile Number',
          pattern: 'The mobile number must have length 11 digits and must not include the country code',
          required: 'This field is required',
        },

        frames: {
          tokenizationFailed: 'Can\'t transfer payment details, please try again',

          placeholders: {
            cardNumberPlaceholder: 'Card Number',
            expiryMonthPlaceholder: 'MM',
            expiryYearPlaceholder: 'YY',
            cvvPlaceholder: 'CVV'
          }
        },

        merchantKeyFailed: 'Payment with debit/credit card is not available at this moment. Please try another payment option',

        apmChanged: 'The previously selected payment option is not available, please select another one.'
      },
      paymentPdp: {
        payDirectly: 'Pay Directly',
        or: 'OR',
      },
      paymentCard: {
        apm: 'Payment with {{ apm }}'
      },

      sepaForm: {
        firstName: {
          label: 'First name',
          placeholder: 'Enter first name',
        },
        lastName: {
          label: 'Last name',
          placeholder: 'Enter last name',
        },
        accountIban: {
          label: 'IBAN',
          placeholder: 'Enter IBAN',
        },
        addressLine1: {
          label: 'Address line 1',
          placeholder: 'Enter Address line 1',
        },
        addressLine2: {
          label: 'Address line 2',
          placeholder: 'Enter Address line 2',
        },
        city: {
          label: 'City',
          placeholder: 'Enter City',
        },
        postalCode: {
          label: 'Postal Code',
          placeholder: 'Enter Postal Code',
          required: 'Please enter a valid postal code',
          maxlength: 'Please enter a valid postal code'
        },
        country: {
          label: 'Country',
          placeholder: 'Enter Country',
        },
        paymentType: {
          label: 'Payment Type',
          placeholder: 'Select a payment type',
        },
        paymentTypes: {
          single: 'One-off payment',
          recurring: 'Recurring payment',
        }
      },

      idealForm: {
        bic: {
          label: 'BIC',
          placeholder: 'Enter your bank\'s BIC number',
          pattern: 'BIC field is mandatory and must 8 or 11 characters long',
        }
      }
    },
    checkout: {
      checkoutReview: {
        threeDsChallengeFailed: 'Failed to verify your payment details',
        tokenizationFailed: 'Please check your payment details',
        initialPaymentRequestFailed: 'Failed to process your payment',
        challengeFailed: 'Your payment was refused. Please try again with a different card',
        paymentAuthorizationError: 'Payment authorization was not successful',
      },

      checkoutOrderConfirmation: {
        benefitPay: {
          scanToComplete: 'Please scan the barcode to complete the order',
        }
      }
    },
    common: {
      formErrors: {
        pattern: 'Input format is wrong'
      },
      httpHandlers: {
        validationErrors: {
          invalid: {
            cardType: {
              code: 'The card type that was provided is currently not supported'
            }
          }
        }
      }
    },
  },
};

export const checkoutComTranslationChunkConfig = {
  payment: ['paymentForm',
    'paymentMethods',
    'paymentCard',
    'paymentTypes',
    'sepaForm',
    'idealForm',
    'paymentPdp',
  ]
};
