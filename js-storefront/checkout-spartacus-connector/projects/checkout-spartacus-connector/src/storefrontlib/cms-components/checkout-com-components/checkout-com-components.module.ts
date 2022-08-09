import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {CheckoutComPaymentFormModule} from '../checkout-com-payment-form/checkout-com-payment-form.module';
import {CheckoutComPaymentMethodModule} from '../checkout-com-payment-method/checkout-com-payment-method.module';
import {CheckoutComPlaceOrderModule} from '../checkout-com-place-order/checkout-com-place-order.module';
import {CheckoutComApmModule} from '../checkout-com-apm-component/checkout-com-apm.module';
import {CmsConfig, provideConfig} from '@spartacus/core';
import {CheckoutComCheckoutGuard} from '../../../core/guards/checkout-com-checkout.guard';
import {CheckoutComReviewSubmitModule} from '../checkout-com-review-submit/checkout-com-review-submit.module';
import {CheckoutComOrderReviewModule} from '../checkout-com-order-review/checkout-com-order-review.module';
import {
  CheckoutComOrderConfirmationOverviewComponent
} from '../checkout-com-order-confirmation-overview/checkout-com-order-confirmation-overview.component';
import {
  CheckoutComOrderConfirmationThankYouMessageComponent
} from '../checkout-com-order-confirmation-thank-you-message/checkout-com-order-confirmation-thank-you-message.component';
import {
  CheckoutComOrderDetailItemsComponent
} from '../checkout-com-order-detail-items/checkout-com-order-detail-items.component';
import {
  CheckoutComOrderDetailItemsModule
} from '../checkout-com-order-detail-items/checkout-com-order-detail-items.module';
import {
  CheckoutComOrderDetailShippingModule
} from '../checkout-com-order-detail-shipping/checkout-com-order-detail-shipping.module';
import {
  CheckoutComOrderDetailShippingComponent
} from '../checkout-com-order-detail-shipping/checkout-com-order-detail-shipping.component';
import {OrderConfirmationItemsComponent, OrderConfirmationTotalsComponent} from '@spartacus/checkout/components';
import {
  CheckoutComOrderConfirmationThankYouMessageModule
} from "../checkout-com-order-confirmation-thank-you-message/checkout-com-order-confirmation-thank-you-message.module";
import {CheckoutComGuestFormComponent} from "../checkout-com-guest-form/checkout-com-guest-form.component";

@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    CheckoutComPaymentFormModule,
    CheckoutComPaymentMethodModule,
    CheckoutComPlaceOrderModule,
    CheckoutComApmModule,
    CheckoutComReviewSubmitModule,
    CheckoutComOrderReviewModule,
    CheckoutComOrderDetailItemsModule,
    CheckoutComOrderDetailShippingModule,
    CheckoutComOrderConfirmationThankYouMessageModule,
  ],
  providers: [
    provideConfig({
      cmsComponents: {
        OrderConfirmationThankMessageComponent: {
          component: CheckoutComOrderConfirmationThankYouMessageComponent,
          guards: [CheckoutComCheckoutGuard],
        },
        OrderConfirmationItemsComponent: {
          component: OrderConfirmationItemsComponent,
          guards: [CheckoutComCheckoutGuard],
        },
        OrderConfirmationTotalsComponent: {
          component: OrderConfirmationTotalsComponent,
          guards: [CheckoutComCheckoutGuard],
        },
        OrderConfirmationOverviewComponent: {
          component: CheckoutComOrderConfirmationOverviewComponent,
          guards: [CheckoutComCheckoutGuard],
        },
        GuestRegisterFormComponent: {
          component: CheckoutComGuestFormComponent,
        },
        AccountOrderDetailsItemsComponent: {
          component: CheckoutComOrderDetailItemsComponent,
        },
        AccountOrderDetailsShippingComponent: {
          component: CheckoutComOrderDetailShippingComponent,
        },
      },
    } as CmsConfig)]
})

export class CheckoutComComponentsModule {
}
