import { InjectionToken, Provider } from '@angular/core';
import { ActionReducerMap } from '@ngrx/store';
import { CheckoutComState } from './checkout-com.state';
import * as CheckoutComActions from './checkout-com.actions';
import { CheckoutComAction } from './checkout-com.actions';
import { PaymentType } from '../model/ApmData';
import { CheckoutActions } from '@spartacus/checkout/core';

export const initialState: CheckoutComState = {
  paymentDetails: null,
  paymentDetailsError: null,
  placeOrder: null,
  availableApms: [],
  selectedApm: {
    code: PaymentType.Card
  },
  klarnaInitParams: null,
};

export function reducer(state = initialState,
                        action: CheckoutComAction | CheckoutActions.ClearCheckoutData): CheckoutComState {
  switch (action.type) {
    case CheckoutComActions.SET_OCC_MERCHANT_KEY_SUCCESS:
      return {...state, occMerchantKey: action.payload};
    case CheckoutComActions.CREATE_PAYMENT_DETAILS:
      return {...state, paymentDetails: null, paymentDetailsError: null};
    case CheckoutComActions.CREATE_PAYMENT_DETAILS_SUCCESS:
      return {...state, paymentDetails: action.payload, paymentDetailsError: null};
    case CheckoutComActions.CREATE_PAYMENT_DETAILS_FAIL:
      return {...state, paymentDetails: null, paymentDetailsError: action.error};

    case CheckoutComActions.SET_PAYMENT_ADDR:
      return {...state, paymentAddress: null};
    case CheckoutComActions.SET_PAYMENT_ADDR_SUCCESS:
      return {...state, paymentAddress: action.payload};
    case CheckoutComActions.SET_PAYMENT_ADDR_FAIL:
      return {...state, paymentAddress: action.error};

    case CheckoutComActions.PLACE_ORDER:
      return {...state, placeOrder: null};
    case CheckoutComActions.PLACE_ORDER_REDIRECT:
      return {...state, placeOrder: {successful: undefined, redirect: action.payload}};
    case CheckoutComActions.PLACE_ORDER_SUCCESS:
      return {...state, placeOrder: {successful: true, order: action.payload}};
    case CheckoutComActions.PLACE_ORDER_FAIL:
      return {
        ...state,
        placeOrder: {successful: false, httpError: action.payload.error, redirect: action.payload.redirect}
      };

    case CheckoutComActions.REQUEST_AVAILABLE_APMS_SUCCESS:
      return {...state, availableApms: action.payload};

    case CheckoutComActions.SET_SELECTED_APM:
      return {...state, selectedApm: action.payload};

    case CheckoutComActions.GET_CONFIG_GOOGLE_PAY_SUCCESS:
      return {...state, googlePayMerchantConfiguration: action.payload};

    case CheckoutComActions.AUTHORISE_GOOGLE_PAY_PAYMENT:
      return {...state, apmLoading: true};

    case CheckoutComActions.AUTHORISE_GOOGLE_PAY_PAYMENT_FAIL:
    case CheckoutComActions.AUTHORISE_GOOGLE_PAY_PAYMENT_SUCCESS:
      return {...state, apmLoading: false};

    case CheckoutComActions.SET_KLARNA_INIT_PARAMS:
      return {...state, klarnaInitParams: null};
    case CheckoutComActions.SET_KLARNA_INIT_PARAMS_SUCCESS:
      return {...state, klarnaInitParams: action.payload};
    case CheckoutComActions.SET_KLARNA_INIT_PARAMS_FAIL:
      return {...state, klarnaInitParams: {httpError: action.payload}};

    case CheckoutComActions.REQUEST_APPLE_PAY_PAYMENT_REQUEST_SUCCESS:
      return {
        ...state,
        applePayPaymentRequest: action.payload
      };

    case CheckoutComActions.START_APPLE_PAY_SESSION: {
      return {
        ...state,
        applePayAuthorization: null,
        applePayMerchantSession: null,
        applePayShippingMethodUpdate: null,
        applePayShippingContactUpdate: null,
      };
    }
    case CheckoutComActions.VALIDATE_APPLE_PAY_MERCHANT_SUCCESS:
      return {
        ...state,
        applePayMerchantSession: action.payload
      };

    case CheckoutComActions.AUTHORISE_APPLE_PAY_PAYMENT_SUCCESS:
      return {
        ...state,
        applePayAuthorization: action.payload
      };

    case CheckoutComActions.SELECT_APPLEPAY_DELIVERY_METHOD_SUCCESS:
      return {...state, applePayShippingMethodUpdate: action.payload};

    case CheckoutComActions.SELECT_APPLEPAY_DELIVERY_ADDRESS_SUCCESS:
      return {...state, applePayShippingContactUpdate: action.payload};

    case CheckoutComActions.GET_GOOGLE_PAY_PAYMENT_UPDATE:
      return {
        ...state,
        googlePayPaymentDataUpdate: undefined
      };

    case CheckoutComActions.GET_GOOGLE_PAY_PAYMENT_UPDATE_SUCCESS:
      return {
        ...state,
        googlePayPaymentDataUpdate: action.payload
      };

    case CheckoutComActions.GET_GOOGLE_PAY_PAYMENT_AUTHORISE_SUCCESS:
    case CheckoutComActions.GET_GOOGLE_PAY_PAYMENT_AUTHORISE_FAIL:
      return {
        ...state,
        googlePayPaymentAuthorizationResult: action.payload
      };

    case CheckoutActions.CLEAR_CHECKOUT_DATA: {
      return {
        ...initialState
      };
    }
  }

  return state;
}

export const reducerToken: InjectionToken<ActionReducerMap<CheckoutComState>>
  = new InjectionToken<ActionReducerMap<CheckoutComState>>('CheckoutComReducers');

export const reducerProvider: Provider = {
  provide: reducerToken,
  useFactory: reducer
};
