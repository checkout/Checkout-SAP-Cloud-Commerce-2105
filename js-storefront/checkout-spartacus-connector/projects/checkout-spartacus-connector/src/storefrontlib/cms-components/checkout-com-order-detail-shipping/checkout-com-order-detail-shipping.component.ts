import { Component } from '@angular/core';
import { OrderDetailShippingComponent } from '@spartacus/storefront';
import { Observable } from 'rxjs';

@Component({
  selector: 'cx-order-details-shipping',
  templateUrl: './checkout-com-order-detail-shipping.component.html'
})
export class CheckoutComOrderDetailShippingComponent extends OrderDetailShippingComponent {
  order$: Observable<any> = this.orderDetailsService.getOrderDetails();
}
