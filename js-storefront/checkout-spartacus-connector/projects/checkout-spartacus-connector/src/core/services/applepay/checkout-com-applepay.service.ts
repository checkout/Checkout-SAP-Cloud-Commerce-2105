import { Injectable } from '@angular/core';
import { StateWithCheckoutCom } from '../../store/checkout-com.state';
import { select, Store } from '@ngrx/store';
import { ActiveCartService, GlobalMessageService, GlobalMessageType, UserIdService, WindowRef } from '@spartacus/core';
import { Observable } from 'rxjs';
import { ApplePayAuthorization, ApplePayPaymentRequest } from '../../model/ApplePay';
import { createApplePaySession } from './applepay-session';
import { AuthoriseApplePayPayment, RequestApplePayPaymentRequest, StartApplePaySession, ValidateApplePayMerchant } from '../../store/checkout-com.actions';
import { getApplePayMerchantSession, getApplePayPaymentAuthorization, getApplePayPaymentRequest } from '../../store/checkout-com.selectors';
import { getUserIdCartId } from '../../shared/get-user-cart-id';

@Injectable({
  providedIn: 'root'
})
export class CheckoutComApplepayService {

  constructor(
    protected checkoutComStore: Store<StateWithCheckoutCom>,
    protected activeCartService: ActiveCartService,
    protected userIdService: UserIdService,
    protected globalMessageService: GlobalMessageService,
    protected windowRef: WindowRef) {

  }

  createSession(paymentRequest: ApplePayPaymentRequest): any {
    if (!this.windowRef.isBrowser()) {
      return;
    }

    const ApplePaySession = createApplePaySession(this.windowRef);
    const session = new ApplePaySession(5, paymentRequest);
    session.onvalidatemerchant = this.onValidateMerchant.bind(this);
    session.onpaymentauthorized = this.onPaymentAuthorized.bind(this);

    session.onerror = this.onPaymentError.bind(this);
    session.oncancel = this.onPaymentError.bind(this);

    session.begin();

    this.checkoutComStore.dispatch(new StartApplePaySession());

    return session;
  }

  /**
   * Create observable for ApplePay Payment Request
   */
  getPaymentRequestFromState(): Observable<ApplePayPaymentRequest> {
    return this.checkoutComStore.pipe(select(getApplePayPaymentRequest));
  }

  getMerchantSesssionFromState(): Observable<any> {
    return this.checkoutComStore.pipe(select(getApplePayMerchantSession));
  }

  getPaymentAuthorizationFromState(): Observable<ApplePayAuthorization> {
    return this.checkoutComStore.pipe(
      select(getApplePayPaymentAuthorization)
    );
  }

  /**
   * Dispatch event to request Apple Pay request that can be used to initialize the ApplePaySession
   */
  requestApplePayPaymentRequest(userId: string, cartId: string): void {
    this.checkoutComStore.dispatch(
      new RequestApplePayPaymentRequest({userId, cartId})
    );
  }

  public onValidateMerchant(event: { validationURL: string }): void {
    getUserIdCartId(this.userIdService, this.activeCartService)
    .subscribe(({userId, cartId}) => {
      this.checkoutComStore.dispatch(
        new ValidateApplePayMerchant({
          userId,
          cartId,
          validationURL: event.validationURL
        })
      );
    });
  }

  public onPaymentAuthorized(event: { payment: any }) {
    getUserIdCartId(this.userIdService, this.activeCartService)
    .subscribe(({userId, cartId}) => {
      this.checkoutComStore.dispatch(
        new AuthoriseApplePayPayment({
          userId,
          cartId,
          payment: event.payment
        })
      );
    });
  }

  onPaymentError() {
    this.globalMessageService.add(
      {key: 'paymentForm.applePay.cancelled'},
      GlobalMessageType.MSG_TYPE_ERROR
    );
  }
}
