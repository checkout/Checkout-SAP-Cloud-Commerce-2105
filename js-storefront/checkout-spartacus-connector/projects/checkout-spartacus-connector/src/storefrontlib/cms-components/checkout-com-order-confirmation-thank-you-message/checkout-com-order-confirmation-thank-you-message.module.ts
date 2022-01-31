import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CheckoutComOrderConfirmationThankYouMessageComponent } from './checkout-com-order-confirmation-thank-you-message.component';
import { PwaModule, SpinnerModule } from '@spartacus/storefront';
import { I18nModule } from '@spartacus/core';
import { NgxQRCodeModule } from '@techiediaries/ngx-qrcode';
import { CheckoutModule } from '@spartacus/checkout';
import { OrderConfirmationModule } from '@spartacus/checkout/components';

@NgModule({
  declarations: [CheckoutComOrderConfirmationThankYouMessageComponent],
  imports: [
    CommonModule,
    CheckoutModule,
    OrderConfirmationModule,
    I18nModule,
    PwaModule,
    NgxQRCodeModule,
    SpinnerModule,
  ]
})
export class CheckoutComOrderConfirmationThankYouMessageModule { }
