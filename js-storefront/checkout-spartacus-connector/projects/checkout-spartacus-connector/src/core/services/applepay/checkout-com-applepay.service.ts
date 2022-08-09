import {Injectable} from '@angular/core';
import {StateWithCheckoutCom} from '../../store/checkout-com.state';
import {select, Store} from '@ngrx/store';
import {ActiveCartService, GlobalMessageService, GlobalMessageType, UserIdService, WindowRef} from '@spartacus/core';
import {Observable, Subscription} from 'rxjs';
import {
  ApplePayAuthorization,
  ApplePayPaymentContact,
  ApplePayPaymentRequest,
  ApplePayShippingContactUpdate,
  ApplePayShippingMethod,
  ApplePayShippingMethodUpdate
} from '../../model/ApplePay';
import {createApplePaySession} from './applepay-session';
import {
  AuthoriseApplePayPayment,
  RequestApplePayPaymentRequest,
  SelectApplePayDeliveryAddress,
  SelectApplePayShippingMethod,
  StartApplePaySession,
  ValidateApplePayMerchant
} from '../../store/checkout-com.actions';
import {
  getApplePayDeliveryAddressUpdate,
  getApplePayDeliveryMethodUpdate,
  getApplePayMerchantSession,
  getApplePayPaymentAuthorization,
  getApplePayPaymentRequest
} from '../../store/checkout-com.selectors';

@Injectable({
  providedIn: 'root'
})
export class CheckoutComApplepayService {
  private deliveryAddress$: Subscription;
  private deliveryMethod$: Subscription;
  protected cartId;
  protected userId;

  constructor(
    protected checkoutComStore: Store<StateWithCheckoutCom>,
    protected activeCartService: ActiveCartService,
    protected userIdService: UserIdService,
    protected globalMessageService: GlobalMessageService,
    protected windowRef: WindowRef) {

  }

  createSession(paymentRequest: ApplePayPaymentRequest, cartId?: string, userId?: string): any {
    if (!this.windowRef.isBrowser()) {
      return;
    }
    this.userId = userId;
    this.cartId = cartId;
    const ApplePaySession = createApplePaySession(this.windowRef);
    const session = new ApplePaySession(5, paymentRequest);
    session.onvalidatemerchant = this.onValidateMerchant.bind(this);
    session.onpaymentauthorized = this.onPaymentAuthorized.bind(this);
    session.onshippingmethodselected = this.onShippingMethodSelected.bind(this);
    session.onshippingcontactselected = this.onShippingContactSelected.bind(this);

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

  getDeliveryAddressUpdateFromState(): Observable<ApplePayShippingContactUpdate> {
    return this.checkoutComStore.pipe(
      select(getApplePayDeliveryAddressUpdate)
    );
  }

  getDeliveryMethodUpdateFromState(): Observable<ApplePayShippingMethodUpdate> {
    return this.checkoutComStore.pipe(
      select(getApplePayDeliveryMethodUpdate)
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
    this.checkoutComStore.dispatch(
      new ValidateApplePayMerchant({
        cartId: this.cartId,
        userId: this.userId,
        validationURL: event.validationURL
      })
    );
  }

  /**
   * Called when the ApplePay user selects a different shipping method
   *
   * @param event
   */
  public onShippingMethodSelected(event: { shippingMethod: ApplePayShippingMethod }) {
    this.checkoutComStore.dispatch(
      new SelectApplePayShippingMethod({
        cartId: this.cartId,
        userId: this.userId,
        shippingMethod: event.shippingMethod,
      })
    );
  }

  /**
   * Called when the ApplePay user selects a different delivery address
   *
   * @param event
   */
  public onShippingContactSelected(event: { shippingContact: ApplePayPaymentContact }) {
    this.checkoutComStore.dispatch(
      new SelectApplePayDeliveryAddress({
        cartId: this.cartId,
        userId: this.userId,
        shippingContact: event.shippingContact
      })
    );
  }

  public onPaymentAuthorized(event: { payment: any }) {
    this.checkoutComStore.dispatch(
      new AuthoriseApplePayPayment({
        cartId: this.cartId,
        userId: this.userId,
        payment: event.payment
      })
    );
  }

  onPaymentError() {
    this.globalMessageService.add(
      {key: 'paymentForm.applePay.cancelled'},
      GlobalMessageType.MSG_TYPE_ERROR
    );

    try {
      this.deliveryAddress$?.unsubscribe();
      this.deliveryMethod$?.unsubscribe();
    } catch (err) {
      // nop
    }
  }
}
