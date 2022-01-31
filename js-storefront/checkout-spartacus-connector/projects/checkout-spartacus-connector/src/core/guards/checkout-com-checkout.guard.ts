import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, UrlTree } from '@angular/router';
import { Observable, of } from 'rxjs';
import {
  ActiveCartService,
  GlobalMessageService,
  GlobalMessageType,
  RoutingConfigService,
  SemanticPathService,
  UserIdService,
} from '@spartacus/core';
import { CheckoutComCheckoutService } from '../services/checkout-com-checkout.service';
import { getUserIdCartId } from '../shared/get-user-cart-id';
import { catchError, map, switchMap, timeout } from 'rxjs/operators';
import { CheckoutStepService } from '@spartacus/checkout/components';
import { CheckoutFacade, CheckoutPaymentFacade, CheckoutStepType } from '@spartacus/checkout/root';

@Injectable({
  providedIn: 'root',
})
export class CheckoutComCheckoutGuard implements CanActivate {

  constructor(protected router: Router,
              protected checkoutComCheckoutService: CheckoutComCheckoutService,
              protected globalMessageService: GlobalMessageService,
              protected routingConfigService: RoutingConfigService,
              protected checkoutStepService: CheckoutStepService,
              protected checkoutPaymentFacade: CheckoutPaymentFacade,
              protected userIdService: UserIdService,
              protected activeCartService: ActiveCartService,
              protected checkoutFacade: CheckoutFacade,
              protected semanticPathService: SemanticPathService
  ) {
  }

  canActivate(route: ActivatedRouteSnapshot): Observable<boolean | UrlTree> {
    const params = route.queryParams;
    if (params == null || typeof params !== 'object' || Object.keys(params).length === 0) {
      return this.checkoutFacade.getOrderDetails().pipe(
        map((orderDetails) => {
          if (orderDetails && Object.keys(orderDetails).length !== 0) {
            return true;
          } else {
            return this.router.parseUrl(this.semanticPathService.get('orders'));
          }
        })
      );
    }

    if (params.authorized === false || params.authorized === 'false') {
      this.globalMessageService.add({key: 'checkoutReview.paymentAuthorizationError'}, GlobalMessageType.MSG_TYPE_ERROR);
      this.checkoutPaymentFacade.resetSetPaymentDetailsProcess();
    } else {
      const sessionId = params['cko-session-id'];
      if (sessionId) {

        return getUserIdCartId(this.userIdService, this.activeCartService).pipe(
          switchMap(({userId, cartId}) => {
            return this.checkoutComCheckoutService.authorizeOrder(sessionId, userId, cartId)}
          ),
          // if cart is not loaded within 4 seconds we reload the page.
          // This can happen when user reloads the page and the params are still in tact
          timeout(4000),
          catchError(() => of(this.router.parseUrl(this.semanticPathService.get('orderConfirmation')))),
        );
      }
    }

    return of(this.router.parseUrl(
      this.routingConfigService.getRouteConfig(
        this.checkoutStepService.getCheckoutStepRoute(
          CheckoutStepType.PAYMENT_DETAILS
        )
      ).paths[0]
    ));
  }

}
