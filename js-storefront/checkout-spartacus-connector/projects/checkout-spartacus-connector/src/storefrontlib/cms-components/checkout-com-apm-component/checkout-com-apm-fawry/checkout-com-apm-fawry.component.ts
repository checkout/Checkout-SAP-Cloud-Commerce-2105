import { Component, ChangeDetectionStrategy, Input, Output, EventEmitter } from '@angular/core';
import { PaymentType } from '../../../../core/model/ApmData';
import { ApmPaymentDetails } from '../../../interfaces';
import { FormGroup, FormControl, Validators } from '@angular/forms';
import { Address } from '@spartacus/core';
import { makeFormErrorsVisible } from '../../../../core/shared/make-form-errors-visible';

@Component({
  selector: 'lib-checkout-com-apm-fawry',
  templateUrl: './checkout-com-apm-fawry.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CheckoutComApmFawryComponent {
  @Input() billingAddressForm: FormGroup = new FormGroup({});
  @Output() setPaymentDetails = new EventEmitter<{ paymentDetails: ApmPaymentDetails, billingAddress: Address }>();
  public mobileNumberCtrl = new FormControl('', [Validators.required, Validators.pattern('^[0-9]{11}$')]);
  public form = new FormGroup({mobileNumberCtrl: this.mobileNumberCtrl});
  public sameAsShippingAddress: boolean = true;

  next() {
    if (!this.mobileNumberCtrl.value || !this.mobileNumberCtrl.valid) {
      makeFormErrorsVisible(this.form);
      return;
    }
    if (!this.sameAsShippingAddress && !this.billingAddressForm.valid) {
      makeFormErrorsVisible(this.billingAddressForm);
      return;
    }
    let billingAddress = null;
    if (!this.sameAsShippingAddress) {
      billingAddress = this.billingAddressForm.value;
    }
    this.setPaymentDetails.emit({
      paymentDetails: {
        type: PaymentType.Fawry,
        mobileNumber: this.mobileNumberCtrl.value
      } as ApmPaymentDetails,
      billingAddress
    });
  }
}
