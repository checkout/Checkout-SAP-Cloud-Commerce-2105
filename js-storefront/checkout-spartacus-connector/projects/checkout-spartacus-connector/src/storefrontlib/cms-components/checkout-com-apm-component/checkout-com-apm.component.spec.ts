import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CheckoutComApmComponent } from './checkout-com-apm.component';
import { CheckoutComApmService } from '../../../core/services/checkout-com-apm.service';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { ApmData, PaymentType } from '../../../core/model/ApmData';
import { FormBuilder } from '@angular/forms';
import {
  ActiveCartService,
  Cart,
  Currency,
  CurrencyService,
  GlobalMessageService,
  MockTranslatePipe,
  UserAddressService,
  UserIdService,
  UserPaymentService
} from '@spartacus/core';
import { take } from 'rxjs/operators';
import { CheckoutComGooglepayService } from '../../../core/services/googlepay/checkout-com-googlepay.service';
import { GooglePayMerchantConfiguration } from '../../../core/model/GooglePay';
import { CheckoutComApplepayService } from '../../../core/services/applepay/checkout-com-applepay.service';
import { ApplePayPaymentRequest } from '../../../core/model/ApplePay';
import { CheckoutDeliveryFacade } from '@spartacus/checkout/root';
import createSpy = jasmine.createSpy;

class MockActiveCartService {
  isGuestCart(): boolean {
    return false;
  }

  getActiveCartId() {
    return of('0001');
  }

  getActive(): Observable<Cart> {
    return of({guid: '010-001', code: '0001'});
  }
}

class MockUserIdService {
  getUserId() {
    return of('current');
  }
}

class MockUserAddressService {
  getRegions() {
    return of();
  }
}

class MockUserPaymentService {
  getAllBillingCountries() {
    return of();
  }
}

class MockCheckoutDeliveryFacade {
  getDeliveryAddress() {
    return of();
  }
}

class MockCurrencyService {
  getActive() {
    return of({
      active: true, isocode: 'en_GB'
    } as Currency);
  }
}

const card = {code: PaymentType.Card};
const mockApms: ApmData[] = [
  {code: PaymentType.Sofort, isRedirect: true, isUserDataRequired: false, name: 'Sofort'},
  {code: PaymentType.PayPal, isRedirect: false, isUserDataRequired: false, name: 'PayPal'}
];

const apmState = new BehaviorSubject<ApmData>(null);

class CheckoutComApmServiceStub {
  getIsApmLoadingFromState = createSpy('getIsApmLoadingFromState').and.returnValue(of(false));

  requestAvailableApms(): Observable<ApmData[]> {
    return of([{code: PaymentType.Klarna}, {code: PaymentType.PayPal}])
  }

  getAvailableApmsFromState(): Observable<ApmData[]> {
    return of(mockApms);
  }

  getSelectedApmFromState(): Observable<ApmData> {
    return apmState.asObservable();
  }

  getApmByComponent(): Observable<ApmData> {
    return of(card);
  }

  selectApm(apm: ApmData): void {
    apmState.next(apm);
  }
}

const mockApplePayRequest: ApplePayPaymentRequest = {
  countryCode: 'US',
  currencyCode: 'USD',
  requiredBillingContactFields: [
    'postal'
  ],
  total: {
    amount: '123.00',
    label: 'Beans with toast',
    type: 'FINAL'
  },
  supportedNetworks: [],
  merchantCapabilities: []
};

class CheckoutComApplepayServiceStub {
  requestApplePayPaymentRequest = createSpy('requestApplePayPaymentRequest').and.stub();
  getPaymentRequestFromState = createSpy('getPaymentRequestFromState').and.returnValue(of(mockApplePayRequest));
}

