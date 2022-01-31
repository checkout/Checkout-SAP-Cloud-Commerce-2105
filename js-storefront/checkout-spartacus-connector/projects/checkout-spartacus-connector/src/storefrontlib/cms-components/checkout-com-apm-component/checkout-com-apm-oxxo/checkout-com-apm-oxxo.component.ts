import { Component, ChangeDetectionStrategy, Output, EventEmitter, Input } from '@angular/core';
import { ApmPaymentDetails } from '../../../interfaces';
import { Address } from '@spartacus/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';
import { PaymentType } from '../../../../core/model/ApmData';
import { makeFormErrorsVisible } from '../../../../core/shared/make-form-errors-visible';

@Component({
  selector: 'lib-checkout-com-apm-oxxo',
  templateUrl: './checkout-com-apm-oxxo.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CheckoutComApmOxxoComponent {
  @Input() billingAddressForm: FormGroup = new FormGroup({});
  @Output() setPaymentDetails = new EventEmitter<{ paymentDetails: ApmPaymentDetails, billingAddress: Address }>();
  public documentCtrl = new FormControl('', [Validators.required, Validators.pattern('^[a-zA-Z0-9]{18}$')]);
  public form = new FormGroup({document: this.documentCtrl});
  public sameAsShippingAddress: boolean = true;

  next() {
    if (!this.documentCtrl.value || !this.documentCtrl.valid) {
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
        type: PaymentType.Oxxo,
        document: this.documentCtrl.value
      } as ApmPaymentDetails,
      billingAddress
    });
  }
}
