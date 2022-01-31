import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CheckoutComApmTileComponent } from './checkout-com-apm-tile.component';
import { MediaModule } from '@spartacus/storefront';

@NgModule({
  declarations: [CheckoutComApmTileComponent],
  exports: [CheckoutComApmTileComponent],
  imports: [
    CommonModule,
    MediaModule,
  ]
})
export class CheckoutComApmTileModule { }
