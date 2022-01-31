import { ChangeDetectionStrategy, Component } from '@angular/core';
import { Card } from '@spartacus/storefront';
import { ReviewSubmitComponent } from '@spartacus/checkout/components';
import { combineLatest, Observable } from 'rxjs';
import { map } from 'rxjs/operators';

@Component({
  selector: 'lib-checkout-com-review-submit',
  templateUrl: './checkout-com-review-submit.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CheckoutComReviewSubmitComponent extends ReviewSubmitComponent {

  /**
   * override the 2nd line to not show `Expiry / ` in case of APM but the name of the APM
   * @param paymentDetails either standard PaymentDetails or ApmPaymentDetails
   */
  getPaymentMethodCard(paymentDetails: any): Observable<Card> {
    return combineLatest([
      this.translation.translate('paymentForm.payment'),
      this.getPaymentDetailsLineTranslation(paymentDetails),
      this.translation.translate('paymentForm.billingAddress'),
    ]).pipe(
      map(([textTitle, textExpires, billingAddress]) => {
        const region = paymentDetails.billingAddress?.region?.isocode
          ? paymentDetails.billingAddress?.region?.isocode + ', '
          : '';
        return {
          title: textTitle,
          text: [paymentDetails.accountHolderName, paymentDetails.cardNumber, textExpires],
          paragraphs: [
            {
              title: billingAddress + ':',
              text: [
                paymentDetails.billingAddress?.firstName +
                ' ' +
                paymentDetails.billingAddress?.lastName,
                paymentDetails.billingAddress?.line1,
                paymentDetails.billingAddress?.town +
                ', ' +
                region +
                paymentDetails.billingAddress?.country?.isocode,
                paymentDetails.billingAddress?.postalCode,
              ],
            },
          ],
        };
      })
    );
  }

  protected getPaymentDetailsLineTranslation(paymentDetails: any) {
    let paymentDetailsTranslation;
    if (paymentDetails.expiryYear) {
      paymentDetailsTranslation = this.translation.translate('paymentCard.expires', {
        month: paymentDetails.expiryMonth,
        year: paymentDetails.expiryYear,
      });
    } else {
      paymentDetailsTranslation =
        this.translation.translate(
          'paymentCard.apm', {
            apm: paymentDetails.type
          }
        );
    }
    return paymentDetailsTranslation;
  }
}
