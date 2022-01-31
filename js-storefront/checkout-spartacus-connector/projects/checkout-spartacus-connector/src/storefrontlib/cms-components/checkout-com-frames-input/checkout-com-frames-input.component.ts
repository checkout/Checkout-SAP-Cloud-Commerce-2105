import { Component, OnInit, ChangeDetectionStrategy, Input } from '@angular/core';
import { CSS_CLASS_CARD_NUMBER, CSS_CLASS_EXPIRY_DATE, CSS_CLASS_CVV } from './interfaces';
import { FormGroup, FormControl } from '@angular/forms';
import { FrameElementIdentifier } from '../checkout-com-frames-form/interfaces';

@Component({
  selector: 'lib-checkout-com-frames-input',
  templateUrl: './checkout-com-frames-input.component.html',
  styleUrls: ['./checkout-com-frames-input.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CheckoutComFramesInputComponent implements OnInit {
  @Input() fieldType: FrameElementIdentifier = null;
  @Input() form: FormGroup = null;
  @Input() fieldName: string = null;
  @Input() icon: string;

  public fieldCtrl: FormControl = null;

  constructor() { }

  ngOnInit() {
    this.setCtrlByName(this.fieldName);
  }

  private setCtrlByName(name: string) {
    if (name) {
      this.fieldCtrl = new FormControl();
      if (!this.form) {
        const controls = {};
        controls[name] = this.fieldCtrl;
        this.form = new FormGroup(controls);
      } else {
        this.form.setControl(name, this.fieldCtrl);
      }
    }
  }

  get cssClassByFieldType(): string {
    switch (this.fieldType) {
      case FrameElementIdentifier.CardNumber:
        return CSS_CLASS_CARD_NUMBER;
      case FrameElementIdentifier.ExpiryDate:
        return CSS_CLASS_EXPIRY_DATE;
      case FrameElementIdentifier.Cvv:
        return CSS_CLASS_CVV;
    }
    return null;
  }
}
