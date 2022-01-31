import { TestBed } from '@angular/core/testing';

import { CheckoutComCheckoutService } from './checkout-com-checkout.service';
import { Store, StoreModule } from '@ngrx/store';
import { CHECKOUT_COM_FEATURE, CheckoutComState } from '../store/checkout-com.state';
import { ActiveCartService, Cart, Order, UserIdService } from '@spartacus/core';
import { Observable, of } from 'rxjs';
import { reducer } from '../store/checkout-com.reducer';
import * as CheckoutComActions from '../store/checkout-com.actions';
import { RedirectPlaceOrderAuthorize } from '../store/checkout-com.actions';
import createSpy = jasmine.createSpy;
import { CheckoutComRedirect } from '../interfaces';

describe('CheckoutComCheckoutService', () => {
  let service: CheckoutComCheckoutService;
  let checkoutComStore: Store<CheckoutComState>;
  let userIdService: UserIdService;
  let activeCartService: ActiveCartService;

  const userId = 'current';
  const cartId = '0000000';

  class ActiveCartServiceStub {
    cartId = cartId;

    public getActiveCartId() {
      return of(this.cartId);
    }

    getActive(): Observable<Cart> {
      return of({guid: this.cartId, code: this.cartId});
    }
  }

  class UserIdServiceStub implements Partial<UserIdService> {
    getUserId = createSpy('getUserId').and.returnValue(of(userId));
  }

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [StoreModule.forRoot({}), StoreModule.forFeature(CHECKOUT_COM_FEATURE, reducer)],
      providers: [
        {provide: ActiveCartService, useClass: ActiveCartServiceStub},
        {provide: UserIdService, useClass: UserIdServiceStub},
      ]
    });

    service = TestBed.inject(CheckoutComCheckoutService);

    activeCartService = TestBed.inject(ActiveCartService);
    userIdService = TestBed.inject(UserIdService);
    checkoutComStore = TestBed.inject(Store);

    spyOn(checkoutComStore, 'dispatch').and.callThrough();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should dispatch place order event', () => {
    service.placeOrder(true);

    expect(checkoutComStore.dispatch).toHaveBeenCalledWith(
      new CheckoutComActions.PlaceOrder({
        userId,
        cartId,
        termsChecked: true
      })
    );
  });

  it('should authorize redirect place order', () => {
    service.authorizeOrder('session-id', userId, cartId);

    expect(checkoutComStore.dispatch).toHaveBeenCalledWith(
      new RedirectPlaceOrderAuthorize({
        userId,
        cartId,
        sessionId: 'session-id'
      })
    );
  });

  describe('order result', () => {

    it('should get order result from state', (done) => {
      const order: Order = {
        code: '124',
      };

      checkoutComStore.dispatch(
        new CheckoutComActions.PlaceOrderSuccess(order)
      );

      service.getOrderResultFromState().subscribe(res => {
        expect(res.order).toEqual(order);
        expect(res.successful).toBeTrue();
        done()
      }).unsubscribe();
    });

    it('should get order redirect result from state', (done) => {
      const payload: CheckoutComRedirect = {
        redirectUrl: 'https://www.placekitten.com',
        type: 'sometype'
      };

      checkoutComStore.dispatch(
        new CheckoutComActions.PlaceOrderRedirect(payload)
      );

      service.getOrderResultFromState().subscribe(res => {
        expect(res.order).toBeFalsy();
        expect(res.successful).toBeFalsy();
        expect(res.redirect).toEqual(payload);
        done()
      }).unsubscribe();
    });
  });

});
