import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ConfigModule, CmsConfig, I18nModule } from '@spartacus/core';
import { CheckoutComStoreModule } from '../../../core/store/checkout-com-store.module';
import { CheckoutComFramesFormModule } from '../checkout-com-frames-form/checkout-com-frames-form.module';
import { ReactiveFormsModule } from '@angular/forms';
import { NgSelectModule } from '@ng-select/ng-select';
import { FormErrorsModule, IconModule, CardModule, SpinnerModule } from '@spartacus/storefront';
import { CheckoutComOccModule } from '../../../core/adapters/occ/checkout-com-occ.module';
import { CheckoutComPaymentMethodComponent } from './checkout-com-payment-method.component';
import {CheckoutComPaymentFormModule} from '../checkout-com-payment-form/checkout-com-payment-form.module';
import {CheckoutComApmModule} from '../checkout-com-apm-component/checkout-com-apm.module';


@NgModule({
  declarations: [CheckoutComPaymentMethodComponent],
  imports: [
    CommonModule,
    ConfigModule.withConfig({
      cmsComponents: {
        CheckoutPaymentDetails: {
          component: CheckoutComPaymentMethodComponent
        }
      }
    } as CmsConfig),
    CheckoutComStoreModule,
    CheckoutComOccModule,
    CheckoutComFramesFormModule,
    CheckoutComPaymentFormModule,
    ReactiveFormsModule,
    NgSelectModule,
    FormErrorsModule,
    I18nModule,
    IconModule,
    CardModule,
    SpinnerModule,
    CheckoutComApmModule
  ]
})
export class CheckoutComPaymentMethodModule { }
