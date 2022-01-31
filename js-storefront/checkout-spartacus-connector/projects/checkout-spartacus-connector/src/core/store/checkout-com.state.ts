import { Address, PaymentDetails, Order, HttpErrorModel } from '@spartacus/core';
import { CheckoutComRedirect, KlarnaInitParams } from '../interfaces';
import { HttpErrorResponse } from '@angular/common/http';

import { ApmData } from '../model/ApmData';
import { ApplePayAuthorization, ApplePayPaymentRequest } from '../model/ApplePay';

export interface CheckoutComState {
  occMerchantKey?: string;
  paymentDetails?: PaymentDetails;
  paymentDetailsError?: any;
  paymentAddress?: Address | HttpErrorResponse;
  placeOrder?: CheckoutComOrderResult;
  availableApms?: ApmData[];
  selectedApm: ApmData;
  googlePayMerchantConfiguration?: any;

  apmLoading?: boolean;

  klarnaInitParams?: KlarnaInitParams;

  applePayPaymentRequest?: ApplePayPaymentRequest;
  applePayMerchantSession?: any;
  applePayAuthorization?: ApplePayAuthorization;
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
