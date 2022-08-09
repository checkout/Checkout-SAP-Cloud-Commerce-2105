import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {
  CheckoutComOrderConfirmationThankYouMessageComponent
} from './checkout-com-order-confirmation-thank-you-message.component';
import {
  CardModule,
  CartSharedModule,
  FormErrorsModule,
  OrderOverviewModule,
  PromotionsModule,
  PwaModule,
  SpinnerModule
} from '@spartacus/storefront';
import {FeaturesConfigModule, I18nModule} from '@spartacus/core';
import {NgxQRCodeModule} from '@techiediaries/ngx-qrcode';
import {CheckoutModule} from '@spartacus/checkout';
import {ReactiveFormsModule} from "@angular/forms";
import {CheckoutComGuestFormModule} from "../checkout-com-guest-form/checkout-com-guest-form.module";

@NgModule({
  declarations: [CheckoutComOrderConfirmationThankYouMessageComponent],
  imports: [
    CommonModule,
    CheckoutModule,
    CommonModule,
    CartSharedModule,
    CardModule,
    PwaModule,
    FeaturesConfigModule,
    PromotionsModule,
    I18nModule,
    ReactiveFormsModule,
    FormErrorsModule,
    OrderOverviewModule,
    PwaModule,
    NgxQRCodeModule,
    SpinnerModule,
    CheckoutComGuestFormModule,
  ]
})
export class CheckoutComOrderConfirmationThankYouMessageModule {
}
