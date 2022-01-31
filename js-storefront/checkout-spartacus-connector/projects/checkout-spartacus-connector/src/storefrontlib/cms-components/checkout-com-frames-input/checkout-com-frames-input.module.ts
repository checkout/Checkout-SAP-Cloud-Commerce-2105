import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CheckoutComFramesInputComponent } from './checkout-com-frames-input.component';
import { ReactiveFormsModule } from '@angular/forms';

@NgModule({
  declarations: [CheckoutComFramesInputComponent],
  exports: [
    CheckoutComFramesInputComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
  ]
})
export class CheckoutComFramesInputModule { }
