import { Address, PaymentDetails } from '@spartacus/core';
import { PaymentType } from '../core/model/ApmData';

export interface CheckoutComPaymentDetails extends PaymentDetails {
  accountIban?: string;
  accountNumber?: string;
  accountType?: string;
  addressLine1?: string;
  addressLine2?: string;
  authorizationToken?: string;
  bic?: string;
  cardBin: string;
  city?: string;
  companyName?: string;
  country?: string;
  firstName?: string;
  lastName?: string;
  mobileNumber?: string;
  paymentToken?: string;
  paymentType?: string;
  postalCode?: string;
  routingNumber?: string;
  type?: string; // 'CARD'
}

export interface ApmPaymentDetails {
  type: PaymentType;
  billingAddress?: Address;
  authorizationToken?: string;
  document?: string;
  country?: string;
  paymentType?: SepaPaymentTypes;
  mobileNumber?: string;
  bic?: string;
  firstName?: string;
  lastName?: string;
  addressLine1?: string;
  addressLine2?: string;
  postalCode?: string;
  city?: string;
  accountIban?: string;
}

export enum KlarnaPaymentMethodCategory {
  payNow = 'pay_now',
  payLater = 'pay_later',
  payOverTime = 'pay_over_time',
}

export enum SepaPaymentTypes {
  SINGLE = 'SINGLE',
  RECURRING = 'RECURRING'
}

export interface SepaPaymentTypeOption {
  code: string;
  label: string;
}
