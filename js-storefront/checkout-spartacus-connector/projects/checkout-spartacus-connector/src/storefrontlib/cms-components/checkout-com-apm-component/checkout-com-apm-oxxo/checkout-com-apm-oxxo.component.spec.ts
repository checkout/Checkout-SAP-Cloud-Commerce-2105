import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CheckoutComApmOxxoComponent } from './checkout-com-apm-oxxo.component';
import { I18nTestingModule, Address } from '@spartacus/core';
import { FormErrorsModule } from '@spartacus/storefront';
import { ReactiveFormsModule, FormGroup, Validators, FormControl } from '@angular/forms';
import { PaymentType } from '../../../../core/model/ApmData';
import { timeout } from 'rxjs/operators';

function expectSubmitButtonIsDisabled(disabled) {
  expect(document.querySelector('button[data-test-id="oxxo-continue-btn"]')['disabled']).toEqual(disabled);
}

describe('CheckoutComApmOxxoComponent', () => {
  let component: CheckoutComApmOxxoComponent;
  let fixture: ComponentFixture<CheckoutComApmOxxoComponent>;
  const docId = '111111111111111111';

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [I18nTestingModule, FormErrorsModule, ReactiveFormsModule],
      declarations: [CheckoutComApmOxxoComponent]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CheckoutComApmOxxoComponent);
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

  it('should have Document ID field', () => {
    fixture.detectChanges();
    expect(document.querySelectorAll('input[data-test-id="oxxo-document-input"]').length).toEqual(1);
  });

  it('continue button should be disabled on init', () => {
    fixture.detectChanges();
    expectSubmitButtonIsDisabled(true);
  });

  it('continue button should be disabled on wrong format', () => {
    fixture.detectChanges();
    component.documentCtrl.setValue('11111111111111');
    fixture.detectChanges();
    expectSubmitButtonIsDisabled(true);
  });

  it('continue button should be enabled on correct format', () => {
    fixture.detectChanges();
    component.documentCtrl.setValue(docId);
    fixture.detectChanges();
    expectSubmitButtonIsDisabled(false);
  });

  it('should send Document ID field', (done) => {
    fixture.detectChanges();

    component.setPaymentDetails.subscribe((event) => {
      expect(event.billingAddress).toBeNull();
      expect(event.paymentDetails).toEqual({type: PaymentType.Oxxo, document: docId});

      done();
    });
    component.documentCtrl.setValue(docId);
    fixture.detectChanges();
    component.next();
  });

  it('should use the billing address if given', (done) => {
    component.documentCtrl.setValue(docId);
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
    component.documentCtrl.setValue(docId);
    component.sameAsShippingAddress = false;
    component.setPaymentDetails.pipe(timeout(2)).subscribe({error: (err) => {
      expect(err.message).toEqual('Timeout has occurred');
      done();
    }});
    const billingAddress = {
      firstName: 'John',
      lastName: '',
    } as Address;
    component.billingAddressForm.setValue(billingAddress);
    fixture.detectChanges();
    component.next();
  });

  it('should detect invalid document ids', (done) => {
    component.documentCtrl.setValue("222");

    component.next();

    expectSubmitButtonIsDisabled(true);

    component.setPaymentDetails.pipe(timeout(2)).subscribe({error: (err) => {
        expect(err.message).toEqual('Timeout has occurred');
        done();
      }});

    const {dirty, touched} = component.form.get('document');
    expect(dirty).toBeTruthy();
    expect(touched).toBeTruthy();
  })
});
