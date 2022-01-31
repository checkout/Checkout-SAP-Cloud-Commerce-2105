import { Component, ChangeDetectionStrategy, OnInit } from '@angular/core';
import { NgxQrcodeElementTypes, NgxQrcodeErrorCorrectionLevels } from '@techiediaries/ngx-qrcode';
import { Consignment, PromotionLocation, PromotionResult } from '@spartacus/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { OrderDetailItemsComponent, OrderDetailsService } from '@spartacus/order/components';

@Component({
  selector: 'cx-order-details-items',
  templateUrl: './checkout-com-order-detail-items.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CheckoutComOrderDetailItemsComponent extends OrderDetailItemsComponent implements OnInit {
  elementType = NgxQrcodeElementTypes.IMG;
  correctionLevel = NgxQrcodeErrorCorrectionLevels.HIGH;

  // hack because SAP doesn't export them !!!
  completedValues = ['DELIVERY_COMPLETED', 'PICKUP_COMPLETE'];
  cancelledValues = ['CANCELLED'];

  // OOTB
  promotionLocation: PromotionLocation = PromotionLocation.Order;
  order$: Observable<any> = this.orderDetailsService.getOrderDetails();
  others$: Observable<Consignment[]>;
  completed$: Observable<Consignment[]>;
  cancel$: Observable<Consignment[]>;

  constructor(
    protected orderDetailsService: OrderDetailsService,
  ) {
    super(orderDetailsService);
  }

  ngOnInit() {
    this.others$ = this.otherStatus(...this.completedValues, ...this.cancelledValues);
    this.completed$ = this.exactStatus(this.completedValues);
    this.cancel$ = this.exactStatus(this.cancelledValues);
  }

  protected exactStatus(
    consignmentStatus: string[]
  ): Observable<Consignment[]> {
    return this.order$.pipe(
      map((order) => {
        if (Boolean(order.consignments)) {
          return order.consignments.filter((consignment) =>
            consignmentStatus.includes(consignment.status)
          );
        }
      })
    );
  }

  protected otherStatus(
    ...consignmentStatus: string[]
  ): Observable<Consignment[]> {
    return this.order$.pipe(
      map((order) => {
        if (Boolean(order.consignments)) {
          return order.consignments.filter(
            (consignment) => !consignmentStatus.includes(consignment.status)
          );
        }
      })
    );
  }
}
