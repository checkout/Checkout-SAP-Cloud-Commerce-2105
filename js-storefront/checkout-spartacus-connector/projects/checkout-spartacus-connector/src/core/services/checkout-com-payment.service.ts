import { Injectable } from '@angular/core';
import { StateWithCheckoutCom } from '../store/checkout-com.state';
import { select, Store } from '@ngrx/store';
import { getOccMerchantKey, getPaymentAddress, getPaymentDetails } from '../store/checkout-com.selectors';
import { Observable, throwError, of } from 'rxjs';
import { SetOccMerchantKey, SetPaymentAddress, CreatePaymentDetails, CreateApmPaymentDetails } from '../store/checkout-com.actions';
import { ActiveCartService, Address, UserIdService, PaymentDetails, OCC_USER_ID_ANONYMOUS } from '@spartacus/core';
import { switchMap, first } from 'rxjs/operators';
import { ApmPaymentDetails, CheckoutComPaymentDetails } from '../../storefrontlib/interfaces';
import { HttpErrorResponse } from '@angular/common/http';
import { getUserIdCartId } from '../shared/get-user-cart-id';

@Injectable({
  providedIn: 'root'
})
export class CheckoutComPaymentService {

  constructor(protected checkoutComStore: Store<StateWithCheckoutCom>,
              protected activeCartService: ActiveCartService,
              protected userIdService: UserIdService,
  ) { }

  // state observables
  public getOccMerchantKeyFromState(): Observable<string> {
    return this.checkoutComStore.pipe(select(getOccMerchantKey));
  }

  public requestOccMerchantKey(userId: string): void {
    this.checkoutComStore.dispatch(new SetOccMerchantKey({userId}));
  }

  public canSaveCard(userId): boolean {
    return userId !== OCC_USER_ID_ANONYMOUS;
  }

  public getPaymentDetailsFromState(): Observable<PaymentDetails> {
    return this.checkoutComStore.select(getPaymentDetails).pipe(
      first(state => state.paymentDetails != null || state.paymentDetailsError != null),
      switchMap((state) => {
        if (state.paymentDetailsError != null) {
          return throwError(state.paymentDetailsError);
        } else {
          return of(state.paymentDetails);
        }
      }));
  }

  public createPaymentDetails(paymentDetails: CheckoutComPaymentDetails, userId: string, cartId: string): Observable<PaymentDetails> {
    this.checkoutComStore.dispatch(new CreatePaymentDetails({paymentDetails, cartId, userId}));
    return this.getPaymentDetailsFromState();
  }

  public createApmPaymentDetails(paymentDetails: ApmPaymentDetails, cartId: string, userId: string): Observable<PaymentDetails> {
    this.checkoutComStore.dispatch(new CreateApmPaymentDetails({paymentDetails, cartId, userId}));
    return this.getPaymentDetailsFromState();
  }

  public getPaymentAddressFromState(): Observable<Address> {
    return this.checkoutComStore.select(getPaymentAddress).pipe(
      first(result => result != null),
      switchMap(result => {
        if (result instanceof HttpErrorResponse) {
          return throwError(result);
        } else {
          return of(result);
        }
      })
    );
  }

  public updatePaymentAddress(address: Address): Observable<Address> {
    return getUserIdCartId(this.userIdService, this.activeCartService).pipe(
      switchMap(({userId, cartId}) => {
        this.checkoutComStore.dispatch(
          new SetPaymentAddress({userId, cartId, address})
        );
        return this.getPaymentAddressFromState();
      }));
  }

  public setPaymentAddress(address: Address, userId: string, cartId: string) {
    this.checkoutComStore.dispatch(
      new SetPaymentAddress({userId, cartId, address})
    );
  }
}
