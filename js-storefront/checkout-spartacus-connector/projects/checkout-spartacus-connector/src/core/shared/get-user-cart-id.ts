import { ActiveCartService, UserIdService, getCartIdByUserId } from '@spartacus/core';
import { Observable } from 'rxjs';
import { withLatestFrom, map, first, take } from 'rxjs/operators';

export function getUserIdCartId(
  userIdService: UserIdService,
  activeCartService: ActiveCartService,
): Observable<{ userId: string, cartId: string }> {
  return activeCartService.getActive().pipe(
    first(cart => cart != null && typeof cart === 'object' && Object.keys(cart).length > 0),
    withLatestFrom(userIdService.getUserId()),
    map(([cart, userId]) => ({userId, cartId: getCartIdByUserId(cart, userId)})),
    take(1)
  );
}
