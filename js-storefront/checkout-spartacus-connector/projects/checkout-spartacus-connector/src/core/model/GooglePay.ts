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
    merchantInfo?: {
      merchantName: string;
      merchantId: string;
    },
    paymentDataCallbacks?: {
      onPaymentAuthorized?(payload: GooglePayPaymentRequest): Promise<any>;
      onPaymentDataChanged(payload: IntermediatePaymentData): Promise<any>;
    }
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
  shippingAddress?: any;
  email?: string;
}

export interface PlaceOrderResponse {
  redirectUrl: any;
  status: string;
  orderData: Order;
}

export interface IntermediatePaymentData {
  callbackTrigger?: CallbackTrigger;
  shippingAddress?: IntermediateAddress;
  shippingOption?: SelectionOptionData;
}

export enum CallbackTrigger {
  INITIALIZE = 'INITIALIZE',
  SHIPPING_ADDRESS = 'SHIPPING_ADDRESS',
  SHIPPING_OPTION = 'SHIPPING_OPTION'
}

export interface IntermediateAddress {
  administrativeArea: string;
  countryCode: string;
  locality: string;
  postalCode: string;
}

export interface SelectionOptionData {
  id: string;
}

export interface PaymentDataRequestUpdate {
  newTransactionInfo?: any;
  newShippingOptionParameters?: ShippingOptionParameters
  error?: PaymentDataError
}

export interface PaymentAuthorizationResult {
  transactionState: string;
}

export interface ShippingOptionParameters {
  shippingOptions: SelectionOption[];
  defaultSelectedOptionId?: string;
}

export interface SelectionOption {
  id: string;
  label: string;
  description?: string;
}

export interface PaymentDataError {
  reason: string;
  message: string;
  intent: string;
}
