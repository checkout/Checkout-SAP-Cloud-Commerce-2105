import { ChangeDetectionStrategy, Component, HostListener, Input, OnInit } from '@angular/core';
import {ApmData} from '../../../../core/model/ApmData';
import {Observable} from 'rxjs';
import {CheckoutComApmService} from '../../../../core/services/checkout-com-apm.service';

@Component({
  selector: 'lib-checkout-com-apm-tile',
  templateUrl: './checkout-com-apm-tile.component.html',
  styleUrls: ['./checkout-com-apm-tile.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CheckoutComApmTileComponent {
  @Input() apm: ApmData;
  selectedApm$: Observable<ApmData> = this.checkoutComApmService.getSelectedApmFromState();

  constructor(protected checkoutComApmService: CheckoutComApmService) {
  }

  @HostListener('click')
  select(): void {
    this.checkoutComApmService.selectApm(this.apm);
  }
}
