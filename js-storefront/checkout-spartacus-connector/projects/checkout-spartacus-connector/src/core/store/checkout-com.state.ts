import { Address, HttpErrorModel, Order, PaymentDetails } from '@spartacus/core';
import { CheckoutComRedirect, KlarnaInitParams } from '../interfaces';
import { HttpErrorResponse } from '@angular/common/http';

import { ApmData } from '../model/ApmData';
import {
  ApplePayAuthorization,
  ApplePayPaymentRequest,
  ApplePayShippingContactUpdate,
  ApplePayShippingMethodUpdate
} from '../model/ApplePay';
import {
  GooglePayPaymentRequest,
  IntermediatePaymentData,
  PaymentAuthorizationResult,
  PaymentDataRequestUpdate
} from '../model/GooglePay';

export interface CheckoutComState {
  occMerchantKey?: string;
  paymentDetails?: PaymentDetails;
  paymentDetailsError?: any;
  paymentAddress?: Address | HttpErrorResponse;
  placeOrder?: CheckoutComOrderResult;
  availableApms?: ApmData[];
  selectedApm: ApmData;
  googlePayMerchantConfiguration?: any;
  googlePayAuth?: GooglePayPaymentRequest;
  googlePayPaymentDataUpdate?: PaymentDataRequestUpdate;
  googlePayPaymentAuthorizationResult?: PaymentAuthorizationResult;

  apmLoading?: boolean;

  klarnaInitParams?: KlarnaInitParams;

  applePayPaymentRequest?: ApplePayPaymentRequest;
  applePayMerchantSession?: any;
  applePayAuthorization?: ApplePayAuthorization;
  applePayShippingContactUpdate?: ApplePayShippingContactUpdate;
  applePayShippingMethodUpdate?: ApplePayShippingMethodUpdate;
}

export const CHECKOUT_COM_FEATURE = 'CheckoutCom';

export interface StateWithCheckoutCom {
  [CHECKOUT_COM_FEATURE]: CheckoutComState;
}

export class CheckoutComOrderResult {
  successful: boolean = null;
  httpError?: HttpErrorModel = null;
  order?: Order = null;
  redirect?: CheckoutComRedirect = null;
}
