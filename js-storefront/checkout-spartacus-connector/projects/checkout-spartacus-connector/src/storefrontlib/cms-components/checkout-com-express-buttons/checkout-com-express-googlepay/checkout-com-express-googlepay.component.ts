import {
  AfterViewInit,
  Component,
  EventEmitter,
  Input,
  NgZone, OnChanges,
  OnDestroy,
  OnInit,
  Output,
  Renderer2
} from '@angular/core';
import {ActiveCartService, GlobalMessageService, RoutingService, UserIdService, WindowRef} from '@spartacus/core';
import {Observable, Subject} from 'rxjs';
import {first, map, switchMap, takeUntil} from 'rxjs/operators';
import {CheckoutComGooglepayService} from '../../../../core/services/googlepay/checkout-com-googlepay.service';
import {CheckoutFacade} from '@spartacus/checkout/root';
import {CheckoutComPaymentService} from '../../../../core/services/checkout-com-payment.service';
import {
  CheckoutComApmGooglepayComponent
} from '../../checkout-com-apm-component/checkout-com-apm-googlepay/checkout-com-apm-googlepay.component';
import {getUserIdCartId} from '../../../../core/shared/get-user-cart-id';
import {loadScript} from "../../../../core/shared/loadScript";

@Component({
  selector: 'lib-checkout-com-express-googlepay',
  templateUrl: './checkout-com-express-googlepay.component.html',
})
export class CheckoutComExpressGooglepayComponent extends CheckoutComApmGooglepayComponent implements OnInit, OnDestroy, AfterViewInit,OnChanges {

  protected drop = new Subject();
  @Output() buttonGooglePayClicked = new EventEmitter<boolean>();
  @Input() expressCheckout? : boolean;
  @Input() newCart? : Observable<boolean>;

  constructor(
    protected checkoutComGooglePayService: CheckoutComGooglepayService,
    protected globalMessageService: GlobalMessageService,
    protected checkoutFacade: CheckoutFacade,
    protected routingService: RoutingService,
    protected checkoutComPaymentService: CheckoutComPaymentService,
    protected userIdService: UserIdService,
    protected activeCartService: ActiveCartService,
    protected ngZone: NgZone,
    protected windowRef: WindowRef,
    protected renderer: Renderer2,
  ) {
    super(checkoutComGooglePayService,
      globalMessageService,
      checkoutFacade,
      routingService,
      checkoutComPaymentService,
      userIdService,
      activeCartService,
      ngZone,
      windowRef,
      renderer
    );

    this.shippingAddressRequired = true;
  }

  /**
   * Request the merchant configuration when button is available and the cart is loaded.
   * In Checkout, the APM Component loads the merchant configuration
   */
  ngAfterViewInit() {
    if (this.gpayBtn?.nativeElement) {
      const idButton = this.windowRef.document.getElementById("google-id-button");
      // @ts-ignore
      if (!idButton) {
        loadScript(this.windowRef, 'https://pay.google.com/gp/p/js/pay.js', () => {
          this.ngZone.run(() => {
            // @ts-ignore
            this.paymentsClient = new google.payments.api.PaymentsClient({});
            const button = this.paymentsClient?.createButton({
              onClick: () => {
                this.ngZone.run(() => {
                  this.googlePayButtonClicked();
                });
              }
            });
            if (this.gpayBtn?.nativeElement.children.length === 0) {
              this.renderer.setAttribute(this.gpayBtn?.nativeElement, 'id', 'google-pay-button');
              this.renderer.appendChild(this.gpayBtn?.nativeElement, button)
            }
          });
        }, 'google-id-button');
      } else {
        this.createdButton(idButton);
      }
    }
  }
  ngOnChanges(changes){
    if(changes["newCart"] && this.newCart){
      this.newCart.pipe(
        takeUntil(this.drop)
      ).subscribe(value => {
        if(value){
          this.startGooglePayPayment();
        }
      }, error => {console.log(error)});
    }
  }
  protected createdButton(idButton?: any) {
    idButton.remove();
    // @ts-ignore
    this.paymentsClient = new google.payments.api.PaymentsClient({});
    const button = this.paymentsClient?.createButton({
      onClick: () => {
        this.ngZone.run(() => {
          this.googlePayButtonClicked();
        });
      }
    });
    if (this.gpayBtn?.nativeElement.children.length === 0) {
      this.renderer.setAttribute(this.gpayBtn?.nativeElement, 'id', 'google-pay-button');
      this.renderer.appendChild(this.gpayBtn?.nativeElement, button)
    }
  }

