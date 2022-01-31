import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CheckoutComOrderDetailItemsComponent } from './checkout-com-order-detail-items.component';
import { NgxQRCodeModule } from '@techiediaries/ngx-qrcode';
import {
  CardModule,
  CartSharedModule,
  OrderOverviewModule,
  PromotionsModule,
  SpinnerModule,
} from '@spartacus/storefront';
import { FeaturesConfigModule, I18nModule, UrlModule } from '@spartacus/core';
import { CheckoutModule } from '@spartacus/checkout';
import { OrderDetailsModule } from '@spartacus/order/components';

@NgModule({
  declarations: [CheckoutComOrderDetailItemsComponent],
  imports: [
    CartSharedModule,
    CardModule,
    CommonModule,
    I18nModule,
    FeaturesConfigModule,
    PromotionsModule,
    OrderOverviewModule,
    UrlModule,
    SpinnerModule,
    NgxQRCodeModule,
    CheckoutModule,
    OrderDetailsModule,
  ]
})
export class CheckoutComOrderDetailItemsModule { }
