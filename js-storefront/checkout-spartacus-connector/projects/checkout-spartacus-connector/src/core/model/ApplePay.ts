import {Order} from '@spartacus/core';

export interface ApplePayPaymentRequest {
  currencyCode: string;
  countryCode: string;
  supportedNetworks: string[];
  merchantCapabilities: string[];
  total: {
    type: string;
    label: string;
    amount: string;
  };
  requiredBillingContactFields: string[];
}

export interface ApplePayAuthorization {
  status: string;
  orderData: Order;
}
