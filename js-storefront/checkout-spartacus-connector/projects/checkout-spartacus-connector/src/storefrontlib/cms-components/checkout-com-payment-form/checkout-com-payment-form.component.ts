/* Angular */
import { ChangeDetectionStrategy, Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { FormBuilder } from '@angular/forms';
/* Rxjs */
import { BehaviorSubject, combineLatest, Observable, Subject } from 'rxjs';
import { CheckoutComPaymentDetails } from '../../interfaces';
import { debounceTime, distinctUntilChanged, filter, map, take, takeUntil } from 'rxjs/operators';
/* Spartacus */
import {
  Address,
  CardType,
  GlobalMessageService,
  GlobalMessageType,
  TranslationService,
  UserAddressService,
  UserIdService,
  UserPaymentService
} from '@spartacus/core';
import { ModalService } from '@spartacus/storefront';
import { PaymentFormComponent } from '@spartacus/checkout/components';
/* CheckoutCom */
import { CheckoutComPaymentService } from '../../../core/services/checkout-com-payment.service';
import {
  FrameCardTokenizationFailedEvent,
  FrameCardTokenizedEvent,
  FramePaymentMethodChangedEvent, FramesCardholder,
  FramesLocalization
} from '../checkout-com-frames-form/interfaces';
import { makeFormErrorsVisible } from '../../../core/shared/make-form-errors-visible';
import { CheckoutDeliveryFacade, CheckoutPaymentFacade } from '@spartacus/checkout/root';

@Component({
  selector: 'lib-checkout-com-payment-form',
  templateUrl: './checkout-com-payment-form.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CheckoutComPaymentFormComponent extends PaymentFormComponent implements OnInit, OnDestroy {
  @Input() processing = false;
  @Output() setPaymentDetails = new EventEmitter<{
    paymentDetails: CheckoutComPaymentDetails,
    billingAddress: Address
  }>();
  public submitEvent$ = new Subject<void>();
  public submitting$ = new BehaviorSubject<boolean>(false);
  public canSaveCard$ = new BehaviorSubject<boolean>(false);
  public paymentForm = this.fb.group({
    defaultPayment: [false],
    save: [false],
    accountHolderName: ['', false],
  });
  private drop = new Subject<void>();
  private spartacusCardTypes: CardType[] = [];
  private framesPaymentMethod: string = null;
  framesLocalization$: Observable<FramesLocalization>;
  framesCardholder$ = new EventEmitter<FramesCardholder>();

  constructor(
    protected checkoutPaymentService: CheckoutPaymentFacade,
    protected checkoutDeliveryService: CheckoutDeliveryFacade,
    protected userPaymentService: UserPaymentService,
    protected globalMessageService: GlobalMessageService,
    protected fb: FormBuilder,
    protected modalService: ModalService,
    protected userAddressService: UserAddressService,
    protected checkoutComPaymentService: CheckoutComPaymentService,
    protected userIdService: UserIdService,
    protected translationService: TranslationService,
  ) {
    super(checkoutPaymentService,
      checkoutDeliveryService,
      userPaymentService,
      globalMessageService,
      fb,
      modalService,
      userAddressService);
  }

  ngOnInit(): void {
    super.ngOnInit();
    this.checkoutPaymentService.loadSupportedCardTypes();
    this.checkoutPaymentService.getCardTypes().pipe(
      filter(types => types != null && types?.length > 0),
      takeUntil(this.drop)
    ).subscribe((cardTypes) => this.spartacusCardTypes = cardTypes);

    this.userIdService.getUserId().pipe(takeUntil(this.drop)).subscribe((userId) => {
      this.canSaveCard$.next(
        this.checkoutComPaymentService.canSaveCard(userId)
      );
    });

    this.framesLocalization$ = this.getFramesLocalization();

    this.paymentForm.valueChanges
      .pipe(
        debounceTime(500),
        distinctUntilChanged(),
        takeUntil(this.drop),
      ).subscribe((changes) => {
      const {accountHolderName} = changes;

      this.framesCardholder$.emit({
        name: accountHolderName
      });
    })
  }

  next() {
    let everyFormIsValid = true;
    if (!this.sameAsShippingAddress && !this.billingAddressForm.valid) {
      everyFormIsValid = false;
      makeFormErrorsVisible(this.billingAddressForm);
    }

    if (!this.paymentForm.valid) {
      everyFormIsValid = false;
      makeFormErrorsVisible(this.paymentForm);
    }

    if (everyFormIsValid) {
      this.submitting$.next(true);
      this.submitEvent$.next();
    }
  }

  tokenized(event: FrameCardTokenizedEvent) {
    const userInput = this.paymentForm.value;
    let billingAddress = null;
    if (!this.sameAsShippingAddress && this.billingAddressForm.valid) {
      billingAddress = this.billingAddressForm.value;
    }

    const details: CheckoutComPaymentDetails = {
      addressLine1: billingAddress?.line1,
      addressLine2: billingAddress?.line2,
      city: billingAddress?.town,
      country: billingAddress?.country,
      postalCode: billingAddress?.postalCode,
      billingAddress,
      cardNumber: event.bin + '******' + event.last4,
      cardType: this.getCardTypeFromTokenizedEvent(event.scheme ?? this.framesPaymentMethod),
      defaultPayment: userInput.defaultPayment,
      expiryMonth: event.expiry_month,
      expiryYear: event.expiry_year,
      paymentToken: event.token,
      type: event.type.toUpperCase(),
      cardBin: event.bin,
      saved: userInput.save,
      accountHolderName: userInput.accountHolderName,
    };

    this.submitting$.next(false);

    this.setPaymentDetails.emit({
      paymentDetails: details,
      billingAddress,
    });
  }

  tokenizationFailed(event: FrameCardTokenizationFailedEvent) {
    this.submitting$.next(false);
    console.error('tokenization failed', event);
    this.globalMessageService.add({key: 'paymentForm.frames.tokenizationFailed'}, GlobalMessageType.MSG_TYPE_ERROR);
  }

  ngOnDestroy() {
    this.drop.next();
    this.submitting$.next(false);
    this.processing = false;
  }

  framesPaymentMethodChanged(event: FramePaymentMethodChangedEvent) {
    if (event?.paymentMethod) {
      this.framesPaymentMethod = event.paymentMethod;
    }
  }

  getCardTypeFromTokenizedEvent(scheme: string): CardType {
    const empty: CardType = {code: 'undefined'};
    if (!scheme || typeof scheme !== 'string') {
      return empty;
    }
    try {
      if (this.spartacusCardTypes?.length) {
        for (const spartacusCardType of this.spartacusCardTypes) {
          if (!spartacusCardType.name) {
            continue;
          }
          const spartacusName = spartacusCardType.name.replace(/[^0-9a-z]/gi, '').toLowerCase();
          const framesName = scheme.replace(/[^0-9a-z]/gi, '').toLowerCase();
          if (spartacusName === framesName) {
            return spartacusCardType;
          }
        }
      }
      // "Last Resort"
      return {
        code: scheme.toLowerCase(),
        name: scheme
      };
    } catch (e) {
      console.error('getCardTypeFromTokenizedEvent', e, 'scheme:', scheme);
    }
    return empty;
  }

  protected getFramesLocalization() {
    return combineLatest([
      this.translationService.translate('paymentForm.frames.placeholders.cardNumberPlaceholder'),
      this.translationService.translate('paymentForm.frames.placeholders.expiryMonthPlaceholder'),
      this.translationService.translate('paymentForm.frames.placeholders.expiryYearPlaceholder'),
      this.translationService.translate('paymentForm.frames.placeholders.cvvPlaceholder'),
    ]).pipe(
      take(1),
      map(([cardNumberPlaceholder, expiryMonthPlaceholder, expiryYearPlaceholder, cvvPlaceholder]) => {
        return {
          cardNumberPlaceholder,
          expiryMonthPlaceholder,
          expiryYearPlaceholder,
          cvvPlaceholder
        } as FramesLocalization;
      })
    );
  }
}
