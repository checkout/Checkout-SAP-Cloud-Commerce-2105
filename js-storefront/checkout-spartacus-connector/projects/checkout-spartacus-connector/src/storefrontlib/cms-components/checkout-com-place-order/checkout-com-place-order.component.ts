import { Component, ChangeDetectionStrategy, ViewContainerRef, OnDestroy } from '@angular/core';
import {
  CheckoutReplenishmentFormService,
  CheckoutStepService,
  PlaceOrderComponent
} from '@spartacus/checkout/components';
import { RoutingService, ORDER_TYPE, WindowRef } from '@spartacus/core';
import { FormBuilder } from '@angular/forms';
import { CheckoutComCheckoutService } from '../../../core/services/checkout-com-checkout.service';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { ActivatedRoute } from '@angular/router';
import { makeFormErrorsVisible } from '../../../core/shared/make-form-errors-visible';
import { LaunchDialogService } from '@spartacus/storefront';

@Component({
  selector: 'cx-place-order',
  templateUrl: './checkout-com-place-order.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CheckoutComPlaceOrderComponent extends PlaceOrderComponent implements OnDestroy {

  private drop = new Subject<void>();

  constructor(
    protected checkoutService: CheckoutComCheckoutService,
    protected routingService: RoutingService,
    protected fb: FormBuilder,
    protected checkoutReplenishmentFormService: CheckoutReplenishmentFormService,
    protected launchDialogService: LaunchDialogService,
    protected vcr: ViewContainerRef,
    protected stepService: CheckoutStepService,
    protected activatedRoute: ActivatedRoute,
    protected windowRef: WindowRef,
  ) {
    super(checkoutService, routingService, fb, checkoutReplenishmentFormService, launchDialogService, vcr);

    this.checkoutService.clearPlaceOrderState();
  }

  submitForm(): void {
    if (this.checkoutSubmitForm.valid && Boolean(this.currentOrderType)) {
      switch (this.currentOrderType) {
        case ORDER_TYPE.PLACE_ORDER: {
          this.checkoutService.placeOrder(this.checkoutSubmitForm.valid);
          this.checkoutService.getOrderResultFromState().pipe(takeUntil(this.drop)).subscribe((result) => {
            if (result.redirect?.redirectUrl) {
              this.windowRef.nativeWindow.location.href = result.redirect.redirectUrl;
              return;
            }
            if (result.successful === false) {
              this.routingService.go(this.stepService.getPreviousCheckoutStepUrl(this.activatedRoute));
            }
          }, err => console.error('getOrderResultFromState with error', {err}));
          break;
        }

        case ORDER_TYPE.SCHEDULE_REPLENISHMENT_ORDER: {
          this.checkoutService.scheduleReplenishmentOrder(
            this.scheduleReplenishmentFormData,
            this.checkoutSubmitForm.valid
          );
          break;
        }
      }
    } else {
      makeFormErrorsVisible(this.checkoutSubmitForm);
    }
  }

  ngOnDestroy() {
    this.drop.next();
    super.ngOnDestroy();
  }
}
