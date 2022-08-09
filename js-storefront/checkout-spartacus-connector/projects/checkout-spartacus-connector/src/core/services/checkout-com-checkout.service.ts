import { Injectable } from '@angular/core';
import { ActiveCartService, StateWithProcess, UserIdService } from '@spartacus/core';
import { CheckoutService, StateWithCheckout } from '@spartacus/checkout/core';
import { Store } from '@ngrx/store';
import { CheckoutComOrderResult, StateWithCheckoutCom } from '../store/checkout-com.state';
import * as CheckoutComActions from '../store/checkout-com.actions';
import { RedirectPlaceOrderAuthorize } from '../store/checkout-com.actions';
import { Observable } from 'rxjs';
import { getPlaceOrder } from '../store/checkout-com.selectors';
import { first, map, take } from 'rxjs/operators';
import { getUserIdCartId } from '../shared/get-user-cart-id';

@Injectable({
  providedIn: 'root'
})
export class CheckoutComCheckoutService extends CheckoutService {
  constructor(
    protected checkoutStore: Store<StateWithCheckout>,
    protected checkoutComStore: Store<StateWithCheckoutCom>,
    protected activeCartService: ActiveCartService,
    protected userIdService: UserIdService,
    protected processStateStore: Store<StateWithProcess<void>>,
  ) {
    super(
      checkoutStore,
      processStateStore,
      activeCartService,
      userIdService
    );
  }

  placeOrder(termsChecked: boolean) {
    if (this.actionAllowed()) {
      getUserIdCartId(this.userIdService, this.activeCartService) // do not replace this with subscribe.unsubscribe trick, see CHECK-433
      .pipe(take(1))
      .subscribe(({userId, cartId}) => {
        if (userId && cartId) {
          this.checkoutComStore.dispatch(
            new CheckoutComActions.PlaceOrder({
              userId,
              cartId,
              termsChecked,
            })
          );
        }
      }, err => console.error('placeOrder with errors', {err}));
    }
  }

  /**
   * Send the session id from Checkout.com to the backend for order creation
   *
   * @param sessionId the session id from redirect
   * @param userId user ID
   * @param cartId cart ID
   */
  public authorizeOrder(sessionId: string, userId: string, cartId: string): Observable<boolean> {
    this.checkoutComStore.dispatch(
      new RedirectPlaceOrderAuthorize({
        userId,
        cartId,
        sessionId
      })
    );

    return this.getOrderDetails().pipe(
      first(result => result != null),
      map(orderDetails => !!orderDetails)
    );
  }

  public getOrderResultFromState(): Observable<CheckoutComOrderResult> {
    return this.checkoutComStore.select(getPlaceOrder).pipe(
      first(result => result != null)
    );
  }
}
