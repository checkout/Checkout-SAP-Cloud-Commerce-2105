import {Component, OnDestroy} from '@angular/core';
import {GuestRegisterFormComponent} from "@spartacus/checkout/components";
import {takeUntil} from "rxjs/operators";
import {Subject} from "rxjs";

@Component({
  selector: 'cx-guest-register-form',
  templateUrl: './checkout-com-guest-form.component.html',
})
export class CheckoutComGuestFormComponent extends GuestRegisterFormComponent implements OnDestroy {
  private drop = new Subject<void>();

  submit() {
    if (this.guestRegisterForm.valid) {
      this.userRegisterFacade.registerGuest(
        this.guid,
        this.guestRegisterForm.value.password
      )?.pipe(takeUntil(this.drop)).subscribe(res => res, error => console.log(error));
      this.routingService.go({cxRoute: 'home'});
    } else {
      this.guestRegisterForm.markAllAsTouched();
    }
  }

  ngOnDestroy() {
    super.ngOnDestroy();
    this.drop.next();
  }
}
