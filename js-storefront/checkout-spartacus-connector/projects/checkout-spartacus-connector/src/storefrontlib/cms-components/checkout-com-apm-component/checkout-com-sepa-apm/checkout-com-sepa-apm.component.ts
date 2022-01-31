import { ChangeDetectionStrategy, Component, OnInit, Output, EventEmitter, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Address, Country, TranslationService, UserPaymentService } from '@spartacus/core';
import { exhaustMap, tap } from 'rxjs/operators';
import { BehaviorSubject, combineLatest, Observable, of } from 'rxjs';
import { ApmPaymentDetails, SepaPaymentTypeOption, SepaPaymentTypes } from '../../../interfaces';
import { PaymentType } from '../../../../core/model/ApmData';

@Component({
  selector: 'lib-checkout-com-sepa-apm',
  templateUrl: './checkout-com-sepa-apm.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CheckoutComSepaApmComponent implements OnInit, OnDestroy {
  @Output() setPaymentDetails = new EventEmitter<{ paymentDetails: ApmPaymentDetails, billingAddress: Address }>();

  submitting$ = new BehaviorSubject<boolean>(false);

  sepaForm: FormGroup = this.fb.group({
    paymentType: this.fb.group({code: ['', Validators.required]}),
    firstName: ['', Validators.required],
    lastName: ['', Validators.required],
    accountIban: ['', Validators.required],
    addressLine1: [null, Validators.required],
    addressLine2: [],
    city: [null, Validators.required],
    postalCode: ['', [
      Validators.required,
      Validators.maxLength(10)
    ]],
    country: this.fb.group({isocode: ['', Validators.required]}),
  });

  paymentTypes$: Observable<SepaPaymentTypeOption[]>;
  countries$: Observable<Country[]>;

  constructor(protected fb: FormBuilder,
              protected userPaymentService: UserPaymentService,
              protected translationService: TranslationService) {
  }

  ngOnInit(): void {
    this.countries$ = this.userPaymentService.getAllBillingCountries().pipe(
      tap((countries) => {
        // If the store is empty fetch countries. This is also used when changing language.
        if (Object.keys(countries).length === 0) {
          this.userPaymentService.loadBillingCountries();
        }
      })
    );

    this.populatePaymentTypes();
  }

  protected populatePaymentTypes() {
    this.paymentTypes$ = combineLatest([
      this.translationService.translate('sepaForm.paymentTypes.single'),
      this.translationService.translate('sepaForm.paymentTypes.recurring'),
    ]).pipe(
      exhaustMap(([singleTranslation, recurringTranslation]) => {
        const paymentTypes: SepaPaymentTypeOption[] = [
          {code: SepaPaymentTypes.SINGLE, label: singleTranslation},
          {code: SepaPaymentTypes.RECURRING, label: recurringTranslation},
        ];

        return of(paymentTypes);
      })
    );
  }

  next() {
    const {valid, value} = this.sepaForm;
    if (valid) {
      const paymentDetails: ApmPaymentDetails = {
        type: PaymentType.Sepa,
        ...value,
        country: value.country.isocode,
        paymentType: value.paymentType.code
      };

      this.submitting$.next(true);

      this.setPaymentDetails.emit({
        paymentDetails,
        billingAddress: {
          firstName: value.firstName,
          lastName: value.lastName,
          line1: value.addressLine1,
          line2: value.addressLine2 || '',
          postalCode: value.postalCode,
          town: value.city,
          country: value.country,
        } as Address
      });
    } else {
      this.sepaForm.markAllAsTouched();
    }
  }

  ngOnDestroy(): void {
    this.submitting$.next(false);
  }

}
