import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ExpressCartTotalsComponent } from './express-cart-totals.component';
import { FeaturesConfigModule, I18nModule, provideConfig, UrlModule } from '@spartacus/core';
import { CartCouponModule, CartSharedModule, ProgressButtonModule } from '@spartacus/storefront';
import { RouterModule } from '@angular/router';
import { CheckoutComExpressButtonsModule } from 'checkout-spartacus-connector';

@NgModule({
  declarations: [
    ExpressCartTotalsComponent
  ],
  exports: [
    ExpressCartTotalsComponent
  ],
  imports: [
    CommonModule,
    RouterModule,
    UrlModule,
    CartSharedModule,
    I18nModule,
    CartCouponModule,
    FeaturesConfigModule,
    ProgressButtonModule,

    CheckoutComExpressButtonsModule,
  ],
  providers: [
    provideConfig({
      cmsComponents: {
        CartTotalsComponent: {
          component: ExpressCartTotalsComponent,
        },
      },
    }),
  ],
})
export class ExpressCartTotalsModule { }
