import {HttpClientModule} from '@angular/common/http';
import {NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';
import {EffectsModule} from '@ngrx/effects';
import {StoreModule} from '@ngrx/store';
import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {SpartacusModule} from './spartacus/spartacus.module';
import {LogrocketModule} from './logrocket/logrocket.module';
import { ExpressAddToCartComponentModule } from './components/express-add-to-cart/express-add-to-cart-component.module';
import { ExpressCartTotalsModule } from './components/express-cart-totals/express-cart-totals.module';
import {environment} from "../environments/environment";
import { StoreDevtoolsModule } from '@ngrx/store-devtools';

@NgModule({
  declarations: [
    AppComponent,
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    AppRoutingModule,
    StoreModule.forRoot({}),
    EffectsModule.forRoot([]),
    SpartacusModule,
    LogrocketModule,

    // Demo Module For GooglePay / ApplePay Express checkout on Pdp
    ExpressAddToCartComponentModule,
    // Demo Module for GooglePay / ApplePay Express checkout on Cart Page
    ExpressCartTotalsModule,
    StoreDevtoolsModule.instrument({
      maxAge: 25, // Retains last 25 states
      logOnly: environment.production, // Restrict extension to log-only mode
      autoPause: true, // Pauses recording actions and state changes when the extension window is not open
    }),
  ],

  bootstrap: [AppComponent],
})
export class AppModule {
}
