import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  EventEmitter,
  Input,
  NgZone,
  OnDestroy,
  OnInit,
  Output,
  ViewChild
} from '@angular/core';
import { CheckoutComApmService } from '../../../../core/services/checkout-com-apm.service';
import { BehaviorSubject, EMPTY, Subject, throwError } from 'rxjs';
import { debounceTime, distinctUntilChanged, filter, finalize, switchMap, take, takeUntil, tap } from 'rxjs/operators';
import { KlarnaInitParams } from '../../../../core/interfaces';
import { ApmPaymentDetails, KlarnaPaymentMethodCategory } from '../../../interfaces';
import { Address, GlobalMessageService, GlobalMessageType, WindowRef } from '@spartacus/core';
import { PaymentType } from '../../../../core/model/ApmData';
import { FormGroup } from '@angular/forms';
import { CheckoutComPaymentService } from '../../../../core/services/checkout-com-payment.service';
import { makeFormErrorsVisible } from '../../../../core/shared/make-form-errors-visible';
import { CheckoutDeliveryFacade } from '@spartacus/checkout/root';

interface DisplayKlarnaPaymentMethodCategory {
  code: KlarnaPaymentMethodCategory;
  key: string;
  disabled?: boolean;
}

interface KlarnaLoadError {
  invalid_fields?: string[];
}

interface KlarnaLoadResponse {
  show_form: boolean;
  error?: KlarnaLoadError;
}

interface KlarnaAuthResponse {
  authorization_token?: string;
  show_form: boolean;
  approved?: boolean;
  finalize_required?: boolean;
  error?: KlarnaLoadError;
}

