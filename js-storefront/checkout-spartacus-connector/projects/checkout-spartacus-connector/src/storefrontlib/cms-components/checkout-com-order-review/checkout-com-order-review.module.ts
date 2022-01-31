import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CheckoutComOrderReviewComponent } from './checkout-com-order-review.component';
import { CmsConfig, I18nModule, provideConfig } from '@spartacus/core';
import { CheckoutComOrderConfirmationOverviewComponent } from '../checkout-com-order-confirmation-overview/checkout-com-order-confirmation-overview.component';
import { CardModule } from '@spartacus/storefront';
import { OrderConfirmationGuard } from '@spartacus/checkout/components';
import { CheckoutModule } from '@spartacus/checkout';

@NgModule({
  declarations: [CheckoutComOrderReviewComponent],
  imports: [
    CommonModule,
    I18nModule,
    CardModule,
    CheckoutModule,
  ],
  exports: [
    CheckoutComOrderReviewComponent
  ],
  providers: [
    provideConfig(
      {
        cmsComponents: {
          OrderConfirmationOverviewComponent: {
            component: CheckoutComOrderConfirmationOverviewComponent,
            guards: [OrderConfirmationGuard],
          }
        }
      } as CmsConfig
    )
  ]
})
export class CheckoutComOrderReviewModule {
}
