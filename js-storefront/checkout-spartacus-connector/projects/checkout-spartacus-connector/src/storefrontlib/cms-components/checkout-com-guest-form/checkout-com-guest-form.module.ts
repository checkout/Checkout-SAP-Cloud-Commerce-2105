import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CheckoutComGuestFormComponent } from './checkout-com-guest-form.component';
import {I18nModule, UrlModule} from "@spartacus/core";
import {FormErrorsModule} from "@spartacus/storefront";
import {ReactiveFormsModule} from "@angular/forms";
import {RouterModule} from "@angular/router";


@NgModule({
  declarations: [
    CheckoutComGuestFormComponent
  ],
  exports: [
    CheckoutComGuestFormComponent
  ],
  imports: [
    CommonModule,
    FormErrorsModule,
    ReactiveFormsModule,
    I18nModule,
  ]
})
export class CheckoutComGuestFormModule { }
