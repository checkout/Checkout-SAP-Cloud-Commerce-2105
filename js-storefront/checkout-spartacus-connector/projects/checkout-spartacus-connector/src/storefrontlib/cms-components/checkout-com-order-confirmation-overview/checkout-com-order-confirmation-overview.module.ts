import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CheckoutComOrderConfirmationOverviewComponent } from './checkout-com-order-confirmation-overview.component';
import { CheckoutComOrderReviewModule } from '../checkout-com-order-review/checkout-com-order-review.module';

@NgModule({
  declarations: [CheckoutComOrderConfirmationOverviewComponent],
  imports: [
    CommonModule,
    CheckoutComOrderReviewModule
  ]
})
export class CheckoutComOrderConfirmationOverviewModule { }
