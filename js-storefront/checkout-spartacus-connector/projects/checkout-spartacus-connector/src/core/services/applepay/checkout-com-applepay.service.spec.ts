import { TestBed } from '@angular/core/testing';

import { CheckoutComApplepayService } from './checkout-com-applepay.service';
import {
  ActiveCartService, Cart,
  GlobalMessageService,
  GlobalMessageType,
  Translatable,
  UserIdService
} from '@spartacus/core';
import { CHECKOUT_COM_FEATURE, StateWithCheckoutCom } from '../../store/checkout-com.state';
import { Store, StoreModule } from '@ngrx/store';
import { Observable, of } from 'rxjs';
import {
  AuthoriseApplePayPayment,
  AuthoriseApplePayPaymentSuccess,
  RequestApplePayPaymentRequest,
  RequestApplePayPaymentRequestSuccess,
  ValidateApplePayMerchant,
  ValidateApplePayMerchantSuccess
} from '../../store/checkout-com.actions';
import { ApplePayAuthorization, ApplePayPaymentRequest } from '../../model/ApplePay';
import { reducer } from '../../store/checkout-com.reducer';
import createSpy = jasmine.createSpy;

describe('CheckoutComApplepayService', () => {
  let service: CheckoutComApplepayService;
  let activeCartService: ActiveCartService;
  let userIdService: UserIdService;
  let checkoutComStore: Store<StateWithCheckoutCom>;
  let globalMessageService: GlobalMessageService;

  const userId = 'testUserId';
  const cartId = 'testCartId';

  class ActiveCartServiceStub {
    cartId = cartId;

    public getActiveCartId() {
      return of(this.cartId);
    }

    getActive(): Observable<Cart> {
      return of({
        code: cartId
      });
    }
  }

  class UserIdServiceStub implements Partial<UserIdService> {
    getUserId = createSpy('getUserId').and.returnValue(of(userId));
  }

  class MockGlobalMessageService implements Partial<GlobalMessageService> {
    add(text: string | Translatable, type: GlobalMessageType, timeout?: number): void {
      // nop
    }
  }

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        StoreModule.forRoot({}),
        StoreModule.forFeature(CHECKOUT_COM_FEATURE, reducer),
      ],
      providers: [
        {provide: ActiveCartService, useClass: ActiveCartServiceStub},
        {provide: UserIdService, useClass: UserIdServiceStub},
        {provide: GlobalMessageService, useClass: MockGlobalMessageService},
      ]
    });
    service = TestBed.inject(CheckoutComApplepayService);
    checkoutComStore = TestBed.inject(Store);
    userIdService = TestBed.inject(UserIdService);
    globalMessageService = TestBed.inject(GlobalMessageService);

    spyOn(checkoutComStore, 'dispatch').and.callThrough();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should dispatch events for requestApplePayPaymentRequest', () => {
    service.requestApplePayPaymentRequest(userId, cartId);

    expect(checkoutComStore.dispatch).toHaveBeenCalledWith(
      new RequestApplePayPaymentRequest({
        userId,
        cartId
      })
    );
  });

  it('should get payment request from state', (done) => {
    checkoutComStore.dispatch(
      new RequestApplePayPaymentRequestSuccess(
        {
          currencyCode: 'EUR',
          countryCode: 'NL'
        } as ApplePayPaymentRequest
      )
    );

    service.getPaymentRequestFromState()
      .subscribe((paymentRequest) => {
        expect(paymentRequest).not.toBeNull();

        expect(paymentRequest.currencyCode).toEqual('EUR');
        expect(paymentRequest.countryCode).toEqual('NL');
        done();
      })
      .unsubscribe();
  });

  it('should get merchant session from state', (done) => {
    const payload = {
      id: '1'
    };

    checkoutComStore.dispatch(
      new ValidateApplePayMerchantSuccess(payload)
    );

    service.getMerchantSesssionFromState()
      .subscribe((session) => {
        expect(session).toEqual(payload);

        done();
      })
      .unsubscribe();
  });

  it('should get merchant session from state', (done) => {
    const payload = {
      status: 'SUCCESS',
      orderData: {
        code: '1234',
      }
    } as ApplePayAuthorization;

    checkoutComStore.dispatch(
      new AuthoriseApplePayPaymentSuccess(payload)
    );

    service.getPaymentAuthorizationFromState()
      .subscribe((paymentAuthorization) => {
        expect(paymentAuthorization).toEqual(payload);

        done();
      })
      .unsubscribe();
  });

  it('should request payment request', () => {
    service.requestApplePayPaymentRequest(userId, cartId);

    expect(checkoutComStore.dispatch).toHaveBeenCalledWith(
      new RequestApplePayPaymentRequest({userId, cartId})
    );
  });

  it('should validate merchant', () => {
    const event = {
      validationURL: 'https://test.com/validate'
    };

    service.onValidateMerchant(event);

    expect(checkoutComStore.dispatch).toHaveBeenCalledWith(
      new ValidateApplePayMerchant({
        userId,
        cartId,
        validationURL: event.validationURL
      })
    );
  });

  it('should authorise payment', () => {
    const event = {
      payment: {
        total: '12345'
      }
    };

    service.onPaymentAuthorized(event);

    expect(checkoutComStore.dispatch).toHaveBeenCalledWith(
      new AuthoriseApplePayPayment({
        userId,
        cartId,
        payment: event.payment
      })
    );
  });

  it('should send global message on error', () => {
    spyOn(globalMessageService, 'add').and.stub();

    service.onPaymentError();

    expect(globalMessageService.add).toHaveBeenCalledWith(
      {key: 'paymentForm.applePay.cancelled'},
      GlobalMessageType.MSG_TYPE_ERROR
    );
  })
});
