import * as CheckoutComActions from './checkout-com.actions';
import { RequestAvailableApmsSuccess } from './checkout-com.actions';
import { initialState, reducer } from './checkout-com.reducer';
import { HttpErrorResponse } from '@angular/common/http';
import { ApmData, PaymentType } from '../model/ApmData';
import { CheckoutComState } from './checkout-com.state';
import { CheckoutActions } from '@spartacus/checkout/core';

describe('CheckoutComReducer', () => {

  describe('Action: SetOccMerchantKey', () => {
    it('should set the hello message on success', () => {
      const newState = reducer(initialState, new CheckoutComActions.SetOccMerchantKeySuccess('pk_blalala'));

      expect(newState.occMerchantKey).toEqual('pk_blalala');
    });
  });

  describe('Action: CREATE_OCC_PAYMENT_DETAILS', () => {

    it('CREATE_OCC_PAYMENT_DETAILS', () => {
      const newState = reducer(initialState, new CheckoutComActions.CreatePaymentDetails({
        paymentDetails: {
          cardBin: '424242',
          cardNumber: '4242424242424242'
        }, cartId: '00001', userId: 'current'
      }));

      expect(newState.paymentDetails).toBeNull();
      expect(newState.paymentDetailsError).toBeNull();
    });

    it('CREATE_OCC_PAYMENT_DETAILS_SUCCESS', () => {
      const newState = reducer(initialState, new CheckoutComActions.CreatePaymentDetailsSuccess({
        cardNumber: '4242424242424242'
      }));

      expect(newState.paymentDetails).toEqual({
        cardNumber: '4242424242424242'
      });
      expect(newState.paymentDetailsError).toBeNull();
    });

    it('CREATE_OCC_PAYMENT_DETAILS_FAIL', () => {
      const newState = reducer(initialState, new CheckoutComActions.CreatePaymentDetailsFail({msg: 'Error'}));

      expect(newState.paymentDetails).toBeNull();
      expect(newState.paymentDetailsError).toEqual({msg: 'Error'});
    });
  });
  describe('Action: Request Available APM', () => {
    it('should populat the available apms', () => {
      const newAvailableApms: ApmData[] = [{
        "code" : PaymentType.PayPal,
        "name" : "PayPal",
        "isRedirect" : true,
        "isUserDataRequired" : false
      },
        {
          "code" : PaymentType.Sofort,
          "name" : "Sofort",
          "isRedirect" : true,
          "isUserDataRequired" : false
        }];
      const newState = reducer(initialState,
        new RequestAvailableApmsSuccess(newAvailableApms));

      expect(newState.availableApms).toEqual(newAvailableApms);
    });
  })
  describe('Action: SET_PAYMENT_ADDR', () => {
    it('SET_PAYMENT_ADDR', () => {
      const newState = reducer(initialState, new CheckoutComActions.SetPaymentAddress({
        cartId: '00001', userId: 'current', address: {line1: 'line1'}
      }));

      expect(newState.paymentAddress).toBeNull();
    });

    it('SET_PAYMENT_ADDR_SUCCESS', () => {
      const newState = reducer(initialState, new CheckoutComActions.SetPaymentAddressSuccess({line1: 'line1'}));

      expect(newState.paymentAddress).toEqual({line1: 'line1'});
    });

    it('SET_PAYMENT_ADDR_FAIL', () => {
      const err = {status: 499, message: 'test'} as HttpErrorResponse;
      const newState = reducer(initialState, new CheckoutComActions.SetPaymentAddressFail(err));

      expect(newState.paymentAddress).toEqual(err);
    });
  });
  describe('Action; Set Selected APMs', () => {

    it('should persist the selected APM', () => {
      const cardApm: ApmData  = { code: PaymentType.Card};
      const newState = reducer(initialState, new CheckoutComActions.SetSelectedApm(
        cardApm
      ));

      expect(newState.selectedApm.code).toBe(PaymentType.Card);
    })
  });
  describe('Action: SET_KLARNA_INIT_PARAMS', () => {
    it('SET', () => {
      const newState = reducer(initialState, new CheckoutComActions.SetKlarnaInitParams({
        cartId: '00001', userId: 'current'
      }));

      expect(newState.klarnaInitParams).toBeNull();
    });

    it('SUCCESS', () => {
      const newState = reducer(initialState, new CheckoutComActions.SetKlarnaInitParamsSuccess({clientToken: 'tokenKlarna'}));

      expect(newState.klarnaInitParams).toEqual({clientToken: 'tokenKlarna'});
      expect(newState.klarnaInitParams.httpError).toBeFalsy();
    });

    it('FAIL', () => {
      const err = {status: 499, message: 'test'} as HttpErrorResponse;
      const newState = reducer(initialState, new CheckoutComActions.SetKlarnaInitParamsFail(err));

      expect(newState.klarnaInitParams.httpError).toEqual(err);
    });
  });

  describe('Action: clear state', () => {
    it('should clear all state to initialState', ()=> {
      const messyState: CheckoutComState = {
        apmLoading: true,
        selectedApm: {
          code: PaymentType.Sepa
        },
        paymentDetails: {
          accountHolderName: 'jane doe'
        }
      };

      const state = reducer(messyState, new CheckoutActions.ClearCheckoutData());

      expect(state).toEqual(initialState);
    });
  })
});
