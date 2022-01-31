import { CHECKOUT_COM_FEATURE, CheckoutComState, StateWithCheckoutCom } from './checkout-com.state';
import { createFeatureSelector, createSelector, MemoizedSelector } from '@ngrx/store';
import { ApmData } from '../model/ApmData';
import { KlarnaInitParams } from '../interfaces';
import { ApplePayAuthorization, ApplePayPaymentRequest } from '../model/ApplePay';

// projectors
const projectOccMerchantKey = (state: CheckoutComState) => state.occMerchantKey;
const projectPaymentDetails = (state: CheckoutComState) => state;
const projectPaymentAddress = (state: CheckoutComState) => state.paymentAddress;
const projectPlaceOrder = (state: CheckoutComState) => state.placeOrder;
const projectAvailableApms = (state: CheckoutComState) => state.availableApms;
const projectSelectedApm = (state: CheckoutComState) => state.selectedApm;
const projectKlarnaInitParams = (state: CheckoutComState) => state.klarnaInitParams;

const projectGooglePayMerchantConfiguration = (state: CheckoutComState) => state.googlePayMerchantConfiguration;
const projectApmLoading = (state: CheckoutComState) => state.apmLoading;

const projectApplePayMerchantSession = (state: CheckoutComState) => state.applePayMerchantSession;
const projectApplePayAuthorization = (state: CheckoutComState) => state.applePayAuthorization;
const projectApplePayPaymentRequest = (state: CheckoutComState) => state.applePayPaymentRequest;

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
