import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CheckoutComApmComponent } from './checkout-com-apm.component';
import { FormErrorsModule, SpinnerModule } from '@spartacus/storefront';
import { I18nModule } from '@spartacus/core';
import { CheckoutComStoreModule } from '../../../core/store/checkout-com-store.module';
import { CheckoutComOccModule } from '../../../core/adapters/occ/checkout-com-occ.module';
import { CheckoutComBillingAddressModule } from '../checkout-com-billing-address/checkout-com-billing-address.module';
import { CheckoutComApmKlarnaModule } from './checkout-com-apm-klarna/checkout-com-klarna.module';
import { ReactiveFormsModule } from '@angular/forms';
import { NgSelectModule } from '@ng-select/ng-select';
import { CheckoutComApmTileModule } from './checkout-com-apm-tile/checkout-com-apm-tile.module';
import { CheckoutComApmGooglepayModule } from './checkout-com-apm-googlepay/checkout-com-apm-googlepay.module';
import { CheckoutComSepaApmModule } from './checkout-com-sepa-apm/checkout-com-sepa-apm.module';
import { CheckoutComApmApplepayModule } from './checkout-com-apm-applepay/checkout-com-apm-applepay.module';
import { CheckoutComApmOxxoModule } from './checkout-com-apm-oxxo/checkout-com-apm-oxxo.module';
import { CheckoutComApmFawryModule } from './checkout-com-apm-fawry/checkout-com-apm-fawry.module';
import { CheckoutComApmIdealModule } from './checkout-com-apm-ideal/checkout-com-apm-ideal.module';

@NgModule({
  declarations: [CheckoutComApmComponent],
  exports: [CheckoutComApmComponent],
  imports: [
    CommonModule,
    I18nModule,
    SpinnerModule,
    ReactiveFormsModule,
    FormErrorsModule,
    NgSelectModule,
    CheckoutComStoreModule,
    CheckoutComOccModule,
    CheckoutComBillingAddressModule,
    CheckoutComApmKlarnaModule,
    CheckoutComApmOxxoModule,
    CheckoutComApmTileModule,
    CheckoutComApmApplepayModule,
    CheckoutComApmGooglepayModule,
    CheckoutComSepaApmModule,
    CheckoutComApmFawryModule,
    CheckoutComApmIdealModule,
  ]
})
export class CheckoutComApmModule { }
