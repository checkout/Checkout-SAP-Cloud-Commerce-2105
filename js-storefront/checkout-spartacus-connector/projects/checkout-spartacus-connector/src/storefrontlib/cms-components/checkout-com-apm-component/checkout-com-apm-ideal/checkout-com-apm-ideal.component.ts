import { ChangeDetectionStrategy, Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ApmPaymentDetails } from '../../../interfaces';
import { Address } from '@spartacus/core';
import { PaymentType } from '../../../../core/model/ApmData';
import { BehaviorSubject } from 'rxjs';
import { makeFormErrorsVisible } from '../../../../core/shared/make-form-errors-visible';

@Component({
  selector: 'lib-checkout-com-apm-ideal',
  templateUrl: './checkout-com-apm-ideal.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CheckoutComApmIdealComponent implements OnDestroy {
  @Input() billingAddressForm: FormGroup = new FormGroup({});
  @Output() setPaymentDetails = new EventEmitter<{ paymentDetails: ApmPaymentDetails, billingAddress: Address }>();

  public submitting$ = new BehaviorSubject<boolean>(false);
  public sameAsShippingAddress: boolean = true;

  idealForm = this.fb.group({
      bic: ['', Validators.pattern(/^[a-zA-Z0-9]{8}$|^[a-zA-Z0-9]{11}$/)]
    }
  );

  constructor(protected fb: FormBuilder) {
  }

  next() {
    const {valid, value} = this.idealForm;
    const {invalid: billingInvalid, value: billingValue} = this.billingAddressForm;

    if (!this.sameAsShippingAddress && billingInvalid) {
      makeFormErrorsVisible(this.billingAddressForm);
      return;
    }

    if (valid) {
      const paymentDetails: ApmPaymentDetails = {
        type: PaymentType.iDeal,
        bic: value.bic
      };

      this.submitting$.next(true);

      let billingAddress = null;
      if (!this.sameAsShippingAddress) {
        billingAddress = billingValue;
      }

      this.setPaymentDetails.emit({
        paymentDetails,
        billingAddress
      });
    } else {
      this.idealForm.markAllAsTouched();
    }
  }

  ngOnDestroy(): void {
    this.submitting$.next(false);
  }
}
