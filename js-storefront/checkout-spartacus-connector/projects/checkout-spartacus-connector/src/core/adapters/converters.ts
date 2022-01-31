import {ApmData, OccApmData} from '../model/ApmData';
import {Converter, PaymentDetails} from '@spartacus/core';
import {InjectionToken} from '@angular/core';
import { OccCmsComponentWithMedia} from '../model/ComponentData';
import {ApmPaymentDetails} from '../../storefrontlib/interfaces';

export const APM_NORMALIZER = new InjectionToken<Converter<OccApmData, ApmData>>(
  'ApmNormalizer'
);

export const COMPONENT_APM_NORMALIZER = new InjectionToken<Converter<OccCmsComponentWithMedia, ApmData>>(
  'ComponentDataApmNormalizer'
);

export const APM_PAYMENT_DETAILS_NORMALIZER = new InjectionToken<Converter<ApmPaymentDetails, PaymentDetails>>(
  'ApmPaymentDetailsNormalizer'
);
