import { Action } from '@ngrx/store';
import { Address, HttpErrorModel, Order, PaymentDetails, PROCESS_FEATURE, StateUtils } from '@spartacus/core';
import { ApmPaymentDetails, CheckoutComPaymentDetails } from '../../storefrontlib/interfaces';
import { CheckoutComRedirect, KlarnaInitParams } from '../interfaces';
import { HttpErrorResponse } from '@angular/common/http';
import { ApmData } from '../model/ApmData';
import {
  GooglePayMerchantConfiguration,
  IntermediatePaymentData,
  PaymentAuthorizationResult,
  PaymentDataRequestUpdate,
  PlaceOrderResponse
} from '../model/GooglePay';
import {
  ApplePayAuthorization,
  ApplePayPaymentContact,
  ApplePayPaymentRequest,
  ApplePayShippingContactUpdate, ApplePayShippingMethod, ApplePayShippingMethodUpdate
} from '../model/ApplePay';
import { PLACED_ORDER_PROCESS_ID } from '@spartacus/checkout/core';

export const SET_OCC_MERCHANT_KEY = 'Requesting OCC merchant key';
export const SET_OCC_MERCHANT_KEY_FAIL = 'Request OCC merchant key FAIL';
export const SET_OCC_MERCHANT_KEY_SUCCESS = 'Request OCC merchant key from SUCCESS';

export const SET_PAYMENT_ADDR = 'Setting Payment Address';
export const SET_PAYMENT_ADDR_FAIL = 'Set Payment Address FAIL';
export const SET_PAYMENT_ADDR_SUCCESS = 'Set Payment Address SUCCESS';

export const CREATE_PAYMENT_DETAILS = 'Creating Payment Details';
export const CREATE_PAYMENT_DETAILS_FAIL = 'Create Payment Details FAIL';
export const CREATE_PAYMENT_DETAILS_SUCCESS = 'Create Payment Details SUCCESS';
export const CREATE_APM_PAYMENT_DETAILS = 'Creating APM Payment Details';

export const PLACE_ORDER = '[CheckoutCom] Placing Order';
export const PLACE_ORDER_SUCCESS = '[CheckoutCom] Place Order SUCCESS';
export const PLACE_ORDER_REDIRECT = '[CheckoutCom] Place Order REDIRECT';
export const PLACE_ORDER_FAIL = '[CheckoutCom] Place Order FAIL';

export const REDIRECT_PLACE_ORDER_AUTHORIZE = '[CheckoutCom] Redirect Place Order Authorize';
export const REDIRECT_PLACE_ORDER_AUTHORIZE_FAIL = '[CheckoutCom] Redirect Place Order Authorize FAIL';
export const REDIRECT_PLACE_ORDER_AUTHORIZE_SUCCESS = '[CheckoutCom] Redirect Place Order Authorize SUCCESS';

export const REQUEST_AVAILABLE_APMS = 'Request Available Apms';
export const REQUEST_AVAILABLE_APMS_FAIL = 'Request Available Apms FAIL';
export const REQUEST_AVAILABLE_APMS_SUCCESS = 'Request Available Apms SUCCESS';

export const SET_SELECTED_APM = 'Set Selected APM';

export const GET_CONFIG_GOOGLE_PAY = '[GooglePay] Get Configuration';
export const GET_CONFIG_GOOGLE_PAY_FAIL = '[GooglePay] Get Configuration Fail';
export const GET_CONFIG_GOOGLE_PAY_SUCCESS =
  '[GooglePay] Get Configuration Success';

export const AUTHORISE_GOOGLE_PAY_PAYMENT = '[GooglePay] Authorise payment';
export const AUTHORISE_GOOGLE_PAY_PAYMENT_FAIL =
  '[GooglePay] Authorise payment Fail';
export const AUTHORISE_GOOGLE_PAY_PAYMENT_SUCCESS =
  '[GooglePay] Authorise payment Success';

