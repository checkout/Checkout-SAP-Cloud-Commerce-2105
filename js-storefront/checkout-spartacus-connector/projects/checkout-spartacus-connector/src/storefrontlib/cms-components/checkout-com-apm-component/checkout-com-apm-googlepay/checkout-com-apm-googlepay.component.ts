import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  Input,
  NgZone,
  OnDestroy,
  OnInit, Renderer2,
  ViewChild
} from '@angular/core';
import {BehaviorSubject, Subject} from 'rxjs';
import {
  ActiveCartService,
  GlobalMessageService,
  GlobalMessageType,
  RoutingService,
  UserIdService,
  WindowRef
} from '@spartacus/core';
import {CheckoutComGooglepayService} from '../../../../core/services/googlepay/checkout-com-googlepay.service';
import {filter, first, switchMap, take, takeUntil} from 'rxjs/operators';
import {CheckoutComPaymentService} from '../../../../core/services/checkout-com-payment.service';
import {FormGroup} from '@angular/forms';
import {makeFormErrorsVisible} from '../../../../core/shared/make-form-errors-visible';
import {loadScript} from '../../../../core/shared/loadScript';
import {getUserIdCartId} from '../../../../core/shared/get-user-cart-id';
import {CheckoutFacade} from '@spartacus/checkout/root';

@Component({
  selector: 'lib-checkout-com-apm-googlepay',
  templateUrl: './checkout-com-apm-googlepay.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CheckoutComApmGooglepayComponent implements OnInit, AfterViewInit, OnDestroy {
  @Input() billingAddressForm: FormGroup = new FormGroup({});
  @ViewChild('gpayBtn') gpayBtn: ElementRef = null;
  public sameAsShippingAddress$ = new BehaviorSubject<boolean>(true);

  protected paymentsClient: any;
  protected drop = new Subject();
  protected shippingAddressRequired = false;

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
  }

  ngOnInit(): void {
    this.checkoutFacade.getOrderDetails().pipe(
      filter(order => Object.keys(order).length !== 0), takeUntil(this.drop)
    ).subscribe((order) => {
      this.routingService.go({cxRoute: 'orderConfirmation'});
    }, err => console.error('return to order confirmation with errors', {err}));
  }

  ngAfterViewInit() {
    if (this.gpayBtn?.nativeElement) {
      getUserIdCartId(this.userIdService, this.activeCartService)
        .pipe(takeUntil(this.drop))
        .subscribe(({userId, cartId}) => {
          this.initBtn(userId, cartId);
        }, err => console.error('get user id and cart id with erros', {err}));
    }
  }

  protected initBtn(userId: string, cartId: string, express = false) {
    this.checkoutComGooglePayService.getMerchantConfigurationFromState()
      .pipe(take(1), takeUntil(this.drop))
      .subscribe((merchantConfiguration) => {
        if (this.paymentsClient) {
          return;
        }

        let paymentClientData = merchantConfiguration;
        if (express) {
          // clone the object, Rx objects are immutable deep
          paymentClientData = JSON.parse(JSON.stringify(merchantConfiguration));
          const onPaymentAuthorized = (paymentData) => this.checkoutComGooglePayService.onPaymentAuthorized(cartId, userId, paymentData);
          const onPaymentDataChanged = (paymentData) => this.checkoutComGooglePayService.onPaymentDataChanged(cartId, userId, paymentData);

          paymentClientData.clientSettings.paymentDataCallbacks = {
            onPaymentAuthorized,
            onPaymentDataChanged,
          };
        }

        // @ts-ignore
        if (this.windowRef.nativeWindow.google?.payments?.api?.PaymentsClient) {
          this.initPaymentsClient(paymentClientData, userId, cartId);
        } else {
          loadScript(this.windowRef, 'https://pay.google.com/gp/p/js/pay.js', () => {
            this.ngZone.run(() => {
              this.initPaymentsClient(paymentClientData, userId, cartId);
            });
          });
        }
      }, err => console.error('initBtn with errors', {err}));
  }

  protected initPaymentsClient(merchantConfiguration, userId: string, cartId: string) {
    // @ts-ignore
    this.paymentsClient = new google.payments.api.PaymentsClient(merchantConfiguration.clientSettings);
    const isReadyToPayRequest: object = this.checkoutComGooglePayService
      .createInitialPaymentRequest(merchantConfiguration, this.shippingAddressRequired);
    this.paymentsClient.isReadyToPay(isReadyToPayRequest)
      .then(({result}) => {
        if (result) {
          const button = this.paymentsClient.createButton({
            onClick: () => {
              this.ngZone.run(() => {
                this.authorisePayment(userId, cartId);
              });
            }
          });
          if (this.gpayBtn?.nativeElement.children.length === 0) {
            this.renderer.setAttribute(this.gpayBtn?.nativeElement, 'id', 'google-pay-button');
            this.renderer.appendChild(this.gpayBtn?.nativeElement, button)
          } else {
            button.remove();
          }
        }
      })
      .catch(err => {
        console.error('failed to initialize googlepay', err);
      });
  }

  ngOnDestroy(): void {
    this.drop.next();
  }

  protected authorisePayment(userId: string, cartId: string) {
    const configReq = this.checkoutComGooglePayService.getMerchantConfigurationFromState().pipe(first(c => !!c));
    let req;
    if (this.sameAsShippingAddress$.value) {
      req = configReq;
    } else {
      if (!this.billingAddressForm.valid) {
        makeFormErrorsVisible(this.billingAddressForm);
        return;
      }
      req = this.checkoutComPaymentService.updatePaymentAddress(this.billingAddressForm.value)
        .pipe(switchMap(() => configReq));
    }
    req.pipe(takeUntil(this.drop)).subscribe((merchantConfiguration) => {
      const paymentDataRequest: any =
        this.checkoutComGooglePayService.createFullPaymentRequest(merchantConfiguration);

      this.paymentsClient.loadPaymentData(paymentDataRequest)
        .then((paymentRequest) => {
          this.checkoutComGooglePayService.authoriseOrder(paymentRequest, false, userId, cartId);
        })
        .catch(function (err) {
          console.log("Error google pay payment...");
          console.error(err);
        });
    }, err => console.error('authorisePayment with errors', {err}));
  }
}
