import { Injectable } from '@angular/core';
import { Actions, Effect, ofType } from '@ngrx/effects';
import { Observable, of } from 'rxjs';

import * as CheckoutComActions from './checkout-com.actions';
import { catchError, exhaustMap, map, mergeMap, switchMap } from 'rxjs/operators';
import { CheckoutComOccAdapter } from '../adapters/occ/checkout-com-occ.adapter';
import {
  CartActions,
  GlobalMessageActions,
  GlobalMessageType,
  normalizeHttpError,
  Translatable,
  GlobalMessage
} from '@spartacus/core';
import { CheckoutActions } from '@spartacus/checkout/core';
import { CheckoutComRedirect } from '../interfaces';
import { ApmData } from '../model/ApmData';
import { GooglePayMerchantConfiguration, PlaceOrderResponse } from '../model/GooglePay';
import { ApplePayAuthorization, ApplePayPaymentRequest } from '../model/ApplePay';

@Injectable()
export class CheckoutComEffects {
  constructor(
    private actions$: Actions, protected checkoutComAdapter: CheckoutComOccAdapter) {
  }

  @Effect()
  getOccMerchantKey: Observable<| CheckoutComActions.SetOccMerchantKeySuccess
    | GlobalMessageActions.AddMessage
    | CheckoutComActions.SetOccMerchantKeyFail> = this.actions$.pipe(
    ofType(CheckoutComActions.SET_OCC_MERCHANT_KEY),
    map((action: any) => action.payload),
    mergeMap(payload => this.checkoutComAdapter.getMerchantKey(payload.userId)
      .pipe(
        switchMap(data => [new CheckoutComActions.SetOccMerchantKeySuccess(data)]),
        catchError(error => [
          new CheckoutComActions.SetOccMerchantKeyFail(error),
          new GlobalMessageActions.AddMessage({
            type: GlobalMessageType.MSG_TYPE_ERROR,
            text: {
              key: 'paymentForm.merchantKeyFailed'
            }
          })
        ])
      )
    )
  );

  @Effect()
  setPaymentAddress: Observable<CheckoutComActions.SetPaymentAddressSuccess
    | CheckoutComActions.SetPaymentAddressFail> = this.actions$.pipe(
    ofType(CheckoutComActions.SET_PAYMENT_ADDR),
    map((action: any) => action.payload),
    mergeMap(payload => this.checkoutComAdapter.setPaymentAddress(payload.cartId, payload.userId, payload.address)
      .pipe(
        switchMap(_ => [new CheckoutComActions.SetPaymentAddressSuccess(payload.address)]),
        catchError(error => of(new CheckoutComActions.SetPaymentAddressFail(error)))
      )
    )
  );

  @Effect()
  createPaymentDetails: Observable<CheckoutComActions.CreatePaymentDetailsSuccess
    | CheckoutActions.CreatePaymentDetailsSuccess
    | CheckoutComActions.CreatePaymentDetailsFail> = this.actions$.pipe(
    ofType(CheckoutComActions.CREATE_PAYMENT_DETAILS),
    exhaustMap((action: CheckoutComActions.CreatePaymentDetails) =>
      this.checkoutComAdapter.createPaymentDetails(action.payload.userId, action.payload.cartId, action.payload.paymentDetails).pipe(
        switchMap(data => [
            new CheckoutComActions.CreatePaymentDetailsSuccess(data),
            new CheckoutActions.CreatePaymentDetailsSuccess(data)
          ]
        ),
        catchError((error) => of(new CheckoutComActions.CreatePaymentDetailsFail(error)))
      )
    )
  );

