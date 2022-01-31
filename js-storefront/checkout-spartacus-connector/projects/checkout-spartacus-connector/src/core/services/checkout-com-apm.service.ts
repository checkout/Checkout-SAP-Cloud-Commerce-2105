import { Injectable } from '@angular/core';
import { select, Store } from '@ngrx/store';
import { StateWithCheckoutCom } from '../store/checkout-com.state';
import { ActiveCartService, CmsService, ConverterService, UserIdService } from '@spartacus/core';
import { map, first, switchMap } from 'rxjs/operators';
import { RequestAvailableApms, SetSelectedApm, SetKlarnaInitParams } from '../store/checkout-com.actions';
import { Observable, throwError, of } from 'rxjs';
import { ApmData, PaymentType } from '../model/ApmData';
import { getApmLoading, getAvailableApms, getSelectedApm, getKlarnaInitParams } from '../store/checkout-com.selectors';
import { OccCmsComponentWithMedia } from '../model/ComponentData';
import { COMPONENT_APM_NORMALIZER } from '../adapters/converters';
import { KlarnaInitParams } from '../interfaces';
import { getUserIdCartId } from '../shared/get-user-cart-id';

@Injectable({
  providedIn: 'root'
})
export class CheckoutComApmService {

  constructor(protected checkoutComStore: Store<StateWithCheckoutCom>,
              protected activeCartService: ActiveCartService,
              protected userIdService: UserIdService,
              protected cmsService: CmsService,
              protected convertService: ConverterService) {
  }

  getApmByComponent(componentUid: string, paymentType: PaymentType): Observable<ApmData> {
    return this.cmsService.getComponentData<OccCmsComponentWithMedia>(componentUid)
      .pipe(this.convertService.pipeable(COMPONENT_APM_NORMALIZER),
        map((apmData: ApmData) => {
          return {
            ...apmData, code: paymentType
          };
        })
      );
  }

  requestAvailableApms(): Observable<ApmData[]> {
    return getUserIdCartId(this.userIdService, this.activeCartService)
      .pipe(
        switchMap(({userId, cartId}) => {
          this.checkoutComStore.dispatch(new RequestAvailableApms({userId, cartId}));
          return this.getAvailableApmsFromState();
        }
      ));
  }

  selectApm(apm: ApmData): void {
    this.checkoutComStore.dispatch(new SetSelectedApm(apm));
  }

  getAvailableApmsFromState(): Observable<ApmData[]> {
    return this.checkoutComStore.pipe(select(getAvailableApms));
  }

  getSelectedApmFromState(): Observable<ApmData> {
    return this.checkoutComStore.pipe(select(getSelectedApm));
  }

  getIsApmLoadingFromState(): Observable<boolean> {
    return this.checkoutComStore.pipe(select(getApmLoading));
  }

  getKlarnaInitParams(): Observable<KlarnaInitParams> {
    return getUserIdCartId(this.userIdService, this.activeCartService)
    .pipe(switchMap(({userId, cartId}) => {
      this.checkoutComStore.dispatch(new SetKlarnaInitParams({userId, cartId}));
      return this.checkoutComStore.select(getKlarnaInitParams).pipe(
        first(p => p != null),
        switchMap((response) => {
          if (response.httpError != null) {
            return throwError(response.httpError);
          } else {
            return of(response);
          }
        })
      );
    }));
  }
}
