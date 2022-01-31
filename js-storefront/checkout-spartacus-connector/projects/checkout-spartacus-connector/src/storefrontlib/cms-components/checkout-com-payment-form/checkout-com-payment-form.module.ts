/* Angular */
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { NgSelectModule } from '@ng-select/ng-select';
/* Spartacus */
import { I18nModule } from '@spartacus/core';
import {
  CardModule,
  IconModule,
  SpinnerModule,
  FormErrorsModule,
} from '@spartacus/storefront';
import { PaymentFormModule, PaymentMethodModule } from '@spartacus/checkout/components';
/* CheckoutCom */
import { CheckoutComStoreModule } from '../../../core/store/checkout-com-store.module';
import { CheckoutComFramesFormModule } from '../checkout-com-frames-form/checkout-com-frames-form.module';
import { CheckoutComOccModule } from '../../../core/adapters/occ/checkout-com-occ.module';
import { CheckoutComPaymentFormComponent } from './checkout-com-payment-form.component';
import { CheckoutComApmModule } from '../checkout-com-apm-component/checkout-com-apm.module';
import { CheckoutComBillingAddressModule } from '../checkout-com-billing-address/checkout-com-billing-address.module';

@NgModule({
  declarations: [CheckoutComPaymentFormComponent],
  exports: [
    CheckoutComPaymentFormComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    NgSelectModule,
    CardModule,
    I18nModule,
    IconModule,
    SpinnerModule,
    FormErrorsModule,
    PaymentMethodModule,
    PaymentFormModule,
    /* CheckoutCom modules */
    CheckoutComStoreModule,
    CheckoutComOccModule,
    CheckoutComFramesFormModule,
    CheckoutComApmModule,
    CheckoutComBillingAddressModule,
  ],
})
export class CheckoutComPaymentFormModule { }
