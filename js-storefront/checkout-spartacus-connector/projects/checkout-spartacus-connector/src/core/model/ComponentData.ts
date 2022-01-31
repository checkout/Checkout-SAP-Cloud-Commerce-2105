import {CmsComponent} from '@spartacus/core';
import { MediaContainer} from '@spartacus/storefront';

export interface OccCmsComponentWithMedia extends CmsComponent {
  media?: {
    code: string;
    mime?: string;
    url: string
  };
}

export interface CmsComponentWithMedia extends CmsComponent {
  media?: MediaContainer;
}
