import { CHECKOUT_COM_FEATURE, CheckoutComState, StateWithCheckoutCom } from './checkout-com.state';
import { createFeatureSelector, createSelector, MemoizedSelector } from '@ngrx/store';
import { ApmData } from '../model/ApmData';
import { KlarnaInitParams } from '../interfaces';
import {
  ApplePayAuthorization,
  ApplePayPaymentRequest,
  ApplePayShippingContactUpdate,
  ApplePayShippingMethodUpdate
} from '../model/ApplePay';
import {PaymentAuthorizationResult, PaymentDataRequestUpdate} from '../model/GooglePay';

// projectors
const projectOccMerchantKey = (state: CheckoutComState) => state.occMerchantKey;
const projectPaymentDetails = (state: CheckoutComState) => state;
const projectPaymentAddress = (state: CheckoutComState) => state.paymentAddress;
const projectPlaceOrder = (state: CheckoutComState) => state.placeOrder;
const projectAvailableApms = (state: CheckoutComState) => state.availableApms;
const projectSelectedApm = (state: CheckoutComState) => state.selectedApm;
const projectKlarnaInitParams = (state: CheckoutComState) => state.klarnaInitParams;

const projectGooglePayMerchantConfiguration = (state: CheckoutComState) => state.googlePayMerchantConfiguration;
const projectGooglePayPaymentDataUpdate = (state: CheckoutComState) => state.googlePayPaymentDataUpdate;
const projectGooglePayPaymentAuthorizationResult = (state: CheckoutComState) => state.googlePayPaymentAuthorizationResult;

const projectApmLoading = (state: CheckoutComState) => state.apmLoading;

const projectApplePayMerchantSession = (state: CheckoutComState) => state.applePayMerchantSession;
const projectApplePayAuthorization = (state: CheckoutComState) => state.applePayAuthorization;
const projectApplePayPaymentRequest = (state: CheckoutComState) => state.applePayPaymentRequest;

const projectApplePayDeliveryAddressUpdate = (state: CheckoutComState) => state.applePayShippingContactUpdate;
const projectApplePayDeliveryMethodUpdate = (state: CheckoutComState) => state.applePayShippingMethodUpdate;

// feature selector
export const getCheckoutComState: MemoizedSelector<StateWithCheckoutCom,
  CheckoutComState> = createFeatureSelector<CheckoutComState>(CHECKOUT_COM_FEATURE);

// selectors
export const getOccMerchantKey: MemoizedSelector<StateWithCheckoutCom,
  string> = createSelector(getCheckoutComState, projectOccMerchantKey);

export const getPaymentDetails = createSelector(getCheckoutComState, projectPaymentDetails);

export const getPaymentAddress = createSelector(getCheckoutComState, projectPaymentAddress);

export const getPlaceOrder = createSelector(getCheckoutComState, projectPlaceOrder);

export const getAvailableApms: MemoizedSelector<
  StateWithCheckoutCom,
  ApmData[]
  > = createSelector(getCheckoutComState, projectAvailableApms);

export const getSelectedApm: MemoizedSelector<StateWithCheckoutCom,
  ApmData> = createSelector(getCheckoutComState, projectSelectedApm);

export const getApplePayPaymentRequest: MemoizedSelector<StateWithCheckoutCom,
  ApplePayPaymentRequest> = createSelector(getCheckoutComState, projectApplePayPaymentRequest);

export const getApplePayMerchantSession: MemoizedSelector<StateWithCheckoutCom,
  any> = createSelector(
  getCheckoutComState,
  projectApplePayMerchantSession
);

export const getApplePayPaymentAuthorization: MemoizedSelector<StateWithCheckoutCom,
  ApplePayAuthorization> = createSelector(
  getCheckoutComState,
  projectApplePayAuthorization
);

export const getGooglePayMerchantConfiguration: MemoizedSelector<StateWithCheckoutCom, any> =
  createSelector(getCheckoutComState, projectGooglePayMerchantConfiguration);

export const getApmLoading: MemoizedSelector<StateWithCheckoutCom, boolean> =
  createSelector(getCheckoutComState, projectApmLoading);

export const getKlarnaInitParams: MemoizedSelector<
  StateWithCheckoutCom, KlarnaInitParams> = createSelector(getCheckoutComState, projectKlarnaInitParams);

export const getApplePayDeliveryAddressUpdate: MemoizedSelector<
  StateWithCheckoutCom, ApplePayShippingContactUpdate
  > = createSelector(getCheckoutComState, projectApplePayDeliveryAddressUpdate);

export const getApplePayDeliveryMethodUpdate: MemoizedSelector<
  StateWithCheckoutCom, ApplePayShippingMethodUpdate
  > = createSelector(getCheckoutComState, projectApplePayDeliveryMethodUpdate);

export const getGooglePayPaymentDataUpdate: MemoizedSelector<
  StateWithCheckoutCom, PaymentDataRequestUpdate
  > = createSelector(getCheckoutComState, projectGooglePayPaymentDataUpdate);

export const getGooglePayPaymentAuthorizationResult: MemoizedSelector<
  StateWithCheckoutCom, PaymentAuthorizationResult
  > = createSelector(getCheckoutComState, projectGooglePayPaymentAuthorizationResult);
