import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CheckoutComPaymentFormComponent } from './checkout-com-payment-form.component';
import { StoreModule } from '@ngrx/store';
import { CheckoutComPaymentService } from '../../../core/services/checkout-com-payment.service';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { ReactiveFormsModule } from '@angular/forms';
import { FormErrorsModule, ICON_TYPE, ModalService } from '@spartacus/storefront';
import { CheckoutComFramesInputModule } from '../checkout-com-frames-input/checkout-com-frames-input.module';
import {
  Address,
  AddressValidation,
  CardType,
  Country,
  GlobalMessageService,
  I18nModule,
  I18nTestingModule,
  Occ, PaymentDetails,
  TranslationService,
  UserAddressService,
  UserPaymentService
} from '@spartacus/core';
import { NgSelectModule } from '@ng-select/ng-select';
import { CheckoutComFramesFormModule } from '../checkout-com-frames-form/checkout-com-frames-form.module';
import { Component, Input } from '@angular/core';
import { FrameCardTokenizedEvent } from '../checkout-com-frames-form/interfaces';
import { PaymentFormComponent } from '@spartacus/checkout/components';
import { CheckoutDeliveryFacade, CheckoutPaymentFacade } from '@spartacus/checkout/root';
import createSpy = jasmine.createSpy;
import Region = Occ.Region;
import { CheckoutComPaymentDetails } from '../../interfaces';

class CheckoutComPaymentStub {
  requestOccMerchantKey = createSpy('requestOccMerchantKey').and.stub();
  setPaymentAddress = createSpy('setPaymentAddress').and.stub();
  getOccMerchantKeyFromState = createSpy('getOccMerchantKeyFromState').and.returnValue(of('pk_test_d4727781-a79c-460e-9773-05d762c63e8f'));
  canSaveCard = createSpy('canSaveCard').and.returnValue(true);
}

@Component({
  selector: 'cx-spinner',
  template: '',
})
class MockSpinnerComponent {
}

const mockBillingCountries: Country[] = [
  {
    isocode: 'CA',
    name: 'Canada',
  },
];

const mockCardTypes: CardType[] = [
  {
    'code': 'amex',
    'name': 'American Express'
  },
  {
    'code': 'jcb',
    'name': 'JCB'
  },
  {
    'code': 'maestro',
    'name': 'Maestro'
  },
  {
    'code': 'undefined'
  },
  {
    'code': 'discover',
    'name': 'Discover'
  },
  {
    'code': 'switch',
    'name': 'Switch'
  },
  {
    'code': 'visa',
    'name': 'Visa'
  },
  {
    'code': 'master',
    'name': 'Mastercard'
  },
  {
    'code': 'mastercard',
    'name': 'Mastercard'
  },
  {
    'code': 'mastercard_eurocard',
    'name': 'Mastercard/Eurocard'
  },
  {
    'code': 'americanexpress',
    'name': 'American Express'
  },
  {
    'code': 'diners',
    'name': 'Diner\'s Club'
  },
  {
    'code': 'dinersclubinternational',
    'name': 'Diners Club International'
  }
];

@Component({
  selector: 'cx-billing-address-form',
  template: '',
})
class MockBillingAddressFormComponent {
  @Input() billingAddress: Address;
  @Input() countries$: Observable<Country[]>;
}

@Component({
  selector: 'cx-card',
  template: '',
})
class MockCardComponent {
  @Input()
  content: any;
}

@Component({
  selector: 'cx-icon',
  template: '',
})
class MockCxIconComponent {
  @Input() type: ICON_TYPE;
}

class MockCheckoutPaymentFacade {
  loadSupportedCardTypes = createSpy();

  getCardTypes(): Observable<CardType[]> {
    return of(mockCardTypes);
  }

  getSetPaymentDetailsResultProcess() {
    return of({loading: false});
  }
}

class MockCheckoutDeliveryService {
  getDeliveryAddress(): Observable<Address> {
    return of(null);
  }

  getAddressVerificationResults(): Observable<AddressValidation> {
    return of();
  }

