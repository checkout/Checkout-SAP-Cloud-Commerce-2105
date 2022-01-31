import {Injectable} from '@angular/core';
import {ApmData, PaymentType} from '../model/ApmData';
import {Converter, Image, OccConfig} from '@spartacus/core';
import {OccCmsComponentWithMedia} from '../model/ComponentData';

@Injectable({
  providedIn: 'root'
})
export class ComponentApmNormalizer implements Converter<OccCmsComponentWithMedia, ApmData> {

  constructor(protected config: OccConfig) {
  }

  convert(source: OccCmsComponentWithMedia, target?: ApmData): ApmData {
    if (target === undefined) {
      target = {...(source as any)};
    }

    target.code = PaymentType.Card; // source.code as PaymentType;
    target.name = source.name;

    if (source.media) {
      target.media =
        {
          mobile: {
            url: this.normalizeImageUrl(source.media.url),
            alt: source.name
          } as Image
        };
    }
    return target;
  }


  /** taken from product-image-normalizer.ts
   * Traditionally, in an on-prem world, medias and other backend related calls
   * are hosted at the same platform, but in a cloud setup, applications are are
   * typically distributed cross different environments. For media, we use the
   * `backend.media.baseUrl` by default, but fallback to `backend.occ.baseUrl`
   * if none provided.
   */
  private normalizeImageUrl(url: string): string {
    if (new RegExp(/^(http|data:image|\/\/)/i).test(url)) {
      return url;
    }
    return (
      (this.config.backend.media.baseUrl ||
        this.config.backend.occ.baseUrl ||
        '') + url
    );
  }
}
