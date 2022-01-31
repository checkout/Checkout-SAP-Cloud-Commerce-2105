import { TestBed } from '@angular/core/testing';

import { ApmDataNormalizer } from './apm-data-normalizer';
import {ApmData, OccApmData} from '../model/ApmData';
import {OccConfig} from '@spartacus/core';

const MockOccModuleConfig: OccConfig = {
  backend: {
    media: {
      baseUrl: 'https://localhost:9002'
    }
  }
};

describe('ApmDataNormalizer', () => {
  let service: ApmDataNormalizer;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        {
          provide: OccConfig,
          useValue: MockOccModuleConfig
        }
      ]});
    service = TestBed.inject(ApmDataNormalizer);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should populate media', () => {
    const source: OccApmData = {
      name: 'tst-apm', code: 'tst-123',
      media: {url: '/image-url', code: 'test-image', mime: 'jpeg'}
    };

    const target = {} as ApmData;
    service.convert(source, target);

    expect(target.media['mobile'].url).toEqual('https://localhost:9002/image-url');
  });

  it('should populate properties', () => {
    const source: OccApmData = {
      name: 'tst-apm', code: 'tst-123',
      isRedirect: false,
      isUserDataRequired: true
    };

    const target = {} as ApmData;
    service.convert(source, target);

    expect(target.name).toEqual(source.name);
    expect(target.code).toEqual(source.code);
    expect(target.isUserDataRequired).toEqual(source.isUserDataRequired);
    expect(target.isRedirect).toEqual(source.isRedirect);
  })
});
