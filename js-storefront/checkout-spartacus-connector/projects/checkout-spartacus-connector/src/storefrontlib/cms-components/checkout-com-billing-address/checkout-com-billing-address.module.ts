import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CheckoutComBillingAddressComponent } from './checkout-com-billing-address.component';
import { I18nModule } from '@spartacus/core';
import { NgSelectModule } from '@ng-select/ng-select';
import { CardModule, FormErrorsModule } from '@spartacus/storefront';
import { ReactiveFormsModule } from '@angular/forms';

@NgModule({
  declarations: [CheckoutComBillingAddressComponent],
  exports: [
    CheckoutComBillingAddressComponent
  ],
  imports: [
    CommonModule,
    I18nModule,
    NgSelectModule,
    FormErrorsModule,
    CardModule,
    ReactiveFormsModule
  ]
})
export class CheckoutComBillingAddressModule {
}
