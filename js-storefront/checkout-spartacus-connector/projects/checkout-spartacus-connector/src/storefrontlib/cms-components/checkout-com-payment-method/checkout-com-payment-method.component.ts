/* Angular */
import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
/* Rxjs */
import { BehaviorSubject, EMPTY, Observable, Subject } from 'rxjs';
import { finalize, skip, switchMap, take, takeUntil } from 'rxjs/operators';
/* Spartacus */
import {
  ActiveCartService,
  Address,
  GlobalMessageService,
  PaymentDetails,
  TranslationService,
  UserIdService,
  UserPaymentService
} from '@spartacus/core';
import { CheckoutStepService, PaymentMethodComponent } from '@spartacus/checkout/components';
/* CheckoutCom */
import { CheckoutComPaymentService } from '../../../core/services/checkout-com-payment.service';
import { ApmData, PaymentType } from '../../../core/model/ApmData';
import { ApmPaymentDetails, CheckoutComPaymentDetails } from '../../interfaces';
import { CheckoutComApmService } from '../../../core/services/checkout-com-apm.service';
import { CheckoutDeliveryFacade, CheckoutFacade, CheckoutPaymentFacade } from '@spartacus/checkout/root';

@Component({
  selector: 'lib-checkout-com-payment-method',
  templateUrl: './checkout-com-payment-method.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CheckoutComPaymentMethodComponent extends PaymentMethodComponent implements OnInit, OnDestroy {
  public requiresCvn = false;
  public processing$ = new BehaviorSubject<boolean>(false);
  public cvnForm: FormGroup = new FormGroup({
    cvn: new FormControl('', Validators.required)
  });
  public selectedPaymentDetails: PaymentDetails;
  public selectedApm$: Observable<ApmData> = this.checkoutComApmService.getSelectedApmFromState();
  public isCardPayment = false;

  protected activeCartId: string = null;
  protected userId: string = null;
  protected shouldRedirect: boolean;
  protected deliveryAddress: Address;

  private drop = new Subject<void>();

  constructor(
    protected activeCartService: ActiveCartService,
    protected checkoutService: CheckoutFacade,
    protected activatedRoute: ActivatedRoute,
    protected translation: TranslationService,
    protected checkoutStepService: CheckoutStepService,
    protected userPaymentService: UserPaymentService,
    protected checkoutDeliveryService: CheckoutDeliveryFacade,
    protected checkoutPaymentService: CheckoutPaymentFacade,
    protected globalMessageService: GlobalMessageService,
    protected checkoutComPaymentService: CheckoutComPaymentService,
    protected userIdService: UserIdService,
    protected checkoutComApmService: CheckoutComApmService
  ) {
    super(
      userPaymentService,
      checkoutService,
      checkoutDeliveryService,
      checkoutPaymentService,
      globalMessageService,
      activatedRoute,
      translation,
      activeCartService,
      checkoutStepService
    );

  }

  ngOnInit(): void {
    super.ngOnInit();

    this.activeCartService.getActiveCartId().pipe(take(1), takeUntil(this.drop)).subscribe((cartId) => {
      this.activeCartId = cartId;
    });

    this.userIdService.getUserId().pipe(take(1), takeUntil(this.drop)).subscribe((userId) => {
      this.userId = userId;
    });

    this.selectedApm$.pipe(takeUntil(this.drop)).subscribe((apm: ApmData) => {
      this.isCardPayment = apm.code === PaymentType.Card;
    });

    this.checkoutDeliveryService.getDeliveryAddress()
      .pipe(
        take(1),
        takeUntil(this.drop)
      ).subscribe(deliveryAddress => {
        this.deliveryAddress = deliveryAddress;
      });
  }

  selectPaymentMethod(paymentDetails: PaymentDetails): void {
    // call the ootb payment details API for saved cards only!
    super.selectPaymentMethod(paymentDetails);

    this.selectedPaymentDetails = paymentDetails;
  }

  next(): void {
    if (this.requiresCvn && this.selectedPaymentDetails) {
      // TODO cvv is not always required. we need a config from the BE
      if (this.cvnForm.invalid) {
        this.cvnForm.markAllAsTouched();
        return;
      } else {
        super.selectPaymentMethod({
          ...this.selectedPaymentDetails,
          cvn: this.cvnForm.value.cvn
        });
      }
    }

    super.next();
  }

  setPaymentDetails({
                      paymentDetails,
                      billingAddress,
                    }: {
    paymentDetails: CheckoutComPaymentDetails;
    billingAddress?: Address;
  }): void {
    const details = {...paymentDetails} as CheckoutComPaymentDetails;
    if (billingAddress == null) {
      billingAddress = this.deliveryAddress;
    }
    details.billingAddress = billingAddress;

    this.processing$.next(true);
    this.checkoutComPaymentService.updatePaymentAddress(billingAddress).pipe(
      switchMap(() => this.checkoutComPaymentService.createPaymentDetails(details, this.userId, this.activeCartId)),
      finalize(() => this.processing$.next(false)),
      takeUntil(this.drop)
    ).subscribe((newPaymentDetails) => {
      // this.checkoutPaymentService.setPaymentDetails(newPaymentDetails);
      this.shouldRedirect = true;
    }, (err) => {
      // TODO :: Display an error message!
      console.error('getPaymentDetailsFromState', err);
    });
  }

  setApmPaymentDetails({
                         paymentDetails,
                         billingAddress,
                       }: {
    paymentDetails: ApmPaymentDetails;
    billingAddress?: Address;
  }) {
    if (billingAddress == null) {
      billingAddress = this.deliveryAddress;
    }
    const details = {...paymentDetails, billingAddress};
    this.processing$.next(true);

    const isApm = paymentDetails.type !== PaymentType.ApplePay
      && paymentDetails.type !== PaymentType.GooglePay
      && paymentDetails.type !== PaymentType.Card;

    if (isApm && billingAddress.country.isocode !== this.deliveryAddress.country.isocode) {
      // if the country has changed, we have to request the available APM's for the given billing
      // country and then validate if the APM is also available in this new dataset.
      // if it is available, we persist the payment details and move to next step in checkout
      // otherwise we show an error
      this.checkoutComPaymentService.updatePaymentAddress(billingAddress).pipe(
        switchMap(() => this.checkoutComApmService.requestAvailableApms().pipe(
          skip(1), // first response is cached
          take(1), // we only want 1 new APM list
          switchMap((apms) => {
            const apmExistsInNewCountryContext = !!apms.find(({code}) => code === paymentDetails.type);
            this.shouldRedirect = apmExistsInNewCountryContext;
            if (apmExistsInNewCountryContext) {
              return this.checkoutComPaymentService
                .createApmPaymentDetails(details, this.activeCartId, this.userId)
                .pipe(finalize(() => this.processing$.next(false)));
            } else {
              this.processing$.next(false);
              return EMPTY;
            }
          })
        )),
        takeUntil(this.drop),
      ).subscribe();
    } else {
      // We know the APM is valid because the billing address is in same country as delivery country,
      // or is the same address
      this.checkoutComPaymentService.updatePaymentAddress(billingAddress).pipe(
        switchMap(() => this.checkoutComPaymentService.createApmPaymentDetails(details, this.activeCartId, this.userId)),
        finalize(() => this.processing$.next(false)),
        takeUntil(this.drop)
      ).subscribe((newPaymentDetails) => {
        this.shouldRedirect = true;
      });
    }
  }

  ngOnDestroy() {
    this.drop.next();
    super.ngOnDestroy();
  }

}
