import {MediaContainer} from '@spartacus/storefront';

export interface AvailableApmResponseData {
  availableApmConfigurations?: OccApmData[];
}

export interface OccApmData {
  name: string;
  code: string;
  media?: {
    code: string;
    mime?: string;
    url: string
  };
  isRedirect?: boolean;
  isUserDataRequired?: boolean;
}

export enum PaymentType {
  Card = 'CARD',
  PayPal = 'PAYPAL',
  Fawry = 'FAWRY',
  iDeal = 'IDEAL',
  ACH = 'ACH',
  Sepa = 'SEPA',
  Klarna = 'KLARNA',
  Oxxo = 'OXXO',
  Sofort = 'SOFORT',
  ApplePay= 'APPLEPAY',
  GooglePay= 'GOOGLE_PAY',
}

export interface ApmData {
  code: PaymentType;
  name?: string;
  media?: MediaContainer;
  isRedirect?: boolean;
  isUserDataRequired?: boolean;
}
