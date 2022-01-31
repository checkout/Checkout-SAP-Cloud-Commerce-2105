import { NgModule } from '@angular/core';
import { translationChunksConfig, translations } from '@spartacus/assets';
import {
  CmsConfig,
  FeaturesConfig,
  I18nConfig,
  OccConfig,
  provideConfig,
  SiteContextConfig
} from '@spartacus/core';
import { defaultCmsContentProviders, layoutConfig, mediaConfig } from '@spartacus/storefront';
import { checkoutComTranslationChunkConfig, checkoutComTranslations } from 'checkout-spartacus-translations';
import { environment } from '../../environments/environment';
import { CheckoutConfig } from '@spartacus/checkout/root';

@NgModule({
  declarations: [],
  providers: [provideConfig(layoutConfig), provideConfig(mediaConfig), ...defaultCmsContentProviders, provideConfig({
    backend: {
      occ: {
        baseUrl: environment.occBaseUrl,
      }
    },
  } as OccConfig), provideConfig({
    context: {
      baseSite: ['electronics-spa'],
    },
  } as SiteContextConfig), provideConfig({
    i18n: {
      resources: translations,
      chunks: translationChunksConfig,
      fallbackLang: 'en'
    },
  } as I18nConfig), provideConfig({
    i18n: {
      resources: checkoutComTranslations,
      chunks: checkoutComTranslationChunkConfig,
      fallbackLang: 'en'
    },
  } as I18nConfig), provideConfig({
    features: {
      level: '3.2'
    }
  } as FeaturesConfig), provideConfig({
    featureModules: {
      CheckoutComComponentsModule: {
        module: () => import('checkout-spartacus-connector').then(m => m.CheckoutComComponentsModule),
        cmsComponents: [
          'CheckoutPaymentDetails',
          'CheckoutPlaceOrder',
          'OrderConfirmationThankMessageComponent',
          'OrderConfirmationOverviewComponent',
          'OrderConfirmationItemsComponent',
          'OrderConfirmationShippingComponent',
          'OrderConfirmationTotalsComponent',
          'OrderConfirmationContinueButtonComponent',
          'CheckoutReviewOrder',
          'AccountOrderDetailsItemsComponent',
          'AccountOrderDetailsShippingComponent',
        ],
      }
    }
  } as CmsConfig), provideConfig({
    checkout: {
      guest: true
    }
  } as CheckoutConfig)]
})
export class SpartacusConfigurationModule {
}
