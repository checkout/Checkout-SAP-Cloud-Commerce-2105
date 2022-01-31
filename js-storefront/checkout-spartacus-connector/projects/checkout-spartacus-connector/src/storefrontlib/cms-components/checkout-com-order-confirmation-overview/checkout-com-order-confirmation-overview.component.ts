import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { OrderConfirmationOverviewComponent } from '@spartacus/checkout/components';
import { Observable } from 'rxjs';

@Component({
  selector: 'cx-order-confirmation-overview',
  templateUrl: './checkout-com-order-confirmation-overview.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CheckoutComOrderConfirmationOverviewComponent extends OrderConfirmationOverviewComponent {

  @Input()
  order$: Observable<any>;

}
