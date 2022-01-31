import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CheckoutComApmFawryComponent } from './checkout-com-apm-fawry.component';
import { I18nTestingModule, Address } from '@spartacus/core';
import { FormErrorsModule } from '@spartacus/storefront';
import { ReactiveFormsModule, FormControl, FormGroup, Validators } from '@angular/forms';
import { PaymentType } from '../../../../core/model/ApmData';
import { timeout } from 'rxjs/operators';

describe('CheckoutComApmFawryComponent', () => {
  let component: CheckoutComApmFawryComponent;
  let fixture: ComponentFixture<CheckoutComApmFawryComponent>;
  const mobileNumber = '01055518212';

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [I18nTestingModule, FormErrorsModule, ReactiveFormsModule],
      declarations: [CheckoutComApmFawryComponent]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CheckoutComApmFawryComponent);
    component = fixture.componentInstance;
    component.billingAddressForm = new FormGroup({
      firstName: new FormControl('', [Validators.required]),
      lastName: new FormControl('', [Validators.required])
    });
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have Mobile Number field', () => {
    fixture.detectChanges();
    expect(document.querySelectorAll('input[data-test-id="fawry-number-input"]').length).toEqual(1);
  });

  it('continue button should be disabled on init', () => {
    fixture.detectChanges();
    expect(document.querySelector('button[data-test-id="fawry-continue-btn"]')['disabled']).toEqual(true);
  });

  it('continue button should be disabled on wrong format (+)', () => {
    fixture.detectChanges();
    component.mobileNumberCtrl.setValue('+01055518212');
    fixture.detectChanges();
    expect(document.querySelector('button[data-test-id="fawry-continue-btn"]')['disabled']).toEqual(true);
  });

  it('continue button should be disabled on wrong format (letter)', () => {
    fixture.detectChanges();
    component.mobileNumberCtrl.setValue('a01055518212');
    fixture.detectChanges();
    expect(document.querySelector('button[data-test-id="fawry-continue-btn"]')['disabled']).toEqual(true);
  });

  it('continue button should be enabled on correct format', () => {
    fixture.detectChanges();
    component.mobileNumberCtrl.setValue('01055518212');
    fixture.detectChanges();
    expect(document.querySelector('button[data-test-id="fawry-continue-btn"]')['disabled']).toEqual(false);
  });

  it('should send MobileNumber field', (done) => {
    fixture.detectChanges();

    component.setPaymentDetails.subscribe((event) => {
      expect(event.billingAddress).toBeNull();
      expect(event.paymentDetails).toEqual({type: PaymentType.Fawry, mobileNumber});

      done();
    });
    component.mobileNumberCtrl.setValue(mobileNumber);
    fixture.detectChanges();
    component.next();
  });

  it('should use the billing address if given', (done) => {
    component.mobileNumberCtrl.setValue(mobileNumber);
    component.sameAsShippingAddress = false;
    const billingAddress = {
      firstName: 'John',
      lastName: 'Doe',
    } as Address;
    component.setPaymentDetails.subscribe((event) => {
      expect(event.billingAddress).toEqual(billingAddress);
      done();
    });
    component.billingAddressForm.setValue(billingAddress);
    fixture.detectChanges();
    component.next();
  });

  it('should not call setPaymentDetails event if billing address is not valid', (done) => {
    component.mobileNumberCtrl.setValue(mobileNumber);
    component.sameAsShippingAddress = false;
    component.setPaymentDetails.pipe(timeout(2)).subscribe({
      error: (err) => {
        expect(err.message).toEqual('Timeout has occurred');
        done();
      }
    });
    const billingAddress = {
      firstName: 'John',
      lastName: '',
    } as Address;
    component.billingAddressForm.setValue(billingAddress);
    fixture.detectChanges();
    component.next();
  });
});
