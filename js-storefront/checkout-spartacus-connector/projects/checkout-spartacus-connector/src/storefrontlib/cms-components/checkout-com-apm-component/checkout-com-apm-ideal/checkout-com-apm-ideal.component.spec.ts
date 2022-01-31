import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CheckoutComApmIdealComponent } from './checkout-com-apm-ideal.component';
import { Address, I18nTestingModule } from '@spartacus/core';
import { FormErrorsModule } from '@spartacus/storefront';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ApmPaymentDetails } from '../../../interfaces';
import { EventEmitter } from '@angular/core';
import { PaymentType } from '../../../../core/model/ApmData';

describe('CheckoutComApmIdealComponent', () => {
  let component: CheckoutComApmIdealComponent;
  let fixture: ComponentFixture<CheckoutComApmIdealComponent>;
  let setPaymentDetails = new EventEmitter<{ paymentDetails: ApmPaymentDetails, billingAddress: Address }>();

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [I18nTestingModule, FormErrorsModule, ReactiveFormsModule],
      declarations: [CheckoutComApmIdealComponent]
    })
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CheckoutComApmIdealComponent);
    component = fixture.componentInstance;

    setPaymentDetails = new EventEmitter<{ paymentDetails: ApmPaymentDetails, billingAddress: Address }>();
    component.setPaymentDetails = setPaymentDetails;
    component.sameAsShippingAddress = true;

    // simplified billing address form for tst
    component.billingAddressForm = new FormGroup({
      firstName: new FormControl('', [Validators.required]),
      lastName: new FormControl('', [Validators.required])
    });

    fixture.detectChanges();
  });

  afterEach(() => {
    if (setPaymentDetails) {
      setPaymentDetails.unsubscribe();
    }
  });


  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('BIC validation', () => {
    [
      {description: 'Should allow 11 character BIC', bic: 'INGDESMMXXX', result: true},
      {description: 'Should allow 8 character BIC', bic: 'CHASUS33', result: true},
      {description: 'Should be invalid if bic is less then 8 characters', bic: 'INGDE', result: false},
      {description: 'Should be invalid if bic is more than 11 characters', bic: 'INGDESMMXXX ', result: false},
      {description: 'Should be invalid if bic not 8 or 11 characters', bic: 'INGDESMMXX ', result: false},
    ].forEach((parameter) => {
      const {description, bic, result} = parameter;
      it(description, () => {
        component.idealForm.setValue({
          bic
        });

        expect(component.idealForm.valid).toBe(result);
      });
    });
  });

  it('should require bic field', (done) => {
    component.idealForm.setValue({
      bic: 'INGDESMMXXX'
    });

    setPaymentDetails
      .subscribe((event) => {
      expect(event.billingAddress).toBeNull();
      expect(event.paymentDetails).toEqual({ type: PaymentType.iDeal, bic: 'INGDESMMXXX' });

      done();
    });

    fixture.detectChanges();

    expect(document.querySelector('button[data-test-id="ideal-continue-btn"]')['disabled']).toEqual(false);

    component.next();
  });

  it('should use the billing address if given', (done) => {
    component.idealForm.setValue({
      bic: 'INGDESMMXXX'
    });
    component.sameAsShippingAddress = false;
    const billingAddress = {
      firstName: 'John',
      lastName: 'Doe',
    } as Address;
    component.billingAddressForm.setValue(billingAddress);

    setPaymentDetails
      .subscribe((event) => {
      expect(event.billingAddress).toEqual(billingAddress);
      expect(event.paymentDetails).toEqual({ type: PaymentType.iDeal, bic: 'INGDESMMXXX' });

      done();
    });

    fixture.detectChanges();

    expect(document.querySelector('button[data-test-id="ideal-continue-btn"]')['disabled']).toEqual(false);

    component.next();
  });

  it('should not call setPaymentDetails event if required bic field is not set', () => {
    component.idealForm.setValue({
      bic: ''
    });
    component.sameAsShippingAddress = false;

    fixture.detectChanges();

    expect(document.querySelector('button[data-test-id="ideal-continue-btn"]')['disabled']).toEqual(true);
  });

  it('should not call setPaymentDetails event if billing address is not valid', () => {
    component.idealForm.setValue({
      bic: '123'
    });
    component.sameAsShippingAddress = false;
    const billingAddress = {
      firstName: 'John',
      lastName: '',
    } as Address;
    component.billingAddressForm.setValue(billingAddress);

    fixture.detectChanges();

    expect(document.querySelector('button[data-test-id="ideal-continue-btn"]')['disabled']).toEqual(true);
  });
});