  @Effect()
  createApmPaymentDetails: Observable<| CheckoutComActions.CreatePaymentDetailsSuccess
    | CheckoutActions.CreatePaymentDetailsSuccess
    | CheckoutComActions.CreatePaymentDetailsFail> = this.actions$.pipe(
    ofType(CheckoutComActions.CREATE_APM_PAYMENT_DETAILS),
    exhaustMap((action: CheckoutComActions.CreateApmPaymentDetails) =>
      this.checkoutComAdapter.createApmPaymentDetails(action.payload.userId, action.payload.cartId, action.payload.paymentDetails).pipe(
        switchMap(data => [
            new CheckoutComActions.CreatePaymentDetailsSuccess(action.payload.paymentDetails),
            new CheckoutActions.CreatePaymentDetailsSuccess(action.payload.paymentDetails),
          ]
        ),
        catchError((error) => of(new CheckoutComActions.CreatePaymentDetailsFail(error)))
      )
    )
  );


  @Effect()
  placeOrder: Observable<| GlobalMessageActions.AddMessage
    | CheckoutComActions.PlaceOrderSuccess
    | CheckoutComActions.PlaceOrderFail
    | CheckoutComActions.PlaceOrderRedirect
    | CheckoutActions.PlaceOrderSuccess
    | CheckoutActions.PlaceOrderFail
    | CartActions.RemoveCart> = this.actions$.pipe(
    ofType(CheckoutComActions.PLACE_ORDER),
    map((action: any) => action.payload),
    mergeMap((payload) => {
      return this.checkoutComAdapter
        .placeOrder(payload.userId, payload.cartId, payload.termsChecked)
        .pipe(
          switchMap((data) => {
            const actions: (CartActions.RemoveCart
              | CheckoutActions.PlaceOrderSuccess
              | CheckoutComActions.PlaceOrderRedirect
              | CheckoutComActions.PlaceOrderSuccess
              )[] = [];
            let redirected = false;
            try {
              if (data != null && typeof data === 'object' && data.hasOwnProperty('redirectUrl')) {
                const redirect = data as CheckoutComRedirect;
                // redirect the user to the checkout.com validation service
                actions.push(new CheckoutComActions.PlaceOrderRedirect(redirect));
                redirected = true;
              }
            } catch (e) {
            }
            if (!redirected) {
              // only remove cart if order is directly approved
              actions.push(new CheckoutActions.PlaceOrderSuccess(data));
              actions.push(new CartActions.RemoveCart({cartId: payload.cartId}));
              actions.push(new CheckoutComActions.PlaceOrderSuccess(data));
            }
            return actions;
          }),
          catchError((response) => {
              const actions: (GlobalMessageActions.AddMessage
                | CheckoutActions.PlaceOrderFail
                | CheckoutComActions.PlaceOrderFail
                )[] = [
                new CheckoutActions.PlaceOrderFail(normalizeHttpError(response)),
                new CheckoutComActions.PlaceOrderFail({error: normalizeHttpError(response)}),
              ];
              try {
                const errObj = response?.error?.errors?.[0];
                if (errObj != null && typeof errObj === 'object') {
                  const text: Translatable = {};
                  if (errObj.type && typeof errObj.type === 'string') {
                    const symbols = errObj.type.split('');
                    symbols[0] = symbols[0].toLowerCase();
                    text.key = 'checkoutReview.' + symbols.join('');
                  }
                  if (!text.key) {
                    if (errObj.message && typeof errObj.message === 'string') {
                      text.raw = errObj.message;
                    }
                  }
                  if (text.key || text.raw) {
                    actions.push(new GlobalMessageActions.AddMessage({
                      text,
                      type: GlobalMessageType.MSG_TYPE_ERROR,
                    }));
                  }
                }
              } catch (e) {
                console.error(e);
              }
              return actions;
            }
          )
        );
    })
  );