export const SET_KLARNA_INIT_PARAMS = 'Setting Klarna Init Params';
export const SET_KLARNA_INIT_PARAMS_SUCCESS = 'Set Klarna Init Params SUCCESS';
export const SET_KLARNA_INIT_PARAMS_FAIL = 'Set Klarna Init Params FAIL';

export const REQUEST_APPLE_PAY_PAYMENT_REQUEST =
  '[ApplePay] Request Payment Request';
export const REQUEST_APPLE_PAY_PAYMENT_REQUEST_FAIL =
  '[ApplePay] Request Payment Request Fail';
export const REQUEST_APPLE_PAY_PAYMENT_REQUEST_SUCCESS =
  '[ApplePay] Request Payment Request Success';

export const START_APPLE_PAY_SESSION = '[ApplePay] Start session';

export const VALIDATE_APPLE_PAY_MERCHANT = '[ApplePay] Validate Merchant';
export const VALIDATE_APPLE_PAY_MERCHANT_FAIL =
  '[ApplePay] Validate Merchant Fail';
export const VALIDATE_APPLE_PAY_MERCHANT_SUCCESS =
  '[ApplePay] Validate Merchant Success';

export const AUTHORISE_APPLE_PAY_PAYMENT = '[ApplePay] Authorise payment';
export const AUTHORISE_APPLE_PAY_PAYMENT_FAIL =
  '[ApplePay] Authorise payment Fail';
export const AUTHORISE_APPLE_PAY_PAYMENT_SUCCESS =
  '[ApplePay] Authorise payment Success';

export const SELECT_APPLEPAY_DELIVERY_ADDRESS = '[ApplePay] Select Delivery Address';
export const SELECT_APPLEPAY_DELIVERY_ADDRESS_FAIL = '[ApplePay] Select Delivery Address Fail';
export const SELECT_APPLEPAY_DELIVERY_ADDRESS_SUCCESS = '[ApplePay] Select Delivery Address Success';

export const SELECT_APPLEPAY_DELIVERY_METHOD = '[ApplePay] Select Delivery Method';
export const SELECT_APPLEPAY_DELIVERY_METHOD_FAIL = '[ApplePay] Select Delivery Method Fail';
export const SELECT_APPLEPAY_DELIVERY_METHOD_SUCCESS = '[ApplePay] Select Delivery Method Success';

export const GET_GOOGLE_PAY_PAYMENT_UPDATE = '[GooglePay] Update Payment Data';
export const GET_GOOGLE_PAY_PAYMENT_UPDATE_FAIL = '[GooglePay] Update Payment Data Fail';
export const GET_GOOGLE_PAY_PAYMENT_UPDATE_SUCCESS = '[GooglePay] Update Payment Data Success';
export const GET_GOOGLE_PAY_PAYMENT_AUTHORISE_SUCCESS = '[GooglePay] Authorise Payment Data Success';
export const GET_GOOGLE_PAY_PAYMENT_AUTHORISE_FAIL = '[GooglePay] Authorise Payment Data Fail';

export class SetOccMerchantKey implements Action {
  readonly type = SET_OCC_MERCHANT_KEY;

  constructor(public payload: {userId: string}) {

  }
}

export class SetOccMerchantKeyFail implements Action {
  readonly type = SET_OCC_MERCHANT_KEY_FAIL;

  constructor(public payload: string) {

  }
}

export class SetOccMerchantKeySuccess implements Action {
  readonly type = SET_OCC_MERCHANT_KEY_SUCCESS;

  constructor(public payload: string) {

  }
}

export class SetPaymentAddress implements Action {
  readonly type = SET_PAYMENT_ADDR;

  constructor(public payload: {
    cartId: string, userId: string, address: Address
  }) {}
}

export class SetPaymentAddressFail implements Action {
  readonly type = SET_PAYMENT_ADDR_FAIL;

  constructor(public error: HttpErrorResponse) {
  }
}

export class CreatePaymentDetails implements Action {
  readonly type = CREATE_PAYMENT_DETAILS;

