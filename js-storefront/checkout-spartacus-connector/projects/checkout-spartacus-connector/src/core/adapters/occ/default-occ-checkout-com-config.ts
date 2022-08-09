import { OccConfig } from '@spartacus/core';
import { OccEndpoint } from '@spartacus/core';

declare module '@spartacus/core' {
  interface OccEndpoints {
    merchantKey?: string | OccEndpoint;
    setDeliveryAddress?: string | OccEndpoint;
    setPaymentAddress?: string | OccEndpoint;
    setPaymentDetails?: string | OccEndpoint;
    setApmPaymentDetails?: string | OccEndpoint;
    directPlaceOrder?: string | OccEndpoint;
    redirectPlaceOrder?: string | OccEndpoint;
    availableApms?: string | OccEndpoint;
    googlePayMerchantConfig?: string | OccEndpoint;
    googlePayPlaceOrder?: string | OccEndpoint;
    klarnaClientToken?: string | OccEndpoint;
    applePayPaymentRequest?: string | OccEndpoint;
    applePayRequestSession?: string | OccEndpoint;
    applePayPlaceOrder?: string | OccEndpoint;
    applePaySetDeliveryAddress?: string | OccEndpoint;
    applePaySetDeliveryMethod?: string | OccEndpoint;
    googlePaySetDeliveryInfo?: string | OccEndpoint;
  }
}

export const defaultOccCheckoutComConfig: OccConfig = {
  backend: {
    occ: {
      endpoints: {
        setDeliveryAddress: 'users/${userId}/carts/${cartId}/addresses/checkoutcomdeliverypayment',

        merchantKey: 'merchantKey',

        setPaymentAddress: 'users/${userId}/carts/${cartId}/checkoutoccbillingaddress',

        setPaymentDetails: 'users/${userId}/carts/${cartId}/checkoutcompaymentdetails',
        setApmPaymentDetails: 'users/${userId}/carts/${cartId}/checkoutcomapmpaymentdetails',

        directPlaceOrder: 'users/${userId}/carts/${cartId}/direct-place-order',
        redirectPlaceOrder: 'users/${userId}/carts/${cartId}/redirect-place-order',

        availableApms: 'users/${userId}/carts/${cartId}/apm/available',

        googlePayMerchantConfig: 'users/${userId}/carts/${cartId}/google/merchant-configuration',
        googlePayPlaceOrder: 'users/${userId}/carts/${cartId}/google/placeOrder',
        googlePaySetDeliveryInfo: 'users/${userId}/carts/${cartId}/google/deliveryInfo',

        klarnaClientToken: 'users/${userId}/carts/${cartId}/klarna/clientToken',

        applePayPaymentRequest: 'users/${userId}/carts/${cartId}/applepay/paymentRequest',
        applePayRequestSession: 'users/${userId}/carts/${cartId}/applepay/requestSession',
        applePayPlaceOrder: 'users/${userId}/carts/${cartId}/applepay/placeOrder',
        applePaySetDeliveryAddress: 'users/${userId}/carts/${cartId}/applepay/deliveryAddress',
        applePaySetDeliveryMethod: 'users/${userId}/carts/${cartId}/applepay/deliveryMethod',
      },
    },
  },
};
