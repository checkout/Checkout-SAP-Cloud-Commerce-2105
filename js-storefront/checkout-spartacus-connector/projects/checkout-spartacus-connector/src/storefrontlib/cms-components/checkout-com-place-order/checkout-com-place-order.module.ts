import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CheckoutComPlaceOrderComponent } from './checkout-com-place-order.component';
import { UrlModule, I18nModule, CmsConfig, provideConfig } from '@spartacus/core';
import { RouterModule } from '@angular/router';
import { ReactiveFormsModule } from '@angular/forms';

@NgModule({
  declarations: [CheckoutComPlaceOrderComponent],
  imports: [
    CommonModule,
    RouterModule,
    UrlModule,
    I18nModule,
    ReactiveFormsModule,
  ],
  providers: [
    provideConfig({
      cmsComponents: {
        CheckoutPlaceOrder: {
          component: CheckoutComPlaceOrderComponent
        }
      }
    } as CmsConfig),
  ],
})
export class CheckoutComPlaceOrderModule {}
