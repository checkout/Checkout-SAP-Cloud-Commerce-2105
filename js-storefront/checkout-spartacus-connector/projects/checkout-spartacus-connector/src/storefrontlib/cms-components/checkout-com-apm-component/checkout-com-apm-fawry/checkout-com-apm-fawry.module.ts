import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CheckoutComApmFawryComponent } from './checkout-com-apm-fawry.component';
import { ReactiveFormsModule } from '@angular/forms';
import { I18nModule } from '@spartacus/core';
import { FormErrorsModule } from '@spartacus/storefront';
import { CheckoutComBillingAddressModule } from '../../checkout-com-billing-address/checkout-com-billing-address.module';



@NgModule({
  declarations: [CheckoutComApmFawryComponent],
  exports: [CheckoutComApmFawryComponent],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    I18nModule,
    FormErrorsModule,
    CheckoutComBillingAddressModule
  ]
})
export class CheckoutComApmFawryModule { }
