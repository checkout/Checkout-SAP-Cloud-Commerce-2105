import {ChangeDetectionStrategy, Component, OnDestroy} from '@angular/core';
import {CheckoutComApplepayService} from '../../../../core/services/applepay/checkout-com-applepay.service';
import {Subject} from 'rxjs';
import {filter, first, takeUntil} from 'rxjs/operators';
import {
  ApplePayAuthorization,
  ApplePayPaymentRequest,
  ApplePayShippingContactUpdate,
  ApplePayShippingMethodUpdate
} from '../../../../core/model/ApplePay';
import {ActiveCartService, RoutingService, UserIdService, WindowRef} from '@spartacus/core';
import {createApplePaySession} from '../../../../core/services/applepay/applepay-session';
import {CheckoutFacade} from '@spartacus/checkout/root';
import {getUserIdCartId} from "../../../../core/shared/get-user-cart-id";

@Component({
  selector: 'lib-checkout-com-apm-applepay',
  templateUrl: './checkout-com-apm-applepay.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CheckoutComApmApplepayComponent implements OnDestroy {
  private applePaySession: any;
  protected drop = new Subject();

  constructor(protected checkoutFacade: CheckoutFacade,
              protected routingService: RoutingService,
              protected checkoutComApplepayService: CheckoutComApplepayService,
              protected windowRef: WindowRef,
              protected userIdService: UserIdService,
              protected activeCartService: ActiveCartService
  ) {
  }

  ngOnDestroy(): void {
    this.drop.next();
  }

  getCardId(){
    getUserIdCartId(this.userIdService, this.activeCartService)
      .pipe(takeUntil(this.drop)).subscribe(({userId, cartId}) => {
        this.placeApplePayOrder(userId, cartId);
      }, err => console.error('getUserIdCartId', {err}));
  }

  placeApplePayOrder(userId?: string, cartId?: string): void {
    this.checkoutComApplepayService.getPaymentRequestFromState().pipe(
      filter(
        paymentRequest =>
          !!paymentRequest && Object.keys(paymentRequest).length > 0
      ),
      takeUntil(this.drop)
    ).subscribe((paymentRequest: ApplePayPaymentRequest) => {
      const modifiedPaymentRequest = this.modifyPaymentRequest(paymentRequest);
      this.applePaySession = this.checkoutComApplepayService.createSession(modifiedPaymentRequest, cartId, userId);
    }, err => console.error('placeApplePayOrder err', {err},err => console.log('getPaymentRequestFromState with errors', {err})));

    // handle merchant validation from ApplePay
    this.checkoutComApplepayService.getMerchantSesssionFromState().pipe(
      filter((session: any) => !!session && Object.keys(session).length > 0),
      takeUntil(this.drop)
    ).subscribe((session: any) => {
      this.applePaySession?.completeMerchantValidation(session);
    }, err => console.error('merchant session with error', {err}));

    // handle update of the delivery address in the ApplePay modal
    this.checkoutComApplepayService.getDeliveryAddressUpdateFromState()
      .pipe(
        filter((update: ApplePayShippingContactUpdate) => !!update && Object.keys(update).length > 0),
        takeUntil(this.drop)
      ).subscribe((update) => {
      this.applePaySession?.completeShippingContactSelection(update);
    }, err => console.error('delivery address update with error', {err}));

    // handle update of the delivery method in the ApplePay modal
    this.checkoutComApplepayService.getDeliveryMethodUpdateFromState()
      .pipe(
        filter((update: ApplePayShippingMethodUpdate) => !!update && Object.keys(update).length > 0),
        takeUntil(this.drop)
      ).subscribe((update) => {
      this.applePaySession?.completeShippingMethodSelection(update);
    }, err => console.error('delivery method update with error', {err}));

    // handle the payment authorization from ApplePay
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
    }, err => console.error('payment authorization with error', {err}));

    // navigate to the order details page if order was created
    this.checkoutFacade.getOrderDetails().pipe(
      filter(order => Object.keys(order).length !== 0),
      takeUntil(this.drop)
    ).subscribe(() => {
      this.routingService.go({cxRoute: 'orderConfirmation'});
    }, err => console.error('return to order confirmation with errors', {err}));
  }

  /**
   * Method you can override to add more details to the payment request
   *
   * @param paymentRequest
   */
  protected modifyPaymentRequest(paymentRequest: ApplePayPaymentRequest): ApplePayPaymentRequest {
    return paymentRequest
  }

}
