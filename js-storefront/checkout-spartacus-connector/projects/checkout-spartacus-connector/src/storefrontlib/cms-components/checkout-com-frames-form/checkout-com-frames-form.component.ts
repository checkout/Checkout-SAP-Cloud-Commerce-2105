import { Component, OnInit, ChangeDetectionStrategy, AfterViewInit, Input, Output, OnDestroy, NgZone, EventEmitter } from '@angular/core';
import { FormGroup, ValidatorFn } from '@angular/forms';
import {
  FramesConfig,
  FramesLocalization,
  FrameElementIdentifier,
  FrameElement,
  FrameValidationChangedEvent,
  FramePaymentMethodChangedEvent,
  FrameCardValidationChangedEvent,
  FrameCardTokenizedEvent,
  FrameCardTokenizationFailedEvent,
  FramesStyle,
  FramesCardholder
} from './interfaces';
import { Subject, BehaviorSubject, Observable } from 'rxjs';
import { takeUntil, switchMap, skipWhile, first, filter } from 'rxjs/operators';
import { CheckoutComPaymentService } from '../../../core/services/checkout-com-payment.service';
import { UserIdService, WindowRef } from '@spartacus/core';

@Component({
  selector: 'lib-checkout-com-frames-form',
  templateUrl: './checkout-com-frames-form.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CheckoutComFramesFormComponent implements OnInit, AfterViewInit, OnDestroy {
  @Input() submitEvent: Observable<any> = null; // required
  @Input() form = new FormGroup({});
  @Input() cardholderStream: Observable<FramesCardholder> = null;
  @Input() localization: FramesLocalization = null;
  @Input() cardNumberInputName = 'cardNumber';
  @Input() expiryDateInputName = 'expiryDate';
  @Input() cvvInputName = 'cvn';

  @Output() tokenized = new EventEmitter<FrameCardTokenizedEvent>();
  @Output() tokenizationFailed = new EventEmitter<FrameCardTokenizationFailedEvent>();
  @Output() paymentMethodChange = new EventEmitter<FramePaymentMethodChangedEvent>();

  public InputType = FrameElementIdentifier;
  protected paymentMethod: string;
  public paymentIconCssClass$ = new BehaviorSubject<string>('');

  private framesReady = new BehaviorSubject<boolean>(false);
  private frameActivated = new Subject<FrameElement>();
  private frameFocus = new Subject<FrameElement>();
  private frameBlur = new Subject<FrameElement>();
  private frameValidationChanged = new Subject<FrameValidationChangedEvent>();
  private paymentMethodChanged = new Subject<FramePaymentMethodChangedEvent>();
  private cardValidationChanged = new Subject<FrameCardValidationChangedEvent>();
  private cardSubmitted = new Subject<void>();
  private cardTokenized = new Subject<FrameCardTokenizedEvent>();
  private cardTokenizationFailed = new Subject<FrameCardTokenizationFailedEvent>();

  private validationStatusMap = new Map<FrameElementIdentifier, boolean>();
  private config: FramesConfig = null;
  private framesInitialized = false;
  private drop = new Subject<void>();
  private formSubmitted = false;
  private isDestroyed = false;

  constructor(protected ngZone: NgZone,
              protected checkoutComPaymentService: CheckoutComPaymentService,
              protected userIdService: UserIdService,
              protected windowRef: WindowRef
  ) { }

  ngOnInit() {
    this.listenForMerchantKey();

    if (this.windowRef.isBrowser()){
      this.listenForFramesEvents();
      this.listenForSubmitEvent();
      this.listenForCardHolder();
    }
  }

  private listenForCardHolder() {
    if (this.cardholderStream) {
      this.cardholderStream.pipe(
        filter(Boolean),
        takeUntil(this.drop)
      ).subscribe((cardholder) => {
        this.modifyFramesCardholder(cardholder);
      }, err => console.error('listenForCardHolder with errors', {err}));
    }
  }

  private listenForMerchantKey() {
    this.userIdService.getUserId().pipe(
      first(id => !!id),
      switchMap((userId) => {
        this.checkoutComPaymentService.requestOccMerchantKey(userId);
        return this.checkoutComPaymentService.getOccMerchantKeyFromState().pipe(
          first(k => !!k),
          switchMap((firstPublicKey) => {
            const initialConfig: FramesConfig = {publicKey: firstPublicKey, cardTokenized: null};
            this.setConfig(initialConfig);
            return this.checkoutComPaymentService.getOccMerchantKeyFromState().pipe(
              skipWhile(pk => pk == null || pk === firstPublicKey)
            );
          }));
      }),
      takeUntil(this.drop)
    ).subscribe((publicKey) => {this.modifyFramesPublicKey(publicKey)},
      err => console.error('listenForMerchantKey with errors', {err}));
  }

  ngAfterViewInit() {
    this.tryInitFrames();
  }

  private getValidator(fieldType: FrameElementIdentifier): ValidatorFn {
    return () => {
      const isValid = this.validationStatusMap.get(fieldType);
      const errObj = {};
      let errKey;
      switch (fieldType) {
        case FrameElementIdentifier.CardNumber:
          errKey = 'cardNumberInvalid';
          break;
        case FrameElementIdentifier.ExpiryDate:
          errKey = 'expiryDateInvalid';
          break;
        case FrameElementIdentifier.Cvv:
          errKey = 'cvvInvalid';
          break;
        default:
          return null;
      }
      errObj[errKey] = true;
      if (typeof isValid !== 'boolean') {
        return errObj;
      }
      if (isValid) {
        return null;
      }
      return errObj;
    };
  }

  private setConfig(initialConfig: FramesConfig) {
    const config = initialConfig;
    config.style = this.getStyleConfig();
    config.ready = () => this.framesReady.next(true);
    config.cardTokenized = (e) => this.cardTokenized.next(e);
    config.frameActivated = (e) => this.frameActivated.next(e);
    config.frameFocus = (e) => this.frameFocus.next(e);
    config.frameBlur = (e) => this.frameBlur.next(e);
    config.frameValidationChanged = (e) => this.frameValidationChanged.next(e);
    config.paymentMethodChanged = (e) => this.paymentMethodChanged.next(e);
    config.cardValidationChanged = (e) => this.cardValidationChanged.next(e);
    config.cardSubmitted = () => this.cardSubmitted.next();
    config.cardTokenizationFailed = (e) => this.cardTokenizationFailed.next(e);

    if (this.localization) {
      config.localization = this.localization;
    }
    this.config = config;
  }

  private tryInitFrames() {
    if (!this.initFrames(this.config)) {
      setTimeout(() => {
        if (!this.isDestroyed) {
          this.tryInitFrames();
        }
      }, 200);
    }
  }

  private initFrames(config): boolean {
    this.framesReady.next(false);
    if (!config) {
      return false;
    }
    if (this.isDestroyed) {
      return true;
    }

    const Frames: any = this.windowRef.nativeWindow['Frames']; // tslint:disable-line
    if (!Frames) {
      return false;
    }
    try {
      Frames.init(config);
      this.framesInitialized = true;
    } catch (e) {
      return false;
    }
    return true;
  }

  private modifyFramesPublicKey(publicKey: string): boolean {
    if (publicKey == null) {
      return;
    }
    const Frames: any = this.windowRef.nativeWindow['Frames']; // tslint:disable-line
    if (!Frames) {
      return false;
    }
    try {
      Frames.publicKey = publicKey;
      if (this.config) {
        this.config.publicKey = publicKey;
      }
    } catch (e) {
      return false;
    }
    return true;
  }

  private listenForFramesEvents() {
    this.frameValidationChanged.pipe(takeUntil(this.drop)).subscribe((event) => {
      this.validationStatusMap.set(event.element, event.isValid && !event.isEmpty);
      const ctrl = this.getElementByIdentifier(event.element);
      if (ctrl) {
        this.ngZone.run(() => {
          ctrl.markAsTouched();
          ctrl.markAsDirty();
          ctrl.updateValueAndValidity();

          if (event.isValid && event.element === FrameElementIdentifier.CardNumber) {
            this.paymentIconCssClass$.next(this.paymentMethod);
          } else if (event.isEmpty) {
            this.paymentIconCssClass$.next('');
            this.paymentMethod = '';
          }
        });
      }
    }, err => console.error('listenForFramesEvents with errors', {err}));

    this.frameActivated.pipe(takeUntil(this.drop)).subscribe((event) => {
      const ctrl = this.getElementByIdentifier(event.element);
      if (ctrl) {
        const validator = this.getValidator(event.element);
        this.ngZone.run(() => {
          ctrl.setValidators(validator);
          ctrl.updateValueAndValidity();
        });
      }
    }, err => console.error('frameActivated with errors', {err}));

    this.paymentMethodChanged.pipe(takeUntil(this.drop)).subscribe((event) => {
      this.ngZone.run(() => {
        this.paymentMethodChange.next(event);

        if (event.paymentMethod) {
          this.paymentMethod = event.paymentMethod
            .replace(/\s+/g, '-') // replace space with a dash
            .toLowerCase();
        } else {
          this.paymentMethod = ''; // reset the payment method
        }
      });
    }, err => console.error('paymentMethodChanged with errors', {err}));
  }

  private getElementByIdentifier(identifier: FrameElementIdentifier) {
    const controlName = this.getInputNameByIdentifier(identifier);
    if (!controlName) {
      return;
    }
    return this.form.controls[controlName];
  }

  private getInputNameByIdentifier(identifier: FrameElementIdentifier): string {
    switch (identifier) {
      case FrameElementIdentifier.CardNumber:
        return this.cardNumberInputName;
      case FrameElementIdentifier.ExpiryDate:
        return this.expiryDateInputName;
      case FrameElementIdentifier.Cvv:
        return this.cvvInputName;
    }
  }

  private listenForSubmitEvent() {
    this.cardTokenized.pipe(takeUntil(this.drop)).subscribe((e) => {
      this.ngZone.run(() => {
        this.tokenized.emit(e);
      });
    }, err => console.error('pipe cardTokenized with errors', {err}));
    this.cardTokenizationFailed.pipe(takeUntil(this.drop)).subscribe((e) => {
      this.ngZone.run(() => {
        this.tokenizationFailed.emit(e);
      });
    }, err => console.error('pipe cardTokenizedFailed with errors', {err}));
    if (this.submitEvent) {
      this.submitEvent.pipe(takeUntil(this.drop)).subscribe(() => {
        this.submitCard();
      }, err => console.error('submitEvent with errors', {err}));
    }
  }

  private submitCard() {
    const Frames: any = this.windowRef.nativeWindow['Frames']; // tslint:disable-line
    if (!Frames) {
      this.tokenizationFailed.emit({errorCode: 'frames_not_found', message: null});
      return false;
    }
    if (this.formSubmitted) {
      Frames.enableSubmitForm();
    }
    Frames.submitCard().catch((err) => {
      this.ngZone.run(() => {
        this.tokenizationFailed.emit({message: err, errorCode: err});
      });
    });
    this.formSubmitted = true;
  }

  private getStyleConfig(): FramesStyle {
    return {
      base: {
        color: '#333',
        fontSize: '16px'
      },
      hover: {
        color: '#333'
      },
      focus: {
        color: '#495057'
      },
      valid: {
        color: '#333'
      },
      invalid: {
        color: 'rgb(219, 0, 2)',
      },
      placeholder: {
        base: {
          color: '#97a2c1',
          fontSize: '16px'
        }
      }
    };
  }

  private modifyFramesCardholder(cardholder: FramesCardholder): boolean {
    if (cardholder == null) {
      return;
    }

    if (!this.framesInitialized && this.config) {
      this.config.cardholder = cardholder;
      return;
    }

    const Frames: any = window['Frames']; // tslint:disable-line
    if (!Frames) {
      return false;
    }
    try {
      Frames.cardholder = cardholder;
      if (this.config) {
        this.config.cardholder = cardholder;
      }
    } catch (e) {
      return false;
    }
    return true;
  }


  ngOnDestroy() {
    this.isDestroyed = true;
    this.drop.next();
  }
}
