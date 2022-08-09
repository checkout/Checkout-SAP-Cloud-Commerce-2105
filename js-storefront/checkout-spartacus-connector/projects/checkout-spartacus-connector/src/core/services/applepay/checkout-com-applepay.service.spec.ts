import {TestBed} from '@angular/core/testing';

import {CheckoutComApplepayService} from './checkout-com-applepay.service';
import {
  ActiveCartService, Cart,
  GlobalMessageService,
  GlobalMessageType,
  Translatable,
  UserIdService
} from '@spartacus/core';
import {CHECKOUT_COM_FEATURE, StateWithCheckoutCom} from '../../store/checkout-com.state';
import {Store, StoreModule} from '@ngrx/store';
import {Observable, of} from 'rxjs';
import {
  AuthoriseApplePayPayment,
  AuthoriseApplePayPaymentSuccess,
  RequestApplePayPaymentRequest,
  RequestApplePayPaymentRequestSuccess,
  ValidateApplePayMerchant,
  ValidateApplePayMerchantSuccess,
  SelectApplePayShippingMethod,
  SelectApplePayDeliveryAddress,
  SelectApplePayDeliveryAddressSuccess,
  SelectApplePayShippingMethodSuccess
} from '../../store/checkout-com.actions';
import {
  ApplePayAuthorization,
  ApplePayPaymentRequest,
  ApplePayShippingContactUpdate,
  ApplePayLineItem,
  ApplePayShippingMethodUpdate, ApplePayShippingMethod, ApplePayPaymentContact
} from '../../model/ApplePay';
import {reducer} from '../../store/checkout-com.reducer';
import createSpy = jasmine.createSpy;

describe('CheckoutComApplepayService', () => {
  let service: CheckoutComApplepayService;
  let activeCartService: ActiveCartService;
  let userIdService: UserIdService;
  let checkoutComStore: Store<StateWithCheckoutCom>;
  let globalMessageService: GlobalMessageService;

  const userId = 'testUserId';
  const cartId = 'testCartId';

  const newTotal = {
    type: 'type_test',
    label: 'label_test',
    amount: 'amount_test'
  }


  class MockCheckoutComApplepayService implements Partial<CheckoutComApplepayService> {
    onShippingMethodSelected(event: { shippingMethod: ApplePayShippingMethod }) {
      checkoutComStore.dispatch(
        new SelectApplePayShippingMethod({
          cartId: cartId,
          userId: userId,
          shippingMethod: event.shippingMethod,
        })
      );
    }

    getDeliveryMethodUpdateFromState(): Observable<ApplePayShippingMethodUpdate> {
      return of({
        newTotal,
        newLineItems: undefined
      })
    }

    getPaymentRequestFromState(): Observable<ApplePayPaymentRequest> {
      return of({
        currencyCode: 'EUR',
        countryCode: 'NL',
        supportedNetworks: ['supportedNetworks_test'],
        merchantCapabilities: ['merchantCapabilities_test'],
        total: newTotal,
        requiredBillingContactFields: ['requiredBillingContactFields_test'],
        requiredShippingContactFields: ['requiredShippingContactFields_test'],
      })
    }

    getMerchantSesssionFromState(): Observable<any> {
      return of({
        id: '1',
      })
    }

    getPaymentAuthorizationFromState(): Observable<ApplePayAuthorization> {
      return of({
        status: 'SUCCESS',
        orderData: {
          code: '1234',
        }
      })
    }

    getDeliveryAddressUpdateFromState(): Observable<ApplePayShippingContactUpdate> {
      return of({
        newTotal,
        newLineItems: undefined,
        errors: undefined,
        newShippingMethods: undefined
      })
    }

    requestApplePayPaymentRequest(userId: string, cartId: string): void {
      checkoutComStore.dispatch(
        new RequestApplePayPaymentRequest({userId, cartId})
      );
    }

    onValidateMerchant(event: { validationURL: string }): void {
      checkoutComStore.dispatch(
        new ValidateApplePayMerchant({
          cartId: cartId,
          userId: userId,
          validationURL: event.validationURL
        })
      );
    }

    onShippingContactSelected(event: { shippingContact: ApplePayPaymentContact }) {
      checkoutComStore.dispatch(
        new SelectApplePayDeliveryAddress({
          cartId: cartId,
          userId: userId,
          shippingContact: event.shippingContact
        })
      );
    }

    onPaymentAuthorized(event: { payment: any }) {
      checkoutComStore.dispatch(
        new AuthoriseApplePayPayment({
          cartId: cartId,
          userId: userId,
          payment: event.payment
        })
      );
    }

    onPaymentError() {
      globalMessageService.add(
        {key: 'paymentForm.applePay.cancelled'},
        GlobalMessageType.MSG_TYPE_ERROR
      );
    }
  }

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
        {provide: CheckoutComApplepayService, useClass: MockCheckoutComApplepayService},
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

  it('should get delivery address update from state', (done) => {
    const payload = {
      newTotal,
      newLineItems: undefined,
      errors: undefined,
      newShippingMethods: undefined
    } as ApplePayShippingContactUpdate;

    checkoutComStore.dispatch(
      new SelectApplePayDeliveryAddressSuccess(payload)
    );

    service.getDeliveryAddressUpdateFromState()
      .subscribe((update) => {
        expect(update).toEqual(payload);

        done();
      })
      .unsubscribe();
  });

  it('should get delivery method update from state', (done) => {
    const payload = {
      newTotal,
      newLineItems: undefined
    } as ApplePayShippingMethodUpdate;

    checkoutComStore.dispatch(
      new SelectApplePayShippingMethodSuccess(payload)
    );

    service.getDeliveryMethodUpdateFromState()
      .subscribe((update) => {
        expect(update).toEqual(payload);

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

  it('should select shipping method', () => {
    const event = {
      shippingMethod: {
        label: 'label_test',
        detail: 'detail_test',
        identifier: 'identifier_test',
        amount: 'amount_test',
        dateComponentsRange: undefined
      }
    };

    service.onShippingMethodSelected(event);

    expect(checkoutComStore.dispatch).toHaveBeenCalledWith(
      new SelectApplePayShippingMethod({
        userId,
        cartId,
        shippingMethod: event.shippingMethod
      })
    );
  });

  it('should select shipping contact', () => {
    const event = {
      shippingContact: {
        phoneNumber: 'phoneNumber_test',
        emailAddress: 'emailAddress_test',
        givenName: 'givenName_test',
        familyName: 'familyName_test',
        phoneticGivenName: 'phoneticGivenName_test',
        phoneticFamilyName: 'phoneticFamilyName_test',
        addressLines: [
          'addressLine1_test'
        ],
        subLocality: 'subLocality_test',
        locality: 'locality_test',
        postalCode: 'postalCode_test',
        subAdministrativeArea: 'subAdministrativeArea_test',
        administrativeArea: 'administrativeArea_test',
        country: 'country_test',
        countryCode: 'countryCode_test'
      }
    };

    service.onShippingContactSelected(event);

    expect(checkoutComStore.dispatch).toHaveBeenCalledWith(
      new SelectApplePayDeliveryAddress({
        userId,
        cartId,
        shippingContact: event.shippingContact
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