class MockCheckoutComGooglepayService {
  getMerchantConfigurationFromState = createSpy('getMerchantConfigurationFromState').and.returnValue(of({
    'baseCardPaymentMethod': {
      'parameters': {
        'allowedAuthMethods': ['PAN_ONLY', 'CRYPTOGRAM_3DS'],
        'allowedCardNetworks': ['AMEX', 'DISCOVER', 'MASTERCARD', 'JCB', 'VISA', 'INTERAC'],
        'billingAddressParameters': {
          'format': 'FULL'
        },
        'billingAddressRequired': true
      },
      'type': 'CARD'
    },
    'clientSettings': {
      'environment': 'TEST'
    },
    'gateway': 'checkoutltd',
    'gatewayMerchantId': 'pk_test_c59321e8-953d-464d-bcfc-bb8785d05001',
    'merchantId': '01234567890123456789',
    'merchantName': 'e2yCheckoutCom',
    'transactionInfo': {
      'currencyCode': 'USD',
      'totalPrice': '16.99',
      'totalPriceStatus': 'FINAL'
    }
  } as GooglePayMerchantConfiguration));
  requestMerchantConfiguration = createSpy('requestMerchantConfiguration').and.stub();
  authoriseOrder = createSpy('authoriseOrder').and.stub();
  createInitialPaymentRequest = createSpy('createInitialPaymentRequest').and.stub();
  createFullPaymentRequest = createSpy('createFullPaymentRequest').and.stub();
}

class MockGlobalMessageService {
  add = createSpy();
}

describe('CheckoutComApmComponent', () => {
  let component: CheckoutComApmComponent;
  let fixture: ComponentFixture<CheckoutComApmComponent>;
  let checkoutComApmService: CheckoutComApmService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CheckoutComApmComponent, MockTranslatePipe],
      providers: [
        {provide: CheckoutComApmService, useClass: CheckoutComApmServiceStub},
        {provide: FormBuilder, useClass: FormBuilder},
        {provide: UserAddressService, useClass: MockUserAddressService},
        {provide: UserPaymentService, useClass: MockUserPaymentService},
        {provide: CheckoutDeliveryFacade, useClass: MockCheckoutDeliveryFacade},
        {provide: CurrencyService, useClass: MockCurrencyService},
        {provide: CheckoutComGooglepayService, useClass: MockCheckoutComGooglepayService},
        {provide: CurrencyService, useClass: MockCurrencyService},
        {provide: CheckoutComApplepayService, useClass: CheckoutComApplepayServiceStub},
        {provide: ActiveCartService, useClass: MockActiveCartService},
        {provide: UserIdService, useClass: MockUserIdService},
        {provide: GlobalMessageService, useClass: MockGlobalMessageService},
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CheckoutComApmComponent);

    checkoutComApmService = TestBed.inject(CheckoutComApmService);
    apmState.next(null);

    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create and request available apms', () => {
    expect(component).toBeTruthy();

    let apms;
    component.availableApms$.subscribe(res => apms = res).unsubscribe();

    expect(apms).toBe(mockApms);
  });

  it('should preset card as the default APM', () => {
    let selectedApm: ApmData;

    component.selectedApm$.subscribe(res => selectedApm = res).unsubscribe();

    expect(selectedApm.code).toEqual(PaymentType.Card);
  });

  it('should get card component', () => {
    let cardApm: ApmData;
    component.card$.subscribe(res => cardApm = res);
    expect(cardApm).toEqual(card);
  });

  it('should selected the payment details and billing addr', (done) => {
    const newApm = mockApms[1];

    component.setPaymentDetails.pipe(take(1)).subscribe((e: any) => {
      expect(e.paymentDetails.type).toEqual(newApm.code);
      expect(e.billingAddress).toBeNull();
      done();
    });

    checkoutComApmService.selectApm(newApm);
    component.sameAsShippingAddress$.next(true);
    component.selectApmPaymentDetails();

    fixture.detectChanges();

  });

  describe('should show or hide billing / continue button', () => {

    const options = [
      {code: PaymentType.Card, show: false},
      {code: PaymentType.GooglePay, show: false},
      {code: PaymentType.ApplePay, show: false},
      {code: PaymentType.Klarna, show: false},
      {code: PaymentType.Sepa, show: false},
      {code: PaymentType.PayPal, show: true},
      {code: PaymentType.Sofort, show: true}
    ];

    options.forEach((option) => {
      it(`${option.code} show ${option.show}`, () => {
        expect(component.showBillingFormAndContinueButton(option.code)).toEqual(option.show);
      });
    });
  });
});
