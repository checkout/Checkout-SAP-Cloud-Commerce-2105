import { Component, OnDestroy, OnInit } from '@angular/core';
import { CheckoutComApplepayService, CheckoutComGooglepayService, getUserIdCartId } from 'checkout-spartacus-connector';
import { CartTotalsComponent } from '@spartacus/storefront';
import {ActiveCartService, UserIdService, WindowRef} from '@spartacus/core';
import { takeUntil } from 'rxjs/operators';
import { Subject } from 'rxjs';
import {
  createApplePaySession
} from "../../../../../checkout-spartacus-connector/src/core/services/applepay/applepay-session";

@Component({
  selector: 'app-express-cart-totals',
  templateUrl: './express-cart-totals.component.html',
})
export class ExpressCartTotalsComponent extends CartTotalsComponent implements OnInit, OnDestroy {
  applePay = false;
  private drop = new Subject<void>();

  constructor(
    activeCartService: ActiveCartService,
    protected userIdService: UserIdService,
    protected checkoutComApplepayService: CheckoutComApplepayService,
    protected checkoutComGooglePayService: CheckoutComGooglepayService,
    protected windowRef?: WindowRef
  ) {
    super(activeCartService);
  }

  ngOnInit(): void {
    super.ngOnInit();
    this.showApplePay();
    getUserIdCartId(this.userIdService, this.activeCartService)
      .pipe(takeUntil(this.drop))
      .subscribe(({userId, cartId}) => {
        this.checkoutComGooglePayService.requestMerchantConfiguration(userId, cartId);
        this.checkoutComApplepayService.requestApplePayPaymentRequest(userId, cartId);
      }, err => console.log('getUserIdCartId with errors', {err}));
  }

  ngOnDestroy(): void {
    this.drop.next();
  }

  showApplePay() {
    const ApplePaySession = createApplePaySession(this.windowRef);
    this.applePay = !!(ApplePaySession && ApplePaySession.canMakePayments());
  }
}
