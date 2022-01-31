import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { I18nModule } from '@spartacus/core';
import { CheckoutComBillingAddressModule } from '../../checkout-com-billing-address/checkout-com-billing-address.module';
import { FormErrorsModule } from '@spartacus/storefront';
import { ReactiveFormsModule } from '@angular/forms';
import { CheckoutComApmOxxoComponent } from './checkout-com-apm-oxxo.component';

@NgModule({
  declarations: [CheckoutComApmOxxoComponent],
  imports: [
    CommonModule,
    I18nModule,
    CheckoutComBillingAddressModule,
    FormErrorsModule,
    ReactiveFormsModule
  ],
  exports: [CheckoutComApmOxxoComponent]
})
export class CheckoutComApmOxxoModule {}
