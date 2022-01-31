import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CheckoutComApmApplepayComponent } from './checkout-com-apm-applepay.component';

@NgModule({
  declarations: [CheckoutComApmApplepayComponent],
  exports: [
    CheckoutComApmApplepayComponent
  ],
  imports: [
    CommonModule
  ]
})
export class CheckoutComApmApplepayModule { }
