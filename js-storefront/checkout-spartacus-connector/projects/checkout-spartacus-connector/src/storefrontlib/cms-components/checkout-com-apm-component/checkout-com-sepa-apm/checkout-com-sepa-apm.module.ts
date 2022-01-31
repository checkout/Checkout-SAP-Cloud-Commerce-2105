import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CheckoutComSepaApmComponent } from './checkout-com-sepa-apm.component';
import { I18nModule } from '@spartacus/core';
import { FormErrorsModule, SpinnerModule } from '@spartacus/storefront';
import { ReactiveFormsModule } from '@angular/forms';
import { NgSelectModule } from '@ng-select/ng-select';

@NgModule({
  declarations: [CheckoutComSepaApmComponent],
  exports: [
    CheckoutComSepaApmComponent
  ],
  imports: [
    CommonModule,
    I18nModule,
    SpinnerModule,
    ReactiveFormsModule,
    FormErrorsModule,
    NgSelectModule,
  ]
})
export class CheckoutComSepaApmModule { }
