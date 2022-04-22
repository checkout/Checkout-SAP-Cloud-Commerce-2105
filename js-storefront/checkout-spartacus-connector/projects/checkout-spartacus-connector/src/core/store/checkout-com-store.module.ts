import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {EffectsModule} from '@ngrx/effects';
import {StoreModule} from '@ngrx/store';
import {effects} from './checkout-com.effects';
import {reducer, reducerProvider} from './checkout-com.reducer';
import {CHECKOUT_COM_FEATURE} from './checkout-com.state';

@NgModule({
  imports: [
    CommonModule,
    EffectsModule.forFeature(effects),
    StoreModule.forFeature(CHECKOUT_COM_FEATURE, reducer)
  ],
  providers: [reducerProvider]
})
export class CheckoutComStoreModule { }
