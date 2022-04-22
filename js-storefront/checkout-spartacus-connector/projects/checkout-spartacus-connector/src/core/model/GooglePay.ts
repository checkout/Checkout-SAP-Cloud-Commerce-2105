import { Order } from '@spartacus/core';

export interface GooglePayMerchantConfiguration {
  baseCardPaymentMethod: {
    parameters: {
      allowedAuthMethods: string[];
      allowedCardNetworks: string[];
      billingAddressParameters: {
        format: string
      },
      billingAddressRequired: boolean
    },
    type: string,
  };
  clientSettings: {
    environment: string
  };
  gateway: string;
  gatewayMerchantId: string;
  merchantName: string;
  merchantId: string;
  transactionInfo: {
    currencyCode: string,
    totalPrice: string,
    totalPriceStatus: string,
  };
}
export interface GooglePayPaymentRequest {
  apiVersion: number;
  apiVersionMinor: number;
  paymentMethodData: {
    type?: string;
    description?: string;
    info: {
      billingAddress: any;
    };
    tokenizationData: {
      token: string;
    };
  };
}

export interface PlaceOrderResponse {
  redirectUrl: any;
  status: string;
  orderData: Order;
}
