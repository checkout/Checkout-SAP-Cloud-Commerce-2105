import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CheckoutComReviewSubmitComponent } from './checkout-com-review-submit.component';
import {
  CardModule,
  CartSharedModule,
  IconModule,
  PromotionsModule
} from '@spartacus/storefront';
import { I18nModule, provideDefaultConfig, UrlModule } from '@spartacus/core';
import { RouterModule } from '@angular/router';
import { CartNotEmptyGuard, CheckoutAuthGuard } from '@spartacus/checkout/components';

@NgModule({
  declarations: [CheckoutComReviewSubmitComponent],
  imports: [
    CommonModule,
    CardModule,
    CartSharedModule,
    I18nModule,
    UrlModule,
    RouterModule,
    PromotionsModule,
    IconModule,
  ],
  providers: [
    provideDefaultConfig({
      cmsComponents: {
        CheckoutReviewOrder: {
          component: CheckoutComReviewSubmitComponent,
          guards: [CheckoutAuthGuard, CartNotEmptyGuard],
        },
      },
    }),
  ]
})
export class CheckoutComReviewSubmitModule { }
