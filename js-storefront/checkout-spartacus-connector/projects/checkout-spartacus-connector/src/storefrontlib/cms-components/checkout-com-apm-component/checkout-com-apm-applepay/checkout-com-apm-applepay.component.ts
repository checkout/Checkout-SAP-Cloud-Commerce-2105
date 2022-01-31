import { ChangeDetectionStrategy, Component, OnDestroy } from '@angular/core';
import { CheckoutComApplepayService } from '../../../../core/services/applepay/checkout-com-applepay.service';
import { Subject } from 'rxjs';
import { filter, takeUntil } from 'rxjs/operators';
import { ApplePayAuthorization, ApplePayPaymentRequest } from '../../../../core/model/ApplePay';
import { RoutingService, WindowRef } from '@spartacus/core';
import { createApplePaySession } from '../../../../core/services/applepay/applepay-session';
import { CheckoutFacade } from '@spartacus/checkout/root';

@Component({
  selector: 'lib-checkout-com-apm-applepay',
  templateUrl: './checkout-com-apm-applepay.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CheckoutComApmApplepayComponent implements OnDestroy {
  private applePaySession: any;
  private drop = new Subject();

  constructor(protected checkoutFacade: CheckoutFacade,
              protected routingService: RoutingService,
              protected checkoutComApplepayService: CheckoutComApplepayService,
              protected windowRef: WindowRef,
  ) {
  }

  ngOnDestroy(): void {
    this.drop.next();
  }

  placeApplePayOrder(): void {
    this.checkoutComApplepayService.getPaymentRequestFromState().pipe(
      filter(
        paymentRequest =>
          !!paymentRequest && Object.keys(paymentRequest).length > 0
      ),
      takeUntil(this.drop)
    ).subscribe((paymentRequest: ApplePayPaymentRequest) => {
      this.applePaySession = this.checkoutComApplepayService.createSession(
        paymentRequest
      );
    });

    this.checkoutComApplepayService.getMerchantSesssionFromState().pipe(
      filter((session: any) => !!session && Object.keys(session).length > 0),
      takeUntil(this.drop)
    ).subscribe((session: any) => {
      this.applePaySession.completeMerchantValidation(session);
    });

    this.checkoutComApplepayService.getPaymentAuthorizationFromState().pipe(
      filter(
        (authorization: ApplePayAuthorization) =>
          !!authorization && Object.keys(authorization).length > 0
      ),
      takeUntil(this.drop)
    ).subscribe((authorization: ApplePayAuthorization) => {
      const ApplePaySession = createApplePaySession(this.windowRef);
      const statusCode =
        authorization.status === 'SUCCESS'
          ? ApplePaySession.STATUS_SUCCESS
          : ApplePaySession.STATUS_FAILURE;

      this.applePaySession.completePayment({
        status: statusCode
      });
    });

    this.checkoutFacade.getOrderDetails().pipe(
      filter(order => Object.keys(order).length !== 0),
      takeUntil(this.drop)
    ).subscribe(() => {
      this.routingService.go({cxRoute: 'orderConfirmation'});
    });
  }

}
