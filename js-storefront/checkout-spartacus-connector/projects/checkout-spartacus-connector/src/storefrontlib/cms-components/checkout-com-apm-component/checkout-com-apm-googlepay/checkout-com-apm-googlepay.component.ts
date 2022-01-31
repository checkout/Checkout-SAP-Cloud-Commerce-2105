import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  Input,
  NgZone,
  OnDestroy,
  OnInit,
  ViewChild
} from '@angular/core';
import { BehaviorSubject, Subject } from 'rxjs';
import {
  ActiveCartService,
  GlobalMessageService,
  GlobalMessageType,
  RoutingService,
  UserIdService,
  WindowRef
} from '@spartacus/core';
import { CheckoutComGooglepayService } from '../../../../core/services/googlepay/checkout-com-googlepay.service';
import { filter, first, switchMap, take, takeUntil } from 'rxjs/operators';
import { CheckoutComPaymentService } from '../../../../core/services/checkout-com-payment.service';
import { FormGroup } from '@angular/forms';
import { makeFormErrorsVisible } from '../../../../core/shared/make-form-errors-visible';
import { loadScript } from '../../../../core/shared/loadScript';
import { getUserIdCartId } from '../../../../core/shared/get-user-cart-id';
import { CheckoutFacade } from '@spartacus/checkout/root';

@Component({
  selector: 'lib-checkout-com-apm-googlepay',
  templateUrl: './checkout-com-apm-googlepay.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CheckoutComApmGooglepayComponent implements OnInit, AfterViewInit, OnDestroy {
  @Input() billingAddressForm: FormGroup = new FormGroup({});
  @ViewChild('gpayBtn') gpayBtn: ElementRef = null;
  public sameAsShippingAddress$ = new BehaviorSubject<boolean>(true);
  private paymentsClient: any;
  private drop = new Subject();

  constructor(
    protected checkoutComGooglePayService: CheckoutComGooglepayService,
    protected globalMessageService: GlobalMessageService,
    protected checkoutFacade: CheckoutFacade,
    protected routingService: RoutingService,
    protected checkoutComPaymentService: CheckoutComPaymentService,
    protected userIdService: UserIdService,
    protected activeCartService: ActiveCartService,
    private ngZone: NgZone,
    protected windowRef: WindowRef,
  ) {
  }

  ngOnInit(): void {
    this.checkoutFacade.getOrderDetails().pipe(
      filter(order => Object.keys(order).length !== 0), takeUntil(this.drop)
    ).subscribe(() => {
      this.routingService.go({cxRoute: 'orderConfirmation'});
    });
  }

  ngAfterViewInit() {
    if (this.gpayBtn?.nativeElement) {
      getUserIdCartId(this.userIdService, this.activeCartService)
      .pipe(takeUntil(this.drop))
      .subscribe(({userId, cartId}) => {
        this.initBtn(userId, cartId);
      });
    }
  }

  private initBtn(userId: string, cartId: string) {
    this.checkoutComGooglePayService.getMerchantConfigurationFromState()
        .pipe(take(1), takeUntil(this.drop))
        .subscribe((merchantConfiguration) => {
          if (this.paymentsClient) {
            return;
          }
          // @ts-ignore
          if (this.windowRef.nativeWindow.google?.payments?.api?.PaymentsClient) {
            this.initPaymentsClient(merchantConfiguration, userId, cartId);
          } else {
            loadScript(this.windowRef, 'https://pay.google.com/gp/p/js/pay.js', () => {
              this.ngZone.run(() => {
                this.initPaymentsClient(merchantConfiguration, userId, cartId);
              });
            });
          }
        });
  }

  private initPaymentsClient(merchantConfiguration, userId: string, cartId: string) {
    // @ts-ignore
    this.paymentsClient = new google.payments.api.PaymentsClient(merchantConfiguration.clientSettings);
    const isReadyToPayRequest: object = this.checkoutComGooglePayService.createInitialPaymentRequest(merchantConfiguration);
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
            this.gpayBtn?.nativeElement.appendChild(button);
          }
        })
        .catch(err => {
          console.error('failed to initialize googlepay', err);
        });
  }

  ngOnDestroy(): void {
    this.drop.next();
  }

  private authorisePayment(userId: string, cartId: string) {
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
      const paymentDataRequest: any = this.checkoutComGooglePayService.createFullPaymentRequest(merchantConfiguration);

      this.paymentsClient.loadPaymentData(paymentDataRequest)
          .then((paymentRequest) => {
            this.checkoutComGooglePayService.authoriseOrder(paymentRequest, false, userId, cartId);
          })
          .catch(() => {
            this.globalMessageService.add({key: 'paymentForm.googlepay.authorisationFailed'}, GlobalMessageType.MSG_TYPE_ERROR);
          });
    });
  }
}