  constructor(public payload: { paymentDetails: CheckoutComPaymentDetails, cartId: string, userId: string }) {

  }
}

export class CreatePaymentDetailsFail implements Action {
  readonly type = CREATE_PAYMENT_DETAILS_FAIL;

  constructor(readonly error) {

  }
}

export class CreatePaymentDetailsSuccess implements Action {
  readonly type = CREATE_PAYMENT_DETAILS_SUCCESS;

  constructor(readonly payload: PaymentDetails) {

  }
}

export class CreateApmPaymentDetails implements Action {
  readonly type = CREATE_APM_PAYMENT_DETAILS;

  constructor(public payload: { paymentDetails: ApmPaymentDetails, cartId: string, userId: string }) {

  }
}

export class SetPaymentAddressSuccess implements Action {
  readonly type = SET_PAYMENT_ADDR_SUCCESS;

  constructor(public payload: Address) {
  }
}

export class PlaceOrder extends StateUtils.EntityLoadAction {
  readonly type = PLACE_ORDER;

  constructor(
    public payload: { userId: string; cartId: string; termsChecked: boolean }
  ) {
    super(PROCESS_FEATURE, PLACED_ORDER_PROCESS_ID);
  }
}
export class RequestAvailableApms implements Action {
  readonly type = REQUEST_AVAILABLE_APMS;

  constructor(public payload: {cartId: string, userId: string}) {}
}
export class RequestAvailableApmsFail implements Action {
  readonly type = REQUEST_AVAILABLE_APMS_FAIL;

  constructor(public payload: string) {
  }
}

export class RequestAvailableApmsSuccess implements Action {
  readonly type = REQUEST_AVAILABLE_APMS_SUCCESS;

  constructor(public payload: ApmData[]) {

  }
}
export class SetSelectedApm implements Action {
  readonly type = SET_SELECTED_APM;

  constructor(public payload: ApmData) {}
}

export class PlaceOrderSuccess {
  readonly type = PLACE_ORDER_SUCCESS;

  constructor(public payload: Order) {
  }
}

export class PlaceOrderRedirect {
  readonly type = PLACE_ORDER_REDIRECT;

  constructor(
    public payload: CheckoutComRedirect
  ) {

  }
}

export class PlaceOrderFail {
  readonly type = PLACE_ORDER_FAIL;

  constructor(public payload: { error: HttpErrorModel, redirect?: CheckoutComRedirect }) {
  }
}

export class RedirectPlaceOrderAuthorize {
  readonly type = REDIRECT_PLACE_ORDER_AUTHORIZE;

  constructor(public payload: {
    userId: string; cartId: string; sessionId: string;
  })
  {}
}

export class RedirectPlaceOrderAuthorizeFail {
  readonly type = REDIRECT_PLACE_ORDER_AUTHORIZE_FAIL;

  constructor(readonly error)
  {}
}

export class RedirectPlaceOrderAuthorizeSuccess {
  readonly type = REDIRECT_PLACE_ORDER_AUTHORIZE_SUCCESS;

  constructor(public payload: Order) {
  }
}

export class GetGooglePayMerchantConfiguration implements Action {
  readonly type = GET_CONFIG_GOOGLE_PAY;
  constructor(public payload: { userId: string; cartId: string }) {}
}

export class GetGooglePayMerchantConfigurationFail implements Action {
  readonly type = GET_CONFIG_GOOGLE_PAY_FAIL;
  constructor(public payload: string) {}
}

export class GetGooglePayMerchantConfigurationSuccess implements Action {
  readonly type = GET_CONFIG_GOOGLE_PAY_SUCCESS;

  constructor(public payload: GooglePayMerchantConfiguration) {}
}

export class AuthoriseGooglePayPayment implements Action {
  readonly type = AUTHORISE_GOOGLE_PAY_PAYMENT;

  constructor(
    public payload: {
      userId: string;
      cartId: string;
      token: any;
      billingAddress: any;
      shippingAddress?: any;
      savePaymentMethod: boolean;
      email?:string;
    }
  ) {}
}