@Component({
  selector: 'lib-checkout-com-apm-klarna',
  templateUrl: './checkout-com-klarna.component.html',
  styleUrls: ['./checkout-com-klarna.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CheckoutComKlarnaComponent implements OnInit, OnDestroy {
  @Input() billingAddressForm: FormGroup = new FormGroup({});
  @Output() setPaymentDetails = new EventEmitter<{ paymentDetails: ApmPaymentDetails, billingAddress: Address }>();
  @ViewChild('widget') widget: ElementRef;
  public selectedCategory$ = new BehaviorSubject<DisplayKlarnaPaymentMethodCategory>(null);
  public categories$ = new BehaviorSubject<DisplayKlarnaPaymentMethodCategory[]>([]);
  public loadingWidget$ = new BehaviorSubject<boolean>(false);
  public authorizing$ = new BehaviorSubject<boolean>(false);
  public initializing$ = new BehaviorSubject<boolean>(false);
  public sameAsShippingAddress$ = new BehaviorSubject<boolean>(true);

  private sameAsShippingAddress: boolean = true;
  private allCategories: KlarnaPaymentMethodCategory[] = [];
  private drop = new Subject<void>();
  private currentCountryCode = new BehaviorSubject<string>(null);
  private billingAddressHasBeenSet = false;

  constructor(
    protected checkoutComApmSrv: CheckoutComApmService,
    protected msgSrv: GlobalMessageService,
    protected checkoutComPaymentService: CheckoutComPaymentService,
    protected checkoutDeliveryFacade: CheckoutDeliveryFacade,
    private ngZone: NgZone,
    protected windowRef: WindowRef,
  ) {
  }

  ngOnInit(): void {
    this.addScript();
    this.listenForCountryCode();
    this.listenForCountrySelection();
    this.listenForCategorySelection();
    this.listenForAddressSourceChange();
  }

  listenForAddressSourceChange() {
    this.sameAsShippingAddress$.pipe(
      switchMap((sameAsShippingAddress) => {
        this.sameAsShippingAddress = sameAsShippingAddress;
        if (sameAsShippingAddress) {
          return this.checkoutDeliveryFacade.getDeliveryAddress().pipe(
            take(1),
            tap((address) => {
              this.currentCountryCode.next(address?.country?.isocode);
            })
          );
        } else {
          const countryCtrl = this.billingAddressForm.get('country.isocode');
          if (countryCtrl && countryCtrl.value) {
            this.currentCountryCode.next(countryCtrl.value);
          }
          return EMPTY;
        }
      }),
      takeUntil(this.drop)
    ).subscribe();
  }

  ngOnDestroy() {
    this.drop.next();
  }

  public authorize() {
    if (!this.sameAsShippingAddress && !this.billingAddressForm.valid) {
      makeFormErrorsVisible(this.billingAddressForm);
      return;
    }
    if (this.authorizing$.getValue()) {
      return;
    }
    const category = this.selectedCategory$.getValue();
    if (!category) {
      return;
    }

    try {
      const k = this.windowRef.nativeWindow['Klarna']?.Payments; // tslint:disable-line
      if (!k) {
        console.error('Klarna is not set');
        return false;
      }
      let billingAddress = null;
      if (!this.sameAsShippingAddress) {
        billingAddress = this.billingAddressForm.value;
      }
      this.authorizing$.next(true);
      k.authorize({payment_method_category: category.code}, this.getKlarnaCountryParams(), (response: KlarnaAuthResponse) => {
        this.authorizing$.next(false);
        if (response != null && response.approved === true && response.authorization_token) {
          this.setPaymentDetails.next({
            paymentDetails: {
              type: PaymentType.Klarna,
              authorizationToken: response.authorization_token,
            } as ApmPaymentDetails,
            billingAddress
          });
        }
      });
    } catch (e) {
      this.authorizing$.next(false);
      console.error('CheckoutComKlarnaComponent::initKlarna', e);
    }
  }

  selectCategory(category: DisplayKlarnaPaymentMethodCategory) {
    if (category && !category.disabled) {
      this.selectedCategory$.next(category);
    }
  }

  private listenForCountryCode() {
    this.currentCountryCode.pipe(
      filter(c => !!c && this.billingAddressHasBeenSet),
      distinctUntilChanged(),
      switchMap(() => {
        this.initializing$.next(true);
        return this.checkoutComApmSrv.getKlarnaInitParams().pipe(
          finalize(() => this.initializing$.next(false))
        );
      }),
      takeUntil(this.drop)
    ).subscribe({
      next: (params) => {
        this.initKlarna(params);
        const allSrc = params.paymentMethodCategories ?? [];
        this.allCategories = allSrc.map(c => c);
        this.selectedCategory$.next(null);
        this.renderCategories(this.allCategories);
      },
      error: (error) => {
        console.error(error);
        this.msgSrv.add({key: 'paymentForm.klarna.initializationFailed'}, GlobalMessageType.MSG_TYPE_ERROR);
      }
    });
  }

  private listenForCountrySelection() {
    this.billingAddressForm.valueChanges.pipe(
      filter(values => values?.country?.isocode), distinctUntilChanged(), takeUntil(this.drop)
    ).subscribe((values) => {
      this.currentCountryCode.next(values?.country?.isocode);
    }, err => console.error('listenForCountrySelection with errors', {err}));
  }

  private klarnaIsReady() {
    this.initializing$.next(true);
    this.checkoutDeliveryFacade.getDeliveryAddress().pipe(
      switchMap((shippingAddress) => {
        if (shippingAddress == null || typeof shippingAddress !== 'object') {
          return throwError('Shipping address is required');
        }
        return this.checkoutComPaymentService.updatePaymentAddress(shippingAddress)
                   .pipe(tap(() => this.billingAddressHasBeenSet = true));
      }),
      take(1),
    ).pipe(finalize(() => this.initializing$.next(false)), takeUntil(this.drop)).subscribe({
      next: (address) => {
        if (address?.country?.isocode) {
          this.currentCountryCode.next(address.country.isocode);
        } else {
          this.msgSrv.add({key: 'paymentForm.klarna.countryIsRequired'}, GlobalMessageType.MSG_TYPE_ERROR);
        }
      }, error: () => {
        this.msgSrv.add({key: 'paymentForm.klarna.countryIsRequired'}, GlobalMessageType.MSG_TYPE_ERROR);
      }
    });
  }

  private initKlarna(params: KlarnaInitParams) {
    try {
      const k = this.windowRef.nativeWindow['Klarna']?.Payments; // tslint:disable-line
      if (!k) {
        console.error('Klarna is not set');
        return false;
      }
      k.init({client_token: params.clientToken});
    } catch (e) {
      console.error('CheckoutComKlarnaComponent::initKlarna', e);
    }
  }

  private addScript() {
    if (!this.windowRef.nativeWindow['Klarna']) {  // tslint:disable-line
      Object.defineProperty(this.windowRef.nativeWindow, 'klarnaAsyncCallback', {
        value: () => {
          this.ngZone.run(() => {
            this.klarnaIsReady();
          });
        },
      });

      const script = this.windowRef.document.createElement('script');
      script.setAttribute('src', 'https://x.klarnacdn.net/kp/lib/v1/api.js');
      script.setAttribute('async', 'true');
      this.windowRef.document.body.appendChild(script);
    } else {
      this.ngZone.run(() => {
        this.klarnaIsReady();
      });
    }
  }

  private getTranslationKeyForCategory(category: KlarnaPaymentMethodCategory): string {
    switch (category) {
      case KlarnaPaymentMethodCategory.payNow:
        return 'paymentForm.klarna.paymentMethodCategory.payNow';
      case KlarnaPaymentMethodCategory.payLater:
        return 'paymentForm.klarna.paymentMethodCategory.payLater';
      case KlarnaPaymentMethodCategory.payOverTime:
        return 'paymentForm.klarna.paymentMethodCategory.payOverTime';
    }
    return null;
  }

  private listenForCategorySelection() {
    this.selectedCategory$.pipe(
      distinctUntilChanged(),
      debounceTime(1), // let Angular render the widget container
      takeUntil(this.drop)
    ).subscribe((category) => {
      if (this.widget?.nativeElement) {
        this.loadWidget(category, this.widget.nativeElement);
      }
    }, err => console.error('listenForCategorySelection with errors', {err}));
  }

  private getKlarnaCountryParams(): { purchase_country: string } {
    if (!this.sameAsShippingAddress) {
      const ctrlCountryIsoCode = this.billingAddressForm.get('country.isocode');
      if (ctrlCountryIsoCode && ctrlCountryIsoCode.value) {
        return {purchase_country: ctrlCountryIsoCode.value};
      }
    }
    return null;
  }

  private loadWidget(category: DisplayKlarnaPaymentMethodCategory, container: HTMLElement | string) {
    try {
      const k = this.windowRef.nativeWindow['Klarna']?.Payments; // tslint:disable-line
      if (!k) {
        console.error('Klarna is not set');
        return false;
      }
      this.loadingWidget$.next(true);
      k.load({container, payment_method_category: category.code}, this.getKlarnaCountryParams(), (response: KlarnaLoadResponse) => {
        this.loadingWidget$.next(false);
        if (response != null && typeof response === 'object') {
          if (response.hasOwnProperty('show_form')) {
            if (response.show_form === false) {
              category.disabled = true;
            }
          }
          if (response.error) {
            console.error('CheckoutComKlarnaComponent::loadWidget::response', response.error);
          }
        }
      });
    } catch (e) {
      this.loadingWidget$.next(false);
      console.error('CheckoutComKlarnaComponent::loadWidget', e);
    }
  }

  private renderCategories(paymentMethodCategories: KlarnaPaymentMethodCategory[]) {
    const categories = [];
    if (paymentMethodCategories?.length) {
      paymentMethodCategories.forEach((category) => {
        const key = this.getTranslationKeyForCategory(category);
        if (key) {
          categories.push({code: category, key});
        }
      });
    }
    this.categories$.next(categories);
  }
}

