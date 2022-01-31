import { Component, OnInit } from '@angular/core';
import { LogrocketService } from '../logrocket.service';

@Component({
  selector: 'app-logrocket-identify',
  template: '',
})
export class LogrocketIdentifyComponent implements OnInit {

  constructor(private logRocketService: LogrocketService) {
  }

  ngOnInit(): void {
    this.logRocketService.identify();
  }

}
