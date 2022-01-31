import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { Card, OrderOverviewComponent } from '@spartacus/storefront';
import { combineLatest, Observable } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { getPaymentDetailsLineTranslation } from '../../../core/shared/paymentDetails';

@Component({
  selector: 'cx-order-review',
  templateUrl: './checkout-com-order-review.component.html',
  styleUrls: ['./checkout-com-order-review.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CheckoutComOrderReviewComponent extends OrderOverviewComponent {
  @Input()
  order: any;

  getPaymentInfoCardContentWithApm(payment: any, paymentType: string): Observable<Card> {
    return combineLatest([
      this.translation.translate('paymentForm.payment'),
      getPaymentDetailsLineTranslation(this.translation, payment, paymentType),
    ]).pipe(
      filter(() => Boolean(payment)),
      map(([textTitle, textExpires]) => ({
        title: textTitle,
        text: [payment.accountHolderName, payment.cardNumber, textExpires],
      }))
    );
  }
}
