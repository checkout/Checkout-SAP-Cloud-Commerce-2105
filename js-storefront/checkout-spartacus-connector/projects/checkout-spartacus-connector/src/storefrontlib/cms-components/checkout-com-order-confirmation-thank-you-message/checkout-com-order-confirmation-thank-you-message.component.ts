import { ChangeDetectionStrategy, Component } from '@angular/core';
import { NgxQrcodeElementTypes, NgxQrcodeErrorCorrectionLevels } from '@techiediaries/ngx-qrcode';
import { OrderConfirmationThankYouMessageComponent } from '@spartacus/checkout/components';

@Component({
  selector: 'lib-checkout-com-order-confirmation-thank-you-message',
  templateUrl: './checkout-com-order-confirmation-thank-you-message.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CheckoutComOrderConfirmationThankYouMessageComponent extends OrderConfirmationThankYouMessageComponent {
  elementType = NgxQrcodeElementTypes.IMG;
  correctionLevel = NgxQrcodeErrorCorrectionLevels.HIGH;

}
