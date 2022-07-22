import {Component, Input, OnDestroy, OnInit} from '@angular/core';
import {CheckoutComApplepayService} from '../../../../core/services/applepay/checkout-com-applepay.service';
import {
  ActiveCartService,
  Cart,
  MultiCartService, OCC_USER_ID_ANONYMOUS,
  RoutingService,
  StateUtils,
  UserIdService,
  WindowRef
} from '@spartacus/core';
import {
  CheckoutComApmApplepayComponent
} from '../../checkout-com-apm-component/checkout-com-apm-applepay/checkout-com-apm-applepay.component';
import {CheckoutFacade} from '@spartacus/checkout/root';
import {filter, map, switchMap, takeUntil, tap} from 'rxjs/operators';
import {ApplePayPaymentRequest} from '../../../../core/model/ApplePay';
import {Subject} from "rxjs";
import {getUserIdCartId} from "../../../../core/shared/get-user-cart-id";


@Component({
  selector: 'lib-checkout-com-express-applepay',
  templateUrl: './checkout-com-express-applepay.component.html',
})
export class CheckoutComExpressApplepayComponent extends CheckoutComApmApplepayComponent implements OnInit, OnDestroy {
  @Input() expressCheckout?: boolean;
  @Input() productCode?: string;
  userId: string;
  cartId: string;
  protected drop = new Subject();

  constructor(
    protected checkoutFacade: CheckoutFacade,
    protected routingService: RoutingService,
    protected checkoutComApplepayService: CheckoutComApplepayService,
    protected windowRef: WindowRef,
    protected userIdService: UserIdService,
    protected activeCartService: ActiveCartService,
    protected multiCartService: MultiCartService,
  ) {
    super(checkoutFacade, routingService, checkoutComApplepayService, windowRef, userIdService, activeCartService);
  }


  ngOnInit(): void {
    if (this.expressCheckout) {
      this.onLoadCart();
    }
  }

  onLoadCart(): void {
    this.userIdService.takeUserId().pipe(
      switchMap((userId: string) =>
        this.multiCartService
          .createCart({
            userId,
            extraData: {active: false},
          })
          .pipe(
            filter((cartData: StateUtils.ProcessesLoaderState<Cart>) =>
              Boolean(cartData.value?.code)
            ),
            map(
              (cartData: StateUtils.ProcessesLoaderState<Cart>) => {
                return userId === OCC_USER_ID_ANONYMOUS ? cartData.value?.guid : cartData.value?.code as string
              }
            ),

            tap((cartId: string) => {
                this.userId = userId;
                this.cartId = cartId;
                this.multiCartService.addEntry(userId, cartId, this.productCode, 1);
                this.multiCartService.getCartEntity(cartId).pipe(
                  filter(response => response.loading),
                  takeUntil(this.drop)
                ).subscribe(() => {
                  this.checkoutComApplepayService.requestApplePayPaymentRequest(userId, cartId);
                }, error => {
                  console.log(error)
                })
              },
              takeUntil(this.drop))
          )
      )
    ).pipe(takeUntil(this.drop)).subscribe()
  }

  onExpressClick(): void {
    if (this.expressCheckout) {
      this.placeApplePayOrder(this.userId, this.cartId);
    } else {
      getUserIdCartId(this.userIdService, this.activeCartService).pipe(takeUntil(this.drop))
        .subscribe(({userId, cartId}) => {
          this.placeApplePayOrder(userId, cartId);
        }, err => console.error('getUserIdCartId', {err}));

    }
  }

  /**
   * Modify the payment request to also require the shipping information
   *
   * @param paymentRequest default payment request from OCC
   */
  protected modifyPaymentRequest(paymentRequest: ApplePayPaymentRequest): ApplePayPaymentRequest {
    return {
      ...paymentRequest,
      requiredShippingContactFields: [
        "postalAddress",
        "name",
        "email"
      ]
    }
  }

  ngOnDestroy() {
    super.ngOnDestroy();
    this.drop.next();
  }
}
