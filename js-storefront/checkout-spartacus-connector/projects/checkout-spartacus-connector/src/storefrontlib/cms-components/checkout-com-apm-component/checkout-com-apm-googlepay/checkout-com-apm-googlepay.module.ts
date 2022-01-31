import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CheckoutComApmGooglepayComponent } from './checkout-com-apm-googlepay.component';
import { CheckoutComBillingAddressModule } from '../../checkout-com-billing-address/checkout-com-billing-address.module';

@NgModule({
  declarations: [CheckoutComApmGooglepayComponent],
  exports: [
    CheckoutComApmGooglepayComponent
  ],
  imports: [
    CommonModule,
    CheckoutComBillingAddressModule,
  ]
})
export class CheckoutComApmGooglepayModule { }
