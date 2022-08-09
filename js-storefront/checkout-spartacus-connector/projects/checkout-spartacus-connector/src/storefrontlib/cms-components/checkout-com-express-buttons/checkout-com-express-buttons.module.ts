import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CheckoutComExpressApplepayComponent } from './checkout-com-express-applepay/checkout-com-express-applepay.component';
import { CheckoutComExpressGooglepayComponent } from './checkout-com-express-googlepay/checkout-com-express-googlepay.component';
import { CheckoutComStoreModule } from '../../../core/store/checkout-com-store.module';
import { CheckoutComOccModule } from '../../../core/adapters/occ/checkout-com-occ.module';
import { CheckoutComApmModule } from '../checkout-com-apm-component/checkout-com-apm.module';

@NgModule({
  declarations: [
    CheckoutComExpressApplepayComponent,
    CheckoutComExpressGooglepayComponent,
  ],
  imports: [
    CommonModule,

    CheckoutComStoreModule,
    CheckoutComOccModule,
    CheckoutComApmModule,
  ],
  exports: [
    CheckoutComExpressApplepayComponent,
    CheckoutComExpressGooglepayComponent,
  ],
})
export class CheckoutComExpressButtonsModule { }
