import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CheckoutComApmIdealComponent } from './checkout-com-apm-ideal.component';
import { FormErrorsModule, SpinnerModule } from '@spartacus/storefront';
import { I18nModule } from '@spartacus/core';
import { ReactiveFormsModule } from '@angular/forms';
import { CheckoutComBillingAddressModule } from '../../checkout-com-billing-address/checkout-com-billing-address.module';



@NgModule({
  declarations: [CheckoutComApmIdealComponent],
  exports: [
    CheckoutComApmIdealComponent
  ],
  imports: [
    CommonModule,
    SpinnerModule,
    I18nModule,
    ReactiveFormsModule,
    FormErrorsModule,
    CheckoutComBillingAddressModule
  ]
})
export class CheckoutComApmIdealModule { }
