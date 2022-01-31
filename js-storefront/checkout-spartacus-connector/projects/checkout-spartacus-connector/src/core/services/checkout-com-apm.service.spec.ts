import { TestBed } from '@angular/core/testing';

import { CheckoutComApmService } from './checkout-com-apm.service';
import { Store, StoreModule } from '@ngrx/store';
import * as CheckoutComActions from '../store/checkout-com.actions';
import { AuthoriseGooglePayPayment, RequestAvailableApmsSuccess, SetSelectedApm } from '../store/checkout-com.actions';
import { CHECKOUT_COM_FEATURE, CheckoutComState } from '../store/checkout-com.state';
import { ActiveCartService, Cart, CmsService, GlobalMessageService, UserIdService } from '@spartacus/core';
import { Observable, of } from 'rxjs';
import { OccCmsComponentWithMedia } from '../model/ComponentData';
import { reducer } from '../store/checkout-com.reducer';
import { ApmData, PaymentType } from '../model/ApmData';
import createSpy = jasmine.createSpy;

describe('CheckoutComApmService', () => {
  let service: CheckoutComApmService;
  let checkoutComStore: Store<CheckoutComState>;
  let userIdService: UserIdService;
  let activeCartService: ActiveCartService;

  const userId = 'current';
  const cartId = '0000000';
  let mockComponent: OccCmsComponentWithMedia = {
    uid: 'component-uid',
    name: 'component-name'
  };

  class MockGlobalMessageService {
    add = createSpy();
  }

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

  class CmsServiceStub {
    getComponentData = createSpy('getComponentData').and.returnValue(of(mockComponent));
  }

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [StoreModule.forRoot({}), StoreModule.forFeature(CHECKOUT_COM_FEATURE, reducer)],
      providers: [
        {provide: ActiveCartService, useClass: ActiveCartServiceStub},
        {provide: UserIdService, useClass: UserIdServiceStub},
        {provide: CmsService, useClass: CmsServiceStub},
        {provide: GlobalMessageService, useClass: MockGlobalMessageService},
      ]
    });

    service = TestBed.inject(CheckoutComApmService);

    activeCartService = TestBed.inject(ActiveCartService);
    userIdService = TestBed.inject(UserIdService);
    checkoutComStore = TestBed.inject(Store);
    checkoutComStore.select = () => of({});
    spyOn(checkoutComStore, 'dispatch').and.callThrough();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should dispatch RequestAvailableApms event', (done) => {
    service.requestAvailableApms().subscribe(() => {
      expect(checkoutComStore.dispatch).toHaveBeenCalledWith(
        new CheckoutComActions.RequestAvailableApms({cartId, userId})
      );
      done();
    });
  });

  it('should dispatch SetKlarnaInitParams event', (done) => {
    service.getKlarnaInitParams().subscribe(() => {
      expect(checkoutComStore.dispatch).toHaveBeenCalledWith(
        new CheckoutComActions.SetKlarnaInitParams({cartId, userId})
      );
      done();
    });
  });

  it('should get available apms from state', (done) => {
    const apms: ApmData[] = [
      {code: PaymentType.iDeal, isRedirect: false, isUserDataRequired: true},
      {code: PaymentType.PayPal, isUserDataRequired: false},
      {code: PaymentType.Sepa, isRedirect: false, isUserDataRequired: true},
    ];

    checkoutComStore.dispatch(new RequestAvailableApmsSuccess(apms));

    service.getAvailableApmsFromState()
      .subscribe(res => {
        expect(res).toEqual(apms);
        done()
      })
      .unsubscribe();
  });

  it('should get Selected apm from state', (done) => {
    const apm = {code: PaymentType.PayPal};

    checkoutComStore.dispatch(new SetSelectedApm(apm));

    service.getSelectedApmFromState()
      .subscribe((res) => {
        expect(res).toEqual(apm);
        done()
      })
      .unsubscribe();
  });

  it('should get loading state', (done) => {
    const apm = {code: PaymentType.PayPal};

    checkoutComStore.dispatch(
      new AuthoriseGooglePayPayment({
        userId,
        cartId,
        token: 'token',
        billingAddress: {},
        savePaymentMethod: false
      })
    );

    service.getIsApmLoadingFromState()
      .subscribe((res) => {
        expect(res).toBeTrue();
        done()
      })
      .unsubscribe();
  });

  it('should dispatch event setting selected apm', () => {
    const apm = {
      code: PaymentType.Card
    };

    service.selectApm(apm);

    expect(checkoutComStore.dispatch).toHaveBeenCalledWith(
      new SetSelectedApm(apm)
    )
  })
});
