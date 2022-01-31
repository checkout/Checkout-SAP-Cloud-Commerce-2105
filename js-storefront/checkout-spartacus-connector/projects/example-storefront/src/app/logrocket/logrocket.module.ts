import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LogrocketService } from './logrocket.service';
import { LogrocketIdentifyComponent } from './logrocket-identify/logrocket-identify.component';

@NgModule({
  declarations: [LogrocketIdentifyComponent],
  imports: [
    CommonModule,
  ],
  exports: [
    LogrocketIdentifyComponent
  ],
  providers: [LogrocketService]
})
export class LogrocketModule {
}
