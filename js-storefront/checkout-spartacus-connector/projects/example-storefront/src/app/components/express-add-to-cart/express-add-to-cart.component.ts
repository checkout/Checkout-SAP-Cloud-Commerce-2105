import {ChangeDetectionStrategy, ChangeDetectorRef, Component, OnChanges, OnDestroy} from '@angular/core';
import {ActiveCartService, CmsAddToCartComponent, MultiCartService, UserIdService, WindowRef} from '@spartacus/core';
import {AddToCartComponent, CmsComponentData, CurrentProductService, ModalService} from '@spartacus/storefront';
import {CheckoutComApplepayService, CheckoutComGooglepayService, getUserIdCartId} from 'checkout-spartacus-connector';
import {combineLatest, Observable, of, Subject} from 'rxjs';
import {filter, map, take, takeUntil} from 'rxjs/operators';
import {
  createApplePaySession
} from "../../../../../checkout-spartacus-connector/src/core/services/applepay/applepay-session";

@Component({
  selector: 'app-express-add-to-cart-component',
  templateUrl: './express-add-to-cart.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush

})
export class ExpressAddToCartComponent extends AddToCartComponent implements OnDestroy {
  applePay = false;
  loadPaymentGoogleConfigurationSuccess = of(false);
  private drop = new Subject<void>();

  constructor(
    modalService: ModalService,
    currentProductService: CurrentProductService,
    cd: ChangeDetectorRef,
    activeCartService: ActiveCartService,
    protected checkoutComApplepayService: CheckoutComApplepayService,
    protected checkoutComGooglePayService: CheckoutComGooglepayService,
    protected userIdService: UserIdService,
    component?: CmsComponentData<CmsAddToCartComponent>,
    protected multiCartService?: MultiCartService,
    protected windowRef?: WindowRef
  ) {
    super(modalService, currentProductService, cd, activeCartService, component);
    this.showApplePay();
  }

  showApplePay() {
    const ApplePaySession = createApplePaySession(this.windowRef);
    this.applePay = !!(ApplePaySession && ApplePaySession.canMakePayments());
  }

  ngOnDestroy(): void {
    this.drop.next();
  }

  public removeCart(): void {
    getUserIdCartId(this.userIdService, this.activeCartService)
      .pipe(take(1),takeUntil(this.drop))
      .subscribe(({userId, cartId}) => {
        this.multiCartService.deleteCart(cartId, userId);
      }, err => console.log('getUserIdCartId with errors', {err}));
  }

  expressAddToCart(googlePay?: boolean) {
    let removeCart = true, emptyCart = true;
    const quantity = this.addToCartForm.get('quantity').value;
    this.activeCartService
      .getEntries().pipe(
      map((entries) => {
        if (entries.length === 0 && emptyCart) {
          emptyCart = false;
          removeCart = false;
          this.activeCartService.addEntry(this.productCode, quantity);
          this.loadPaymentConfiguration(googlePay);
        } else if (entries.length !== 0 && removeCart) {
          removeCart = true;
          this.removeCart();
        }
      }),
      takeUntil(this.drop)
    ).subscribe();
  }

  protected loadPaymentConfiguration(googlePay?: boolean): void {
    combineLatest([
      this.activeCartService.isStable(),
      getUserIdCartId(this.userIdService, this.activeCartService)
    ])
      .pipe(
        filter(([loaded]) => !!loaded),
        take(1),
        takeUntil(this.drop),
      )
      .subscribe(([_, {userId, cartId}]) => {
        this.checkoutComGooglePayService.requestMerchantConfiguration(userId, cartId);
        this.checkoutComApplepayService.requestApplePayPaymentRequest(userId, cartId);
        if (googlePay) {
          this.loadPaymentGoogleConfigurationSuccess = of(true);
          this.cd.markForCheck();
        }
      }, err => console.error('requesting payment request after add to cart with errors', {err}));
  }

  // We override the openModal function to request the ApplePay payment request after add to cart
  protected openModal(): void {
    super.openModal();
    combineLatest([
      this.activeCartService.isStable(),
      getUserIdCartId(this.userIdService, this.activeCartService)
    ])
      .pipe(
        filter(([loaded]) => !!loaded),
        take(1),
        takeUntil(this.drop),
      )
      .subscribe(([_, {userId, cartId}]) => {
        this.checkoutComGooglePayService.requestMerchantConfiguration(userId, cartId);
        this.checkoutComApplepayService.requestApplePayPaymentRequest(userId, cartId);
      }, err => console.error('requesting payment request after add to cart with errors', {err}));
  }
}
