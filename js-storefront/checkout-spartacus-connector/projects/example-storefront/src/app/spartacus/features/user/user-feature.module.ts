import { NgModule } from '@angular/core';
import { CmsConfig, I18nConfig, provideConfig } from '@spartacus/core';
import { userAccountTranslationChunksConfig, userAccountTranslations } from '@spartacus/user/account/assets';
import { UserAccountRootModule } from '@spartacus/user/account/root';
import { userProfileTranslationChunksConfig, userProfileTranslations } from '@spartacus/user/profile/assets';
import { UserProfileRootModule } from '@spartacus/user/profile/root';

@NgModule({
  declarations: [],
  imports: [
    UserAccountRootModule,
    UserProfileRootModule
  ],
  providers: [provideConfig({
    featureModules: {
      userAccount: {
        module: () =>
          import('@spartacus/user/account').then((m) => m.UserAccountModule),
      },
    }
  } as CmsConfig),
  provideConfig({
    i18n: {
      resources: userAccountTranslations,
      chunks: userAccountTranslationChunksConfig,
    },
  } as I18nConfig),
  provideConfig({
    featureModules: {
      userProfile: {
        module: () =>
          import('@spartacus/user/profile').then((m) => m.UserProfileModule),
      },
    }
  } as CmsConfig),
  provideConfig({
    i18n: {
      resources: userProfileTranslations,
      chunks: userProfileTranslationChunksConfig,
    },
  } as I18nConfig)
  ]
})
export class UserFeatureModule { }
