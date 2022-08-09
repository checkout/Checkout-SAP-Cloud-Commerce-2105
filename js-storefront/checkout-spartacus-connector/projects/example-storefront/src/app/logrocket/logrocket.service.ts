import { Injectable } from '@angular/core';
import { ActiveCartService, Cart } from '@spartacus/core';

import LogRocket from 'logrocket';
import { filter } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class LogrocketService {

  constructor(public activeCartService: ActiveCartService) {
  }

  identify(): void {
    this.activeCartService.getActive()
      .pipe(
        filter(c => !!c && Object.keys(c).length > 0)
      ).subscribe(({guid, user}) => {
      try {
        LogRocket.identify(user.uid, {
          cart: guid,
          name: user.name,
        });
      } catch (err) {
        console.log('Failed to identify logrocket error tracking', err);
      }
    }, err => console.log('getActive with errors', {err}));
  }
}
