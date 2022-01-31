import { ChangeDetectionStrategy, Component, EventEmitter, Input, OnInit, Output, OnDestroy, } from '@angular/core';
import { BehaviorSubject, combineLatest, Observable, Subject } from 'rxjs';
import { CheckoutComApmService } from '../../../core/services/checkout-com-apm.service';
import { ApmData, PaymentType } from '../../../core/model/ApmData';
import {
  Address,
  UserIdService,
  ActiveCartService,
  GlobalMessageService,
  GlobalMessageType,
  CurrencyService,
  WindowRef
} from '@spartacus/core';
import { FormGroup } from '@angular/forms';
import { filter, takeUntil, take, switchMap, first, map, finalize } from 'rxjs/operators';
import { ApmPaymentDetails } from '../../interfaces';
import { CheckoutComGooglepayService } from '../../../core/services/googlepay/checkout-com-googlepay.service';
import { createApplePaySession } from '../../../core/services/applepay/applepay-session';
import { CheckoutComApplepayService } from '../../../core/services/applepay/checkout-com-applepay.service';
import { makeFormErrorsVisible } from '../../../core/shared/make-form-errors-visible';
import { getUserIdCartId } from '../../../core/shared/get-user-cart-id';

@Component({
  selector: 'lib-checkout-com-apm',
  templateUrl: './checkout-com-apm.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CheckoutComApmComponent implements OnInit, OnDestroy {
  @Output() setPaymentDetails = new EventEmitter<{ paymentDetails: ApmPaymentDetails, billingAddress: Address }>();
  @Input() goBack: () => void;
  @Input() processing = false;

  public submitting$ = new BehaviorSubject<boolean>(false);
  public selectedApm$: Observable<ApmData> = this.checkoutComApmService.getSelectedApmFromState();
  public paymentType = PaymentType;
  public availableApms$: Observable<ApmData[]> = this.checkoutComApmService.getAvailableApmsFromState();
  public card$: Observable<ApmData> = this.checkoutComApmService.getApmByComponent('cardComponent', PaymentType.Card);
  public applePay$: Observable<ApmData>;
  public googlePay$: Observable<ApmData>;
  public loading$: Observable<boolean> = this.checkoutComApmService.getIsApmLoadingFromState();
  public sameAsShippingAddress$ = new BehaviorSubject<boolean>(true);
  public billingAddressForm: FormGroup = new FormGroup({});

  protected paymentDetails: ApmData;
  private drop = new Subject<void>();

  constructor(protected checkoutComApmService: CheckoutComApmService,
              protected checkoutComGooglePayService: CheckoutComGooglepayService,
              protected currencyService: CurrencyService,
              protected checkoutComApplepayService: CheckoutComApplepayService,
              protected userIdService: UserIdService,
              protected activeCartService: ActiveCartService,
              protected globalMessageService: GlobalMessageService,
              protected windowRef: WindowRef,
  ) {
  }

  ngOnInit(): void {
    this.selectedApm$.pipe(takeUntil(this.drop)).subscribe((apm) => {
      if (!apm) {
        this.checkoutComApmService.selectApm({
          code: PaymentType.Card
        });
      } else {
        this.paymentDetails = apm;
      }
    });

    this.getActiveApms();
    this.listenForCurrencyChange();
    this.listenToAvailableApmsAndProtectSelectedApm();
    this.initializeGooglePay();
    this.initializeApplePay();
  }

  selectApmPaymentDetails() {
    let billingAddress = null;
    if (!this.sameAsShippingAddress$.value) {
      if (this.billingAddressForm.valid) {
        billingAddress = this.billingAddressForm.value;
      } else {
        makeFormErrorsVisible(this.billingAddressForm);
        return;
      }
    }

    this.submitting$.next(true);

    this.setPaymentDetails.emit({
      paymentDetails: {type: this.paymentDetails.code} as ApmPaymentDetails,
      billingAddress
    });
  }

  showBillingFormAndContinueButton(code: PaymentType) {
    switch (code) {
      case PaymentType.Card:
      case PaymentType.Klarna:
      case PaymentType.GooglePay:
      case PaymentType.ApplePay:
      case PaymentType.Sepa:
      case PaymentType.Oxxo:
      case PaymentType.Fawry:
      case PaymentType.iDeal:
        return false;

      default:
        return true;
    }
  }

  ngOnDestroy() {
    this.drop.next();
  }

  /**
   * Request the active APM's.
   * The list should be updated when the currency changes.
   */
  protected getActiveApms() {
    this.checkoutComApmService.requestAvailableApms()
        .pipe(takeUntil(this.drop))
        .subscribe();
  }

  protected listenForCurrencyChange() {
    this.currencyService.getActive().pipe(
      switchMap(() => this.checkoutComApmService.requestAvailableApms()),
      takeUntil(this.drop)).subscribe();
  }

  /**
   * Prevent selected APM not be in the list of available APM's.
   * Will rollback to Card if current selected APM is not available in new context
   */
  protected listenToAvailableApmsAndProtectSelectedApm() {
    combineLatest([
      this.checkoutComApmService.getAvailableApmsFromState(),
      this.checkoutComApmService.getSelectedApmFromState()
    ])
    .pipe(
      filter(([apms, selectedApm]) =>
        !!apms && apms.length > 0 &&
        !!selectedApm && (
          selectedApm.code !== PaymentType.Card
          && selectedApm.code !== PaymentType.ApplePay
          && selectedApm.code !== PaymentType.GooglePay
        )),
      takeUntil(this.drop),
      finalize(() => this.submitting$.next(false)),
    )
    .subscribe(([apms, selectedApm]) => {
      const apm = apms.find(({code}) => code === selectedApm.code);

      this.submitting$.next(false);
      this.processing = false;

      if (!apm) {
        this.globalMessageService.add({key: 'paymentForm.apmChanged'}, GlobalMessageType.MSG_TYPE_ERROR);

        this.checkoutComApmService.selectApm({
          code: PaymentType.Card
        });
      }
    });
  }

  protected initializeGooglePay() {
    this.googlePay$ = this.checkoutComApmService.getApmByComponent('googlePayComponent', PaymentType.GooglePay).pipe(
      take(1),
      switchMap((apmData) => {
        return getUserIdCartId(this.userIdService, this.activeCartService).pipe(
          switchMap(({userId, cartId}) => {
            this.checkoutComGooglePayService.requestMerchantConfiguration(userId, cartId);
            return this.checkoutComGooglePayService.getMerchantConfigurationFromState().pipe(
              first(c => !!c), map(_ => apmData)
            );
          }));
      })
    );
  }

  /**
   * If ApplePay is available, we request the payment request and wait for response.
   * When the response arrives, we show the ApplePay button
   */
  private initializeApplePay(): void {
    const ApplePaySession = createApplePaySession(this.windowRef);
    if (ApplePaySession && ApplePaySession.canMakePayments()) {
      getUserIdCartId(this.userIdService, this.activeCartService)
      .pipe(takeUntil(this.drop)).subscribe(({userId, cartId}) => {
        this.checkoutComApplepayService.requestApplePayPaymentRequest(userId, cartId);
        this.applePay$ = combineLatest([
          this.checkoutComApmService.getApmByComponent('applePayComponent', PaymentType.ApplePay),
          this.checkoutComApplepayService.getPaymentRequestFromState()
        ]).pipe(
          filter(([component, paymentRequest]) => {
            return !!component && !!paymentRequest && Object.keys(paymentRequest).length > 0;
          }),
          map(([apmData, _]) => apmData)
        );
      });
    }
  }
}
