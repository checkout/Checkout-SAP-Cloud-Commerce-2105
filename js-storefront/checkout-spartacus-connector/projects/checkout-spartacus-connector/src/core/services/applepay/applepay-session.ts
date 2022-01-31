/* tslint:disable */
import { WindowRef } from '@spartacus/core';

export const createApplePaySession = (windowRef: WindowRef) => {
  if (!windowRef.isBrowser()) {
    return null;
  }

  return windowRef.nativeWindow['ApplePaySession'];
}
/* tslint:enable */
