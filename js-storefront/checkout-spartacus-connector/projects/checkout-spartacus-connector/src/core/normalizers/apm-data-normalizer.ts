import {Injectable} from '@angular/core';
import {ApmData, OccApmData, PaymentType} from '../model/ApmData';
import {Converter, Image, OccConfig} from '@spartacus/core';

@Injectable({
  providedIn: 'root'
})
export class ApmDataNormalizer implements Converter<OccApmData, ApmData> {

  constructor(protected config: OccConfig) {
  }

  convert(source: OccApmData, target?: ApmData): ApmData {
    if (target === undefined) {
      target = {...(source as any)};
    }

    target.code = source.code as PaymentType;
    target.name = source.name;
    target.isUserDataRequired = source.isUserDataRequired || false;
    target.isRedirect = source.isRedirect || false;

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
