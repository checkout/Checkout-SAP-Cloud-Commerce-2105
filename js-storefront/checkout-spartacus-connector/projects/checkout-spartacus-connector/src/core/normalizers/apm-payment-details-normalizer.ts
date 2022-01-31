import { Injectable } from '@angular/core';
import {ActiveCartService, CardType, Converter, PaymentDetails} from '@spartacus/core';
import { ApmPaymentDetails } from '../../storefrontlib/interfaces';

@Injectable({
  providedIn: 'root'
})
export class ApmPaymentDetailsNormalizer implements Converter<ApmPaymentDetails, PaymentDetails> {

  constructor() {
  }

  convert(source: ApmPaymentDetails, target?: PaymentDetails): PaymentDetails {
    if (!target) {
      target = {};
    }

    target.cardType = {
      code: source.type,
      name: source.type
    } as CardType;

    return target;
  }

}