  googlePayButtonClicked(): void {
    if(this.expressCheckout){
      this.buttonGooglePayClicked.emit(true);
    }
    else{
      this.startGooglePayPayment();
    }
  }

  protected startGooglePayPayment() {
    getUserIdCartId(this.userIdService, this.activeCartService)
      .pipe(
        switchMap(({userId, cartId}) => {
          // request merchant configuration when cart is available
          this.checkoutComGooglePayService.requestMerchantConfiguration(userId, cartId);
          return this.checkoutComGooglePayService.getMerchantConfigurationFromState().pipe(
            first(c => !!c), map(_ => ({userId, cartId}))
          );
        }),
        takeUntil(this.drop)
      )
      .subscribe(({userId, cartId}) => {
        this.initBtn(userId, cartId);
      },err => console.error('startGooglePayPayment with errors', {err}));
  }

  protected initBtn(userId: string, cartId: string) {
    this.checkoutComGooglePayService.getMerchantConfigurationFromState()
      .pipe(first(merchantConfiguration => merchantConfiguration !== undefined && Object.keys(merchantConfiguration).length > 0),
        takeUntil(this.drop))
      .subscribe((merchantConfiguration) => {
        let paymentClientData = merchantConfiguration;
        // clone the object, Rx objects are immutable deep
        paymentClientData = JSON.parse(JSON.stringify(merchantConfiguration));
        const onPaymentDataChanged = (paymentData) => this.checkoutComGooglePayService.onPaymentDataChanged(cartId, userId, paymentData);
        paymentClientData.clientSettings.paymentDataCallbacks = {
          onPaymentDataChanged,
        };
        this.initExpressPaymentsClient(paymentClientData, userId, cartId);

      }, err => console.error('initBtn with errors', {err}));
  }

  protected initExpressPaymentsClient(merchantConfiguration, userId: string, cartId: string) {
    // @ts-ignore
    this.paymentsClient = new google.payments.api.PaymentsClient(merchantConfiguration.clientSettings);
    const isReadyToPayRequest: object = this.checkoutComGooglePayService
      .createInitialPaymentRequest(merchantConfiguration, this.shippingAddressRequired);
    this.paymentsClient.isReadyToPay(isReadyToPayRequest)
      .then(({result}) => {
        if (result) {
          this.authorisePayment(userId, cartId);
        }
      })
      .catch(err => {
        console.error('failed to initialize googlepay', err);
      });
  }

  protected authorisePayment(userId: string, cartId: string) {
    this.checkoutComGooglePayService.getMerchantConfigurationFromState()
      .pipe(
        first(merchantConfiguration => merchantConfiguration !== undefined && Object.keys(merchantConfiguration).length > 0),
        takeUntil(this.drop)
      ).subscribe((merchantConfiguration) => {
      const paymentDataRequest: any =
        this.checkoutComGooglePayService.addPaymentExpressIntents(
          this.checkoutComGooglePayService.createFullPaymentRequest(merchantConfiguration)
        );

      this.paymentsClient.loadPaymentData(paymentDataRequest)
        .then((paymentRequest) => {
          this.checkoutComGooglePayService.onPaymentAuthorized(cartId, userId, paymentRequest);
        })
        .catch(function (err) {
          console.error(err);
        });
    }, err => console.error('authorisePayment with errors', {err}));
  }

  ngOnDestroy(): void {
    this.drop.next();
    super.ngOnDestroy();
  }
}
