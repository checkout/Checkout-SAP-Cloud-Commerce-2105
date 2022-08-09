import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  CartSharedModule,
  IconModule,
  ItemCounterModule,
  KeyboardFocusModule,
  ModalModule,
  PromotionsModule,
  SpinnerModule
} from '@spartacus/storefront';
import { FeaturesConfigModule, I18nModule, provideConfig, UrlModule } from '@spartacus/core';
import { RouterModule } from '@angular/router';
import { ReactiveFormsModule } from '@angular/forms';
import { ExpressAddToCartComponent } from './express-add-to-cart.component';

import { CheckoutComExpressButtonsModule } from 'checkout-spartacus-connector';

@NgModule({
  declarations: [
    ExpressAddToCartComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    CartSharedModule,
    RouterModule,
    SpinnerModule,
    PromotionsModule,
    FeaturesConfigModule,
    UrlModule,
    IconModule,
    I18nModule,
    ItemCounterModule,
    KeyboardFocusModule,
    ModalModule,

    CheckoutComExpressButtonsModule,
  ],
  providers: [
    provideConfig(
      {
        cmsComponents: {
          ProductAddToCartComponent: {
            component: ExpressAddToCartComponent
          },
        }
      }
    ),
  ],
  exports: [
    ExpressAddToCartComponent,
  ],
})
export class ExpressAddToCartComponentModule {
}
