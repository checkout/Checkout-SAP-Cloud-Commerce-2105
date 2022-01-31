import { Component, Input, Type } from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { ActivatedRoute } from '@angular/router';
import {
  ActiveCartService,
  Address,
  GlobalMessageService,
  GlobalMessageType,
  I18nTestingModule,
  PaymentDetails,
  UserIdService,
  UserPaymentService,
} from '@spartacus/core';

import { BehaviorSubject, Observable, of } from 'rxjs';
import { CheckoutComPaymentMethodComponent } from './checkout-com-payment-method.component';
import { CardComponent, ICON_TYPE } from '@spartacus/storefront';
import { CheckoutComPaymentService } from '../../../core/services/checkout-com-payment.service';
import { CheckoutComPaymentDetails } from '../../interfaces';
import { CheckoutComApmService } from '../../../core/services/checkout-com-apm.service';
import { ApmData, PaymentType } from '../../../core/model/ApmData';
import { CheckoutStepService } from '@spartacus/checkout/components';
import createSpy = jasmine.createSpy;
import { CheckoutDeliveryFacade, CheckoutFacade, CheckoutPaymentFacade } from '@spartacus/checkout/root';

@Component({
  selector: 'cx-icon',
  template: '',
})
class MockCxIconComponent {
  @Input() type: ICON_TYPE;
}

const mockPaymentDetails: PaymentDetails = {
  id: 'mock payment id',
  accountHolderName: 'Name',
  cardNumber: '123456789',
  cardType: {
    code: 'Visa',
    name: 'Visa',
  },
  expiryMonth: '01',
  expiryYear: '2022',
  cvn: '123',
};

class MockUserPaymentService {
  loadPaymentMethods(): void {}

  getPaymentMethods(): Observable<PaymentDetails[]> {
    return of();
  }

  getPaymentMethodsLoading(): Observable<boolean> {
    return of();
  }
}

class MockCheckoutFacade {
  clearCheckoutStep = createSpy();
}

class MockCheckoutPaymentFacade {
  setPaymentDetails = createSpy();
  createPaymentDetails = createSpy();

  getPaymentDetails(): Observable<PaymentDetails> {
    return of(mockPaymentDetails);
  }

  paymentProcessSuccess() {}
}

class MockCheckoutComPaymentService {
  setPaymentDetails = createSpy();
  createPaymentDetails = createSpy().and.returnValue(of(mockPaymentDetails));
  updatePaymentAddress = createSpy().and.returnValue(of({line1: 'test'}));

  getPaymentAddressFromState() {
    return of({});
  }

  getPaymentDetails(): Observable<PaymentDetails> {
    return of(mockPaymentDetails);
  }

  paymentProcessSuccess() {}
}

class MockCheckoutDeliveryFacade {
  getDeliveryAddress(): Observable<PaymentDetails> {
    return of(null);
  }
}

class MockCheckoutStepService {
  next = createSpy();
  back = createSpy();

  getBackBntText(): string {
    return 'common.back';
  }
}

const mockActivatedRoute = {
  snapshot: {
    url: ['checkout', 'payment-method'],
  },
};

class MockGlobalMessageService {
  add = createSpy();
}

class MockActiveCartService {
  isGuestCart(): boolean {
    return false;
  }

  getActiveCartId() {
    return of('0001');
  }
}

class MockUserIdService {
  getUserId() {
    return of('current');
  }
}
class MockCheckoutComApmService {
  getSelectedApmFromState() {
    return of({
      code: PaymentType.Card
    });
  }
}

const mockAddress: Address = {
  id: 'mock address id',
  firstName: 'John',
  lastName: 'Doe',
  titleCode: 'mr',
  line1: 'Toyosaki 2 create on cart',
  line2: 'line2',
  town: 'town',
  region: {isocode: 'JP-27'},
  postalCode: 'zip',
  country: {isocode: 'JP'},
};

const mockCheckoutComPaymentDetails: CheckoutComPaymentDetails = {...mockPaymentDetails, cardBin: null, billingAddress: mockAddress};

@Component({
  selector: 'lib-checkout-com-payment-form',
  template: '',
})
class MockPaymentFormComponent {
  @Input()
  paymentMethodsCount: number;
  @Input()
  setAsDefaultField: boolean;
  @Input()
  processing: boolean;
}

@Component({
  selector: 'cx-spinner',
  template: '',
})
class MockSpinnerComponent {}

