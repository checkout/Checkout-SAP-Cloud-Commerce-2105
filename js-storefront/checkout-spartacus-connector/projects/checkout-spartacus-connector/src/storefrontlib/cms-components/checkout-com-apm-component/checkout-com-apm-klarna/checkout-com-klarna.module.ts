import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { I18nModule } from '@spartacus/core';
import { CheckoutComBillingAddressModule } from '../../checkout-com-billing-address/checkout-com-billing-address.module';
import { CheckoutComKlarnaComponent } from './checkout-com-klarna.component';

@NgModule({
  declarations: [CheckoutComKlarnaComponent],
  imports: [
    CommonModule,
    I18nModule,
    CheckoutComBillingAddressModule
  ],
  exports: [CheckoutComKlarnaComponent]
})
export class CheckoutComApmKlarnaModule {}