  verifyAddress(_address: Address): void {
  }

  clearAddressVerificationResults(): void {
  }
}

class MockUserPaymentService {
  loadBillingCountries = createSpy();

  getAllBillingCountries(): Observable<Country[]> {
    return new BehaviorSubject(mockBillingCountries);
  }
}

class MockGlobalMessageService {
  add = createSpy();
}

const mockSuggestedAddressModalRef: any = {
  componentInstance: {
    enteredAddress: '',
    suggestedAddresses: '',
  },
  result: new Promise((resolve) => {
    return resolve(true);
  }),
};

class MockModalService {
  open(): any {
    return mockSuggestedAddressModalRef;
  }
}

class MockUserAddressService {
  getRegions(): Observable<Region[]> {
    return of([]);
  }
}

class MockTranslationService {
  translate(key: string) {
    return of(key);
  }
}

describe('CheckoutComPaymentFormComponent', () => {
  let component: CheckoutComPaymentFormComponent;
  let fixture: ComponentFixture<CheckoutComPaymentFormComponent>;
  let mockCheckoutDeliveryFacade: MockCheckoutDeliveryService;
  let mockCheckoutPaymentFacade: MockCheckoutPaymentFacade;
  let mockUserPaymentService: MockUserPaymentService;
  let mockGlobalMessageService: MockGlobalMessageService;
  let mockModalService: MockModalService;
  let mockUserAddressService: MockUserAddressService;

  beforeEach(async () => {
    mockCheckoutDeliveryFacade = new MockCheckoutDeliveryService();
    mockCheckoutPaymentFacade = new MockCheckoutPaymentFacade();
    mockUserPaymentService = new MockUserPaymentService();
    mockGlobalMessageService = new MockGlobalMessageService();
    mockModalService = new MockModalService();
    mockUserAddressService = new MockUserAddressService();

    await TestBed.configureTestingModule({
      declarations: [CheckoutComPaymentFormComponent,
        PaymentFormComponent,
        MockCardComponent,
        MockBillingAddressFormComponent,
        MockCxIconComponent,
        MockSpinnerComponent,
      ],
      imports: [StoreModule.forRoot({}),
        ReactiveFormsModule,
        CheckoutComFramesInputModule,
        CheckoutComFramesFormModule,
        I18nModule,
        NgSelectModule,
        I18nTestingModule,
        FormErrorsModule,
      ],
      providers: [
        {provide: CheckoutComPaymentService, useClass: CheckoutComPaymentStub},
        {provide: ModalService, useClass: MockModalService},
        {
          provide: CheckoutPaymentFacade,
          useValue: mockCheckoutPaymentFacade,
        },
        {
          provide: CheckoutDeliveryFacade,
          useValue: mockCheckoutDeliveryFacade,
        },
        {provide: UserPaymentService, useValue: mockUserPaymentService},
        {provide: GlobalMessageService, useValue: mockGlobalMessageService},
        {provide: UserAddressService, useValue: mockUserAddressService},
        {provide: TranslationService, useClass: MockTranslationService},
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CheckoutComPaymentFormComponent);

    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should convert card type from frames to spartacus', () => {
    fixture.detectChanges();
    expect(component.getCardTypeFromTokenizedEvent('Visa')).toEqual({code: 'visa', name: 'Visa'});
    expect(component.getCardTypeFromTokenizedEvent('Mastercard')).toEqual({code: 'master', name: 'Mastercard'});
    expect(component.getCardTypeFromTokenizedEvent('Diners Club International')).toEqual({
      code: 'dinersclubinternational',
      name: 'Diners Club International'
    });
    expect(component.getCardTypeFromTokenizedEvent('Maestro')).toEqual({code: 'maestro', name: 'Maestro'});
    expect(component.getCardTypeFromTokenizedEvent('Discover')).toEqual({code: 'discover', name: 'Discover'});
  });


  it('should get translations for frames placeholders', (done) => {
    component.framesLocalization$
      .subscribe(res => {
        expect(res).toEqual({
          cardNumberPlaceholder: 'paymentForm.frames.placeholders.cardNumberPlaceholder',
          expiryMonthPlaceholder: 'paymentForm.frames.placeholders.expiryMonthPlaceholder',
          expiryYearPlaceholder: 'paymentForm.frames.placeholders.expiryYearPlaceholder',
          cvvPlaceholder: 'paymentForm.frames.placeholders.cvvPlaceholder',
        });

        done();
      })
      .unsubscribe();
  });

  describe('tokenized', () => {

    const tokenizedEvent: FrameCardTokenizedEvent = {
      bin: '4242',
      card_type: 'Credit',
      card_category: 'Consumer',
      expires_on: '12/25',
      expiry_month: '12',
      expiry_year: '25',
      last4: '1111',
      type: 'visa',
      token: 'somesessionstoken',
      scheme: 'Visa',
    };

    beforeEach(() => {
      component.paymentForm.setValue({
        accountHolderName: 'Ronnie James Dio',
        save: false,
        defaultPayment: true,
        cardNumber: '42424242424242',
        expiryDate: '12/24',
        cvn: '100',
      });
    });

    it('should trigger cardHolder event', (done) => {
      const name = 'Erik Slagter';
      component.framesCardholder$
        .subscribe((cardHolderEvent) => {
          expect(cardHolderEvent).toEqual({
            name
          });

          done();
        });

      component.paymentForm.get('accountHolderName').setValue(name);
    });

    it('should handle tokenize and call setPaymentDetails', (done) => {
      component.sameAsShippingAddress = true;

      component.setPaymentDetails
        .subscribe((paymentDetailsEvent) => {
          const {paymentDetails, billingAddress} = paymentDetailsEvent;
          expect(paymentDetails).toEqual({
            accountHolderName: 'Ronnie James Dio',
            saved: false,
            defaultPayment: true,
            addressLine1: undefined,
            addressLine2: undefined,
            city: undefined,
            country: undefined,
            postalCode: undefined,
            billingAddress: null,
            cardNumber: '4242******1111',
            cardType: {
              code: 'visa', name: 'Visa'
            },
            expiryMonth: '12',
            expiryYear: '25',
            paymentToken: 'somesessionstoken',
            type: 'VISA',
            cardBin: '4242',
          });
          expect(billingAddress).toBeNull();

          done();
        });

      component.tokenized(tokenizedEvent);
    });

    it('should handle tokenize with billing address and call setPaymentDetails', (done) => {
      const billingAddress = {
        firstName: 'Ronnie James',
        lastName: 'Dio',
        line1: 'line 1',
        line2: 'line 2',
        postalCode: '46001',
        town: 'SameVille',
        region: {
          isocodeShort: 'xx',
        },
        country: {
          isocode: 'ES'
        } as Country,
      };
      component.sameAsShippingAddress = false;
      component.billingAddressForm.setValue(billingAddress);

      component.setPaymentDetails
        .subscribe((paymentDetailsEvent: {paymentDetails: CheckoutComPaymentDetails, billingAddress: Address }) => {
          const {paymentDetails, billingAddress} = paymentDetailsEvent;

          expect(component.billingAddressForm.valid).toBeTrue();

          expect(paymentDetails).toEqual({
            accountHolderName: 'Ronnie James Dio',
            saved: false,
            defaultPayment: true,
            addressLine1: 'line 1',
            addressLine2: 'line 2',
            city: 'SameVille',
            country: {
              isocode: 'ES'
            },
            postalCode: '46001',
            billingAddress,
            cardNumber: '4242******1111',
            cardType: {
              code: 'visa', name: 'Visa'
            },
            expiryMonth: '12',
            expiryYear: '25',
            paymentToken: 'somesessionstoken',
            type: 'VISA',
            cardBin: '4242',
          } as any);
          expect(billingAddress).toEqual(billingAddress);

          done();
        });

      component.tokenized(tokenizedEvent);
    });
  })

});
