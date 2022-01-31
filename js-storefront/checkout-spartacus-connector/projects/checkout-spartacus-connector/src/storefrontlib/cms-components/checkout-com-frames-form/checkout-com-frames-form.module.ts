import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CheckoutComFramesFormComponent } from './checkout-com-frames-form.component';
import { ReactiveFormsModule } from '@angular/forms';
import { CheckoutComFramesInputModule } from '../checkout-com-frames-input/checkout-com-frames-input.module';
import { I18nModule } from '@spartacus/core';
import { FormErrorsModule } from '@spartacus/storefront';

@NgModule({
  declarations: [CheckoutComFramesFormComponent],
  exports: [
    CheckoutComFramesFormComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    CheckoutComFramesInputModule,
    I18nModule,
    FormErrorsModule
  ]
})
export class CheckoutComFramesFormModule {}