  @Effect()
  authorizeRedirectPlaceOrder: Observable<| CheckoutComActions.RedirectPlaceOrderAuthorizeSuccess
    | CheckoutActions.PlaceOrderSuccess
    | CartActions.RemoveCart
    | CheckoutComActions.RedirectPlaceOrderAuthorizeFail> = this.actions$.pipe(
    ofType(CheckoutComActions.REDIRECT_PLACE_ORDER_AUTHORIZE),
    map((action: any) => action.payload),
    mergeMap((payload) => {
      return this.checkoutComAdapter.authorizeRedirectPlaceOrder(payload.userId, payload.cartId, payload.sessionId)
        .pipe(
          switchMap((data) => [
            new CheckoutComActions.RedirectPlaceOrderAuthorizeSuccess(data),
            new CheckoutActions.PlaceOrderSuccess(data),
            new CartActions.RemoveCart({cartId: payload.cartId}),
          ]),
          catchError(error => of(
            new CheckoutComActions.RedirectPlaceOrderAuthorizeFail(error)
            )
          )
        );
    })
  );

  @Effect()
  requestAvailableApms: Observable<CheckoutComActions.RequestAvailableApmsSuccess
    | CheckoutComActions.RequestAvailableApmsFail> = this.actions$.pipe(
    ofType(CheckoutComActions.REQUEST_AVAILABLE_APMS),
    map((action: any) => action.payload),
    mergeMap(payload => this.checkoutComAdapter.requestAvailableApms(payload.userId, payload.cartId)
      .pipe(
        switchMap((data: ApmData[]) => [
          new CheckoutComActions.RequestAvailableApmsSuccess(data)
        ]),
        catchError(error => of(new CheckoutComActions.RequestAvailableApmsFail(error)))
      )
    )
  );

  @Effect()
  googlePayGetMerchantConfiguration: Observable<| CheckoutComActions.GetGooglePayMerchantConfigurationSuccess
    | CheckoutComActions.GetGooglePayMerchantConfigurationFail> = this.actions$.pipe(
    ofType(CheckoutComActions.GET_CONFIG_GOOGLE_PAY),
    map((action: any) => action.payload),
    mergeMap(({userId, cartId}) => {
      return this.checkoutComAdapter
        .getGooglePayMerchantConfiguration(userId, cartId)
        .pipe(
          switchMap((merchantConfiguration: GooglePayMerchantConfiguration) => [
            new CheckoutComActions.GetGooglePayMerchantConfigurationSuccess(
              merchantConfiguration
            )
          ]),
          catchError(error => [
            new CheckoutComActions.GetGooglePayMerchantConfigurationFail(error)
          ])
        );
    })
  );

  @Effect()
  googlePayAuthorisePayment: Observable<| CheckoutActions.SetPaymentDetailsSuccess
    | CheckoutActions.PlaceOrderSuccess
    | CartActions.RemoveCart
    | GlobalMessageActions.AddMessage
    | CheckoutComActions.AuthoriseGooglePayPaymentSuccess
    | CheckoutComActions.AuthoriseGooglePayPaymentFail> = this.actions$.pipe(
    ofType(CheckoutComActions.AUTHORISE_GOOGLE_PAY_PAYMENT),
    map((action: any) => action.payload),
    mergeMap(({userId, cartId, token, billingAddress, savePaymentMethod}) => {
      return this.checkoutComAdapter
        .authoriseGooglePayPayment(
          userId,
          cartId,
          token,
          billingAddress,
          savePaymentMethod
        )
        .pipe(
          switchMap((placeOrderResponse: PlaceOrderResponse) => [
            new CheckoutActions.PlaceOrderSuccess(placeOrderResponse.orderData),
            new CartActions.RemoveCart({cartId}),
            new CheckoutComActions.AuthoriseGooglePayPaymentSuccess(
              placeOrderResponse
            ),
          ]),
          catchError(error => [
            createPaymentFailAction(
              'paymentForm.googlepay.authorisationFailed'
            ),
            new CheckoutComActions.AuthoriseGooglePayPaymentFail(error)
          ])
        );
    })
  );