export class AuthoriseGooglePayPaymentFail implements Action {
  readonly type = AUTHORISE_GOOGLE_PAY_PAYMENT_FAIL;

  constructor(public payload: string) {}
}

export class AuthoriseGooglePayPaymentSuccess implements Action {
  readonly type = AUTHORISE_GOOGLE_PAY_PAYMENT_SUCCESS;

  constructor(public payload: PlaceOrderResponse) {}
}

export class SetKlarnaInitParams {
  readonly type = SET_KLARNA_INIT_PARAMS;

  constructor(public payload: {userId: string; cartId: string}) {}
}

export class SetKlarnaInitParamsSuccess {
  readonly type = SET_KLARNA_INIT_PARAMS_SUCCESS;

  constructor(public payload: KlarnaInitParams) {}
}

export class SetKlarnaInitParamsFail {
  readonly type = SET_KLARNA_INIT_PARAMS_FAIL;

  constructor(public payload: HttpErrorResponse) {}
}

export class RequestApplePayPaymentRequest implements Action {
  readonly type = REQUEST_APPLE_PAY_PAYMENT_REQUEST;

  constructor(public payload: {
    userId: string;
    cartId: string;
  }) {}
}

export class RequestApplePayPaymentRequestFail implements Action {
  readonly type = REQUEST_APPLE_PAY_PAYMENT_REQUEST_FAIL;

  constructor(public payload: string) {}
}

export class RequestApplePayPaymentRequestSuccess implements Action {
  readonly type = REQUEST_APPLE_PAY_PAYMENT_REQUEST_SUCCESS;

  constructor(public payload: ApplePayPaymentRequest) {}
}

export class StartApplePaySession implements Action {
  readonly type = START_APPLE_PAY_SESSION;

  constructor() {}
}

export class ValidateApplePayMerchant implements Action {
  readonly type = VALIDATE_APPLE_PAY_MERCHANT;

  constructor(
    public payload: { userId: string; cartId: string; validationURL: string }
  ) {}
}

export class ValidateApplePayMerchantFail implements Action {
  readonly type = VALIDATE_APPLE_PAY_MERCHANT_FAIL;

  constructor(public payload: string) {}
}

export class ValidateApplePayMerchantSuccess implements Action {
  readonly type = VALIDATE_APPLE_PAY_MERCHANT_SUCCESS;

  constructor(public payload: any) {}
}

export class AuthoriseApplePayPayment implements Action {
  readonly type = AUTHORISE_APPLE_PAY_PAYMENT;

  constructor(
    public payload: { userId: string; cartId: string; payment: any }
  ) {}
}

export class AuthoriseApplePayPaymentFail implements Action {
  readonly type = AUTHORISE_APPLE_PAY_PAYMENT_FAIL;

  constructor(public payload: string) {}
}

export class AuthoriseApplePayPaymentSuccess implements Action {
  readonly type = AUTHORISE_APPLE_PAY_PAYMENT_SUCCESS;

  constructor(public payload: ApplePayAuthorization) {}
}

export class SelectApplePayDeliveryAddress implements Action {
  readonly type = SELECT_APPLEPAY_DELIVERY_ADDRESS;

  constructor(
    public payload: { userId: string; cartId: string; shippingContact: ApplePayPaymentContact }
  ) {}
}

export class SelectApplePayDeliveryAddressFail implements Action {
  readonly type = SELECT_APPLEPAY_DELIVERY_ADDRESS_FAIL;

  constructor(public payload: string) {}
}

export class SelectApplePayDeliveryAddressSuccess implements Action {
  readonly type = SELECT_APPLEPAY_DELIVERY_ADDRESS_SUCCESS;

  constructor(public payload: ApplePayShippingContactUpdate) {}
}

export class SelectApplePayShippingMethod implements Action {
  readonly type = SELECT_APPLEPAY_DELIVERY_METHOD;

  constructor(
    public payload: { userId: string; cartId: string; shippingMethod: ApplePayShippingMethod }
  ) {}
}

