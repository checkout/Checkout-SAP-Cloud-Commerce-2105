import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CheckoutComAdapter } from '../checkout-com.adapter';
import { CheckoutComOccAdapter } from './checkout-com-occ.adapter';
import { ApmDataNormalizer } from '../../normalizers/apm-data-normalizer';
import { APM_NORMALIZER, APM_PAYMENT_DETAILS_NORMALIZER, COMPONENT_APM_NORMALIZER } from '../converters';
import { MediaModule } from '@spartacus/storefront';
import { ComponentApmNormalizer } from '../../normalizers/component-apm-normalizer';
import { ApmPaymentDetailsNormalizer } from '../../normalizers/apm-payment-details-normalizer';
import { provideDefaultConfig } from '@spartacus/core';
import { defaultOccCheckoutComConfig } from './default-occ-checkout-com-config';

@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    MediaModule
  ],
  providers: [
    {provide: CheckoutComAdapter, useClass: CheckoutComOccAdapter},
    {provide: APM_NORMALIZER, useClass: ApmDataNormalizer, multi: true},
    {provide: COMPONENT_APM_NORMALIZER, useClass: ComponentApmNormalizer, multi: true},
    {provide: APM_PAYMENT_DETAILS_NORMALIZER, useClass: ApmPaymentDetailsNormalizer, multi: true},

    provideDefaultConfig(defaultOccCheckoutComConfig),
  ]
})
export class CheckoutComOccModule {
}