describe('CheckoutComPaymentMethodComponent', () => {
  let component: CheckoutComPaymentMethodComponent;
  let fixture: ComponentFixture<CheckoutComPaymentMethodComponent>;
  let mockUserPaymentService: UserPaymentService;
  let mockCheckoutPaymentFacade: CheckoutPaymentFacade;
  let mockCheckoutComPaymentService: CheckoutComPaymentService;
  let mockActiveCartService: ActiveCartService;
  let mockGlobalMessageService: GlobalMessageService;
  let mockCheckoutFacade: CheckoutFacade;
  let checkoutStepService: CheckoutStepService;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        imports: [I18nTestingModule],
        declarations: [
          CheckoutComPaymentMethodComponent,
          MockPaymentFormComponent,
          CardComponent,
          MockSpinnerComponent,
          MockCxIconComponent,
        ],
        providers: [
          {provide: UserPaymentService, useClass: MockUserPaymentService},
          {provide: CheckoutFacade, useClass: MockCheckoutFacade},
          {
            provide: CheckoutDeliveryFacade,
            useClass: MockCheckoutDeliveryFacade,
          },
          {
            provide: ActiveCartService,
            useClass: MockActiveCartService,
          },
          {
            provide: UserIdService,
            useClass: MockUserIdService,
          },
          {
            provide: CheckoutPaymentFacade,
            useClass: MockCheckoutPaymentFacade,
          },
          {
            provide: CheckoutComPaymentService,
            useClass: MockCheckoutComPaymentService,
          },
          {provide: GlobalMessageService, useClass: MockGlobalMessageService},
          {provide: CheckoutStepService, useClass: MockCheckoutStepService},
          {provide: ActivatedRoute, useValue: mockActivatedRoute},
          {provide: CheckoutComApmService, useClass: MockCheckoutComApmService},
        ],
      }).compileComponents();

      mockUserPaymentService = TestBed.inject(UserPaymentService);
      mockCheckoutPaymentFacade = TestBed.inject(CheckoutPaymentFacade);
      mockCheckoutComPaymentService = TestBed.inject(CheckoutComPaymentService);
      mockActiveCartService = TestBed.inject(ActiveCartService);
      mockGlobalMessageService = TestBed.inject(GlobalMessageService);
      mockCheckoutFacade = TestBed.inject(CheckoutFacade);
      checkoutStepService = TestBed.inject(
        CheckoutStepService as Type<CheckoutStepService>
      );
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(CheckoutComPaymentMethodComponent);
    component = fixture.componentInstance;
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });

  describe('component behavior', () => {
    it('should show loader during existing payment methods loading', () => {
      spyOn(mockUserPaymentService, 'getPaymentMethodsLoading').and.returnValue(
        of(true)
      );
      spyOn(mockUserPaymentService, 'getPaymentMethods').and.returnValue(
        of([])
      );
      spyOn(mockCheckoutPaymentFacade, 'getPaymentDetails').and.returnValue(
        of(null)
      );

      component.ngOnInit();
      fixture.detectChanges();

      expect(fixture.debugElement.queryAll(By.css('cx-card')).length).toEqual(
        0
      );
      expect(fixture.debugElement.query(By.css('cx-spinner'))).toBeTruthy();
      expect(fixture.debugElement.query(By.css('cx-payment-form'))).toBeFalsy();
    });

    it('should select default payment method when nothing is selected', () => {
      const mockPayments = [
        {
          id: 'non default method',
          accountHolderName: 'Name',
          cardNumber: '123456789',
          cardType: {
            code: 'Visa',
            name: 'Visa',
          },
          expiryMonth: '01',
          expiryYear: '2022',
          cvn: '123',
        },
        {
          id: 'default payment method',
          accountHolderName: 'Name',
          cardNumber: '123456789',
          cardType: {
            code: 'Visa',
            name: 'Visa',
          },
          expiryMonth: '01',
          expiryYear: '2022',
          cvn: '123',
          defaultPayment: true,
        },
      ];
      spyOn(mockUserPaymentService, 'getPaymentMethods').and.returnValue(
        of(mockPayments)
      );
      spyOn(mockCheckoutPaymentFacade, 'getPaymentDetails').and.returnValue(
        of(null)
      );

      component.ngOnInit();
      fixture.detectChanges();

      expect(mockCheckoutPaymentFacade.setPaymentDetails).toHaveBeenCalledWith(
        mockPayments[1]
      );
    });

    it('should show form to add new payment method, when there are no existing methods', () => {
      spyOn(mockUserPaymentService, 'getPaymentMethodsLoading').and.returnValue(
        of(false)
      );
      spyOn(mockUserPaymentService, 'getPaymentMethods').and.returnValue(
        of([])
      );
      spyOn(mockCheckoutPaymentFacade, 'getPaymentDetails').and.returnValue(
        of(null)
      );

      component.ngOnInit();
      fixture.detectChanges();

      expect(fixture.debugElement.queryAll(By.css('cx-card')).length).toEqual(
        0
      );
      expect(fixture.debugElement.query(By.css('cx-spinner'))).toBeFalsy();
      expect(
        fixture.debugElement.query(By.css('lib-checkout-com-payment-form'))
      ).toBeTruthy();
    });

    it('should create and select new payment method and redirect', () => {
      const selectedPaymentMethod = new BehaviorSubject<PaymentDetails>(null);
      spyOn(mockUserPaymentService, 'getPaymentMethodsLoading').and.returnValue(
        of(false)
      );
      spyOn(mockUserPaymentService, 'getPaymentMethods').and.returnValue(
        of([])
      );
      spyOn(mockCheckoutPaymentFacade, 'getPaymentDetails').and.returnValue(
        selectedPaymentMethod.asObservable()
      );

      component.ngOnInit();
      fixture.detectChanges();
      component.setPaymentDetails({
        paymentDetails: mockCheckoutComPaymentDetails,
        billingAddress: mockAddress,
      });

      expect(
        mockCheckoutComPaymentService.createPaymentDetails
      ).toHaveBeenCalledWith(mockCheckoutComPaymentDetails, 'current', '0001');
      selectedPaymentMethod.next(mockPaymentDetails);
    });

    it('should show form for creating new method after clicking new payment method button', () => {
      spyOn(mockUserPaymentService, 'getPaymentMethodsLoading').and.returnValue(
        of(false)
      );
      spyOn(mockUserPaymentService, 'getPaymentMethods').and.returnValue(
        of([mockPaymentDetails])
      );
      spyOn(mockCheckoutPaymentFacade, 'getPaymentDetails').and.returnValue(
        of(null)
      );

      component.ngOnInit();
      fixture.detectChanges();
      fixture.debugElement
             .queryAll(By.css('button'))
             .filter(
               (btn) => btn.nativeElement.innerText === 'paymentForm.addNewPayment'
             )[0]
      .nativeElement.click();
      fixture.detectChanges();

      expect(fixture.debugElement.queryAll(By.css('cx-card')).length).toEqual(
        0
      );
      expect(fixture.debugElement.query(By.css('cx-spinner'))).toBeFalsy();
      expect(
        fixture.debugElement.query(By.css('lib-checkout-com-payment-form'))
      ).toBeTruthy();
    });

    it('should have enabled button when there is selected method', () => {
      const getContinueButton = () => {
        return fixture.debugElement
                      .queryAll(By.css('button'))
                      .filter(
                        (btn) => btn.nativeElement.innerText === 'common.continue'
                      )[0];
      };
      const selectedPaymentMethod = new BehaviorSubject<PaymentDetails>(null);
      spyOn(mockUserPaymentService, 'getPaymentMethodsLoading').and.returnValue(
        of(false)
      );
      spyOn(mockUserPaymentService, 'getPaymentMethods').and.returnValue(
        of([mockPaymentDetails])
      );
      spyOn(mockCheckoutPaymentFacade, 'getPaymentDetails').and.returnValue(
        selectedPaymentMethod.asObservable()
      );

      component.ngOnInit();
      fixture.detectChanges();

      expect(getContinueButton().nativeElement.disabled).toBeTruthy();
      selectedPaymentMethod.next(mockPaymentDetails);
      fixture.detectChanges();
      expect(getContinueButton().nativeElement.disabled).toBeFalsy();
    });

    it('should display credit card info correctly', () => {
      const selectedPaymentMethod = {
        id: 'selected payment method',
        accountHolderName: 'Name',
        cardNumber: '123456789',
        cardType: {
          code: 'Visa',
          name: 'Visa',
        },
        expiryMonth: '01',
        expiryYear: '2022',
        cvn: '123',
        defaultPayment: true,
      };

      expect(
        component['createCard'](
          selectedPaymentMethod,
          {
            textDefaultPaymentMethod: '✓ DEFAULT',
            textExpires: 'Expires',
            textUseThisPayment: 'Use this payment',
            textSelected: 'Selected',
          },
          selectedPaymentMethod
        )
      ).toEqual({
        title: '✓ DEFAULT',
        textBold: 'Name',
        text: ['123456789', 'Expires'],
        img: 'CREDIT_CARD',
        actions: [{name: 'Use this payment', event: 'send'}],
        header: 'Selected',
      });
    });

    it('should after each payment method selection change that in backend', () => {
      const mockPayments = [
        mockPaymentDetails,
        {
          id: 'default payment method',
          accountHolderName: 'Name',
          cardNumber: '123456789',
          cardType: {
            code: 'Visa',
            name: 'Visa',
          },
          expiryMonth: '01',
          expiryYear: '2022',
          cvn: '123',
          defaultPayment: true,
        },
      ];
      spyOn(mockUserPaymentService, 'getPaymentMethods').and.returnValue(
        of(mockPayments)
      );
      spyOn(mockCheckoutPaymentFacade, 'getPaymentDetails').and.returnValue(
        of(mockPaymentDetails)
      );

      component.ngOnInit();
      fixture.detectChanges();
      fixture.debugElement
             .queryAll(By.css('cx-card'))[1]
      .query(By.css('.btn-link'))
      .nativeElement.click();

      expect(mockCheckoutPaymentFacade.setPaymentDetails).toHaveBeenCalledWith(
        mockPayments[1]
      );
    });

    it('should not try to load methods for guest checkout', () => {
      spyOn(mockUserPaymentService, 'loadPaymentMethods').and.stub();
      spyOn(mockUserPaymentService, 'getPaymentMethods').and.returnValue(
        of([])
      );
      spyOn(mockActiveCartService, 'isGuestCart').and.returnValue(true);

      component.ngOnInit();

      expect(mockUserPaymentService.loadPaymentMethods).not.toHaveBeenCalled();
    });

    it('should show selected card, when there was previously selected method', () => {
      const mockPayments = [
        mockPaymentDetails,
        {
          id: 'default payment method',
          accountHolderName: 'Name',
          cardNumber: '123456789',
          cardType: {
            code: 'Visa',
            name: 'Visa',
          },
          expiryMonth: '01',
          expiryYear: '2022',
          cvn: '123',
          defaultPayment: true,
        },
      ];
      spyOn(mockUserPaymentService, 'getPaymentMethods').and.returnValue(
        of(mockPayments)
      );
      spyOn(mockCheckoutPaymentFacade, 'getPaymentDetails').and.returnValue(
        of(mockPaymentDetails)
      );

      component.ngOnInit();
      fixture.detectChanges();

      expect(
        mockCheckoutPaymentFacade.setPaymentDetails
      ).not.toHaveBeenCalled();
    });

    it('should go to previous step after clicking back', () => {
      spyOn(mockUserPaymentService, 'getPaymentMethodsLoading').and.returnValue(
        of(false)
      );
      spyOn(mockUserPaymentService, 'getPaymentMethods').and.returnValue(
        of([mockPaymentDetails])
      );
      spyOn(mockCheckoutPaymentFacade, 'getPaymentDetails').and.returnValue(
        of(null)
      );

      component.ngOnInit();
      fixture.detectChanges();
      fixture.debugElement
             .queryAll(By.css('button'))
             .filter((btn) => btn.nativeElement.innerText === 'common.back')[0]
      .nativeElement.click();
      fixture.detectChanges();

      expect(checkoutStepService.back).toHaveBeenCalledWith(
        <any> mockActivatedRoute
      );
    });

    it('should show errors on wrong card information', () => {
      spyOn(mockUserPaymentService, 'getPaymentMethodsLoading').and.returnValue(
        of(false)
      );
      spyOn(mockUserPaymentService, 'getPaymentMethods').and.returnValue(
        of([mockPaymentDetails])
      );
      spyOn(mockCheckoutPaymentFacade, 'getPaymentDetails').and.returnValue(
        of({
          ...mockPaymentDetails,
          hasError: true,
          InvalidFieldCVV: 'cvv',
        })
      );

      component.ngOnInit();
      fixture.detectChanges();

      expect(mockGlobalMessageService.add).toHaveBeenCalledWith(
        {
          key: 'paymentMethods.invalidField',
          params: {field: 'cvv'},
        },
        GlobalMessageType.MSG_TYPE_ERROR
      );
      expect(mockCheckoutFacade.clearCheckoutStep).toHaveBeenCalledWith(3);
    });

    it('should subscribe to the selected Apm', () => {
      let apm: ApmData;
      component.selectedApm$
        .subscribe((res) => apm = res)
        .unsubscribe();

      fixture.detectChanges();

      expect(apm.code).toBe(PaymentType.Card);
      expect(component.isCardPayment).toBeTrue();
    })
  });
});
