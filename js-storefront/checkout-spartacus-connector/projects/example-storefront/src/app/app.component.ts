import { Component } from '@angular/core';
import LogRocket from 'logrocket';

const logRocketAppId = 'LOG_ROCKET_APP_ID';

// Initialize LogRocket with your app ID
try {
  if (!logRocketAppId.includes('LOG_ROCKET')){
    LogRocket.init(logRocketAppId);
  }
} catch (err) {
  console.log('failed to initialize logrocket', err);
}

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  title = 'example-storefront';
}