  @Effect()
  applePayPaymentRequest: Observable<| CheckoutComActions.RequestApplePayPaymentRequestSuccess
    | CheckoutComActions.RequestApplePayPaymentRequestFail> = this.actions$.pipe(
    ofType(CheckoutComActions.REQUEST_APPLE_PAY_PAYMENT_REQUEST),
    map((action: any) => action.payload),
    mergeMap(payload => {
      return this.checkoutComAdapter
        .requestApplePayPaymentRequest(payload.userId, payload.cartId)
        .pipe(
          switchMap((applePayPaymentRequest: ApplePayPaymentRequest) => [
            new CheckoutComActions.RequestApplePayPaymentRequestSuccess(
              applePayPaymentRequest
            )
          ]),
          catchError(error => [
            new CheckoutComActions.RequestApplePayPaymentRequestFail(error)
          ])
        );
    })
  );

  @Effect()
  applePayValidateMerchant: Observable<| CheckoutComActions.ValidateApplePayMerchantSuccess
    | CheckoutComActions.ValidateApplePayMerchantFail
    | GlobalMessageActions.AddMessage> = this.actions$.pipe(
    ofType(CheckoutComActions.VALIDATE_APPLE_PAY_MERCHANT),
    map((action: any) => action.payload),
    mergeMap(payload => {
      return this.checkoutComAdapter
        .validateApplePayMerchant(
          payload.userId,
          payload.cartId,
          payload.validationURL
        )
        .pipe(
          switchMap((response: any) => [
            new CheckoutComActions.ValidateApplePayMerchantSuccess(response)
          ]),
          catchError(error => [
            createPaymentFailAction(
              'paymentForm.applePay.merchantValidationFailed'
            ),
            new CheckoutComActions.ValidateApplePayMerchantFail(error)
          ])
        );
    })
  );

  @Effect()
  applePayAuthorisePayment: Observable<| CheckoutComActions.AuthoriseApplePayPaymentSuccess
    | CheckoutComActions.AuthoriseApplePayPaymentFail
    | GlobalMessageActions.AddMessage
    | CheckoutActions.PlaceOrderSuccess
    | CartActions.RemoveCart> = this.actions$.pipe(
    ofType(CheckoutComActions.AUTHORISE_APPLE_PAY_PAYMENT),
    map((action: any) => action.payload),
    mergeMap(payload => {
      return this.checkoutComAdapter
        .authorizeApplePayPayment(
          payload.userId,
          payload.cartId,
          payload.payment
        )
        .pipe(
          switchMap((response: ApplePayAuthorization) => [
            new CheckoutActions.PlaceOrderSuccess(response.orderData),
            new CartActions.RemoveCart({cartId: payload.cartId}),
            new CheckoutComActions.AuthoriseApplePayPaymentSuccess(response),
          ]),
          catchError(error => [
            createPaymentFailAction('paymentForm.applePay.authorisationFailed'),
            new CheckoutComActions.AuthoriseApplePayPaymentFail(error)
          ])
        );
    })
  );

  @Effect()
  setKlarnaInitParams: Observable<
    CheckoutComActions.SetKlarnaInitParamsSuccess
    | CheckoutComActions.SetKlarnaInitParamsFail
    > = this.actions$.pipe(
      ofType(CheckoutComActions.SET_KLARNA_INIT_PARAMS),
    map((action: any) => action.payload),
    mergeMap((payload) => this.checkoutComAdapter.getKlarnaInitParams(payload.userId, payload.cartId)
      .pipe(
        switchMap((params) => [new CheckoutComActions.SetKlarnaInitParamsSuccess(params)]),
        catchError(error => [new CheckoutComActions.SetKlarnaInitParamsFail(error)])
      )
    )
  );

}

const createPaymentFailAction = (
  key: string = 'checkoutReview.initialPaymentRequestFailed'
) => {
  const failMessage: GlobalMessage = {
    text: {
      key
    },
    type: GlobalMessageType.MSG_TYPE_ERROR
  };
  return new GlobalMessageActions.AddMessage(failMessage);
};

export const effects: any[] = [CheckoutComEffects];