export class SelectApplePayShippingMethodFail implements Action {
  readonly type = SELECT_APPLEPAY_DELIVERY_METHOD_FAIL;

  constructor(public payload: string) {}
}

export class SelectApplePayShippingMethodSuccess implements Action {
  readonly type = SELECT_APPLEPAY_DELIVERY_METHOD_SUCCESS;

  constructor(public payload: ApplePayShippingMethodUpdate) {}
}

export class GetGooglePayPaymentDataUpdate implements Action {
  readonly type = GET_GOOGLE_PAY_PAYMENT_UPDATE;
  constructor(public payload: {
    userId: string;
    cartId: string;
    paymentData: IntermediatePaymentData,
  }) {}
}

export class GetGooglePayPaymentDataUpdateFail implements Action {
  readonly type = GET_GOOGLE_PAY_PAYMENT_UPDATE_FAIL;
  constructor(public payload: string) {}
}

export class GetGooglePayPaymentDataUpdateSuccess implements Action {
  readonly type = GET_GOOGLE_PAY_PAYMENT_UPDATE_SUCCESS;

  constructor(public payload: PaymentDataRequestUpdate) {}
}

export class GetGooglePayPaymentAuthorizeSuccess implements Action {
  readonly type = GET_GOOGLE_PAY_PAYMENT_AUTHORISE_SUCCESS;

  constructor(public payload: PaymentAuthorizationResult) {}
}

export class GetGooglePayPaymentAuthorizeFail implements Action {
  readonly type = GET_GOOGLE_PAY_PAYMENT_AUTHORISE_FAIL;

  constructor(public payload: PaymentAuthorizationResult) {}
}

export type CheckoutComAction =
  | SetSelectedApm
  | RequestAvailableApms
  | RequestAvailableApmsFail
  | RequestAvailableApmsSuccess
  | SetOccMerchantKey
  | SetOccMerchantKeyFail
  | SetOccMerchantKeySuccess
  | SetPaymentAddress
  | SetPaymentAddressFail
  | SetPaymentAddressSuccess
  | CreatePaymentDetails
  | CreatePaymentDetailsFail
  | CreatePaymentDetailsSuccess
  | PlaceOrder
  | PlaceOrderSuccess
  | PlaceOrderRedirect
  | PlaceOrderFail
  | RedirectPlaceOrderAuthorize
  | RedirectPlaceOrderAuthorizeFail
  | RedirectPlaceOrderAuthorizeSuccess
  | CreateApmPaymentDetails
  | GetGooglePayMerchantConfiguration
  | GetGooglePayMerchantConfigurationFail
  | GetGooglePayMerchantConfigurationSuccess
  | AuthoriseGooglePayPayment
  | AuthoriseGooglePayPaymentFail
  | AuthoriseGooglePayPaymentSuccess
  | RequestApplePayPaymentRequest
  | RequestApplePayPaymentRequestFail
  | RequestApplePayPaymentRequestSuccess
  | StartApplePaySession
  | ValidateApplePayMerchant
  | ValidateApplePayMerchantFail
  | ValidateApplePayMerchantSuccess
  | AuthoriseApplePayPayment
  | AuthoriseApplePayPaymentFail
  | AuthoriseApplePayPaymentSuccess
  | SetKlarnaInitParams
  | SetKlarnaInitParamsSuccess
  | SetKlarnaInitParamsFail
  | SelectApplePayDeliveryAddress
  | SelectApplePayDeliveryAddressFail
  | SelectApplePayDeliveryAddressSuccess
  | SelectApplePayShippingMethod
  | SelectApplePayShippingMethodFail
  | SelectApplePayShippingMethodSuccess
  | GetGooglePayPaymentDataUpdate
  | GetGooglePayPaymentDataUpdateFail
  | GetGooglePayPaymentDataUpdateSuccess
  | GetGooglePayPaymentAuthorizeFail
  | GetGooglePayPaymentAuthorizeSuccess
  ;
