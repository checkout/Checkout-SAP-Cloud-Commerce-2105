import { TestBed } from '@angular/core/testing';

import { CheckoutComOccAdapter } from './checkout-com-occ.adapter';
import {
  Address,
  OccConfig,
  OccEndpointsService,
  OCC_USER_ID_ANONYMOUS,
  DynamicAttributes,
  BaseOccUrlProperties
} from '@spartacus/core';
import { HttpClientModule } from '@angular/common/http';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { defaultOccCheckoutComConfig } from './default-occ-checkout-com-config';
import { ApmPaymentDetails } from '../../../storefrontlib/interfaces';
import { PaymentType } from '../../model/ApmData';

const MockOccModuleConfig: OccConfig = {
  backend: {
    occ: {
      baseUrl: '',
      prefix: ''
    }
  },

  context: {
    baseSite: ['']
  }
};

class MockOccEndpointsService {
  buildUrl(endpoint: string, attributes?: DynamicAttributes, propertiesToOmit?: BaseOccUrlProperties) {
    const pattern = defaultOccCheckoutComConfig.backend.occ.endpoints[endpoint];
    let templateString = pattern;
    const urlParams = attributes?.hasOwnProperty('urlParams') ? attributes.urlParams : [];

    if (urlParams){

      Object.keys(urlParams).forEach((key) => {
        urlParams[key] = encodeURIComponent(urlParams[key]);
      });

      for (const variableLabel of Object.keys(urlParams)) {
        const placeholder = new RegExp('\\${' + variableLabel + '}', 'g');
        templateString = templateString.replace(
          placeholder,
          urlParams[variableLabel]
        );
      }
    }

    return templateString
  }
}

describe('CheckoutComOccAdapter', () => {
  let service: CheckoutComOccAdapter;
  let httpMock: HttpTestingController;
  let userId: string = 'current';
  let cartId: string = 'cartId';
  let occEndpointsService: OccEndpointsService;

  beforeEach(() => {

    TestBed.configureTestingModule({
      imports: [HttpClientModule, HttpClientTestingModule],
      providers: [
        {
          provide: OccConfig,
          useValue: MockOccModuleConfig
        },
        { provide: OccEndpointsService, useClass: MockOccEndpointsService},
      ]
    });
    service = TestBed.inject(CheckoutComOccAdapter);
    httpMock = TestBed.inject(HttpTestingController);
    occEndpointsService = TestBed.inject(OccEndpointsService);

    spyOn(occEndpointsService, 'buildUrl').and.callThrough();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getMerchantKey', () => {
    it('should return key', () => {
      service.getMerchantKey(userId).subscribe(res => expect(res).toEqual('pk_test_d4727781-a79c-460e-9773-05d762c63e8f'));
      expect(occEndpointsService.buildUrl).toHaveBeenCalledWith('merchantKey');

      const mockReq = httpMock.expectOne(req => {
        return (
          req.method === 'GET' &&
          req.urlWithParams ===
          `merchantKey`
        );
      });


      expect(mockReq.cancelled).toBeFalsy();
      expect(mockReq.request.responseType).toEqual('text');
      mockReq.flush('pk_test_d4727781-a79c-460e-9773-05d762c63e8f');
    });

    it('should return key for guest users', () => {
      service.getMerchantKey(OCC_USER_ID_ANONYMOUS).subscribe(res => expect(res).toEqual('pk_test_d4727781-a79c-460e-9773-05d762c63e8f'));

      const mockReq = httpMock.expectOne(req => {
        return (
          req.method === 'GET' &&
          req.urlWithParams === `merchantKey` &&
          !!req.headers.get('cx-use-client-token')
        );
      });

      expect(mockReq.cancelled).toBeFalsy();
      expect(mockReq.request.responseType).toEqual('text');
      mockReq.flush('pk_test_d4727781-a79c-460e-9773-05d762c63e8f');
    })
  });

  describe('setPaymentAddress', () => {
    it('should post the address', () => {
      const address = {
        id: '1234', country: {
          isocode: 'NL'
        }
      } as Address;

      service.setPaymentAddress(cartId, userId, address).subscribe();

      const mockReq = httpMock.expectOne(req => {
        return (
          req.method === 'POST' &&
          req.urlWithParams ===
          `users/current/carts/cartId/checkoutoccbillingaddress`
        );
      });

      expect(mockReq.cancelled).toBeFalsy();
      expect(mockReq.request.responseType).toEqual('json');
      mockReq.flush({});
    });

    it('should post the address for guest customer', () => {
      const address = {
        id: '1234', country: {
          isocode: 'NL'
        }
      } as Address;

      service.setPaymentAddress(cartId, OCC_USER_ID_ANONYMOUS, address).subscribe();

      const mockReq = httpMock.expectOne(req => {
        return (
          req.method === 'POST' &&
          req.urlWithParams ===
          `users/${OCC_USER_ID_ANONYMOUS}/carts/cartId/checkoutoccbillingaddress` &&
          !!req.headers.get('cx-use-client-token')
        );
      });

      expect(mockReq.cancelled).toBeFalsy();
      expect(mockReq.request.responseType).toEqual('json');
      mockReq.flush({});
    })
  });

  describe('getPaymentDetails', () => {
    it('should send request', () => {
      service.createPaymentDetails(userId, cartId,
        {
          cardBin: '424242',
          cardNumber: '4242424242424242'
        }).subscribe(res => expect(res).toBeTruthy());

      expect(occEndpointsService.buildUrl).toHaveBeenCalledWith('setPaymentDetails', {
        urlParams: {cartId, userId}
      });

      const mockReq = httpMock.expectOne(req => {
        return (
          req.method === 'POST' &&
          req.urlWithParams ===
          `users/current/carts/cartId/checkoutcompaymentdetails`
        );
      });

      expect(mockReq.cancelled).toBeFalsy();
      expect(mockReq.request.responseType).toEqual('json');
      mockReq.flush({});
    });
    it('should send request for guest customer', () => {
      service.createPaymentDetails(OCC_USER_ID_ANONYMOUS, cartId,
        {
          cardBin: '424242',
          cardNumber: '4242424242424242'
        }).subscribe(res => expect(res).toBeTruthy());

      const mockReq = httpMock.expectOne(req => {
        return (
          req.method === 'POST' &&
          req.urlWithParams ===
          `users/${OCC_USER_ID_ANONYMOUS}/carts/cartId/checkoutcompaymentdetails` &&
          !!req.headers.get('cx-use-client-token')
        );
      });

      expect(mockReq.cancelled).toBeFalsy();
      expect(mockReq.request.responseType).toEqual('json');
      mockReq.flush({});
    });
  });

  describe('createApmPaymentDetails', () => {
    it('should post the APM payment details', () => {
      const apmPaymentDetails = {
        type: PaymentType.iDeal,
        bic: 'INGNL2B'
      } as ApmPaymentDetails;

      service.createApmPaymentDetails(userId, cartId, apmPaymentDetails).subscribe();

      const mockReq = httpMock.expectOne(req => {
        return (
          req.method === 'POST' &&
          req.urlWithParams ===
          `users/current/carts/cartId/checkoutcomapmpaymentdetails`
        );
      });

      expect(mockReq.cancelled).toBeFalsy();
      expect(mockReq.request.responseType).toEqual('json');
      mockReq.flush({});
    });
    it('should post the APM payment details for guest customers', () => {
      const apmPaymentDetails = {
        type: PaymentType.iDeal,
        bic: 'INGNL2B'
      } as ApmPaymentDetails;

      service.createApmPaymentDetails(OCC_USER_ID_ANONYMOUS, cartId, apmPaymentDetails).subscribe();

      const mockReq = httpMock.expectOne(req => {
        return (
          req.method === 'POST' &&
          req.urlWithParams ===
          `users/${OCC_USER_ID_ANONYMOUS}/carts/cartId/checkoutcomapmpaymentdetails` &&
          !!req.headers.get('cx-use-client-token')
        );
      });

      expect(mockReq.cancelled).toBeFalsy();
      expect(mockReq.request.responseType).toEqual('json');
      mockReq.flush({});
    });
  });

  describe('placeOrder', () => {
    it('should place order', () => {
      service.placeOrder(userId, cartId, true).subscribe();

      const mockReq = httpMock.expectOne(req => {
        return (
          req.method === 'POST' &&
          req.urlWithParams ===
          `users/current/carts/cartId/direct-place-order?fields=FULL&termsChecked=true`
        );
      });

      expect(mockReq.cancelled).toBeFalsy();
      expect(mockReq.request.responseType).toEqual('json');
      mockReq.flush({});
    });

    it('should place order for guest', () => {
      service.placeOrder(OCC_USER_ID_ANONYMOUS, cartId, true).subscribe();

      const mockReq = httpMock.expectOne(req => {
        return (
          req.method === 'POST' &&
          req.urlWithParams ===
          `users/${OCC_USER_ID_ANONYMOUS}/carts/cartId/direct-place-order?fields=FULL&termsChecked=true` &&
          !!req.headers.get('cx-use-client-token')
        );
      });

      expect(mockReq.cancelled).toBeFalsy();
      expect(mockReq.request.responseType).toEqual('json');
      mockReq.flush({});
    });
  });

  describe('authorizeRedirectPlaceOrder', () => {
    it('should authorize redirect place order', () => {
      const sessionId = '1234';
      service.authorizeRedirectPlaceOrder(userId, cartId, sessionId).subscribe();

      const mockReq = httpMock.expectOne(req => {
        return (
          req.method === 'POST' &&
          req.urlWithParams ===
          `users/current/carts/cartId/redirect-place-order`
        );
      });

      expect(mockReq.cancelled).toBeFalsy();
      expect(mockReq.request.responseType).toEqual('json');
      mockReq.flush({});
    });
  });

  describe('requestAvailableApms', () => {
    it('should get available apms', () => {
      service.requestAvailableApms(userId, cartId).subscribe();

      const mockReq = httpMock.expectOne(req => {
        return (
          req.method === 'GET' &&
          req.urlWithParams ===
          `users/current/carts/cartId/apm/available`
        );
      });

      expect(mockReq.cancelled).toBeFalsy();
      expect(mockReq.request.responseType).toEqual('json');
      mockReq.flush({});
    });

    it('should get available apms for guest customers', () => {
      service.requestAvailableApms(OCC_USER_ID_ANONYMOUS, cartId).subscribe();

      const mockReq = httpMock.expectOne(req => {
        return (
          req.method === 'GET' &&
          req.urlWithParams ===
          `users/${OCC_USER_ID_ANONYMOUS}/carts/cartId/apm/available` &&
          !!req.headers.get('cx-use-client-token')
        );
      });

      expect(mockReq.cancelled).toBeFalsy();
      expect(mockReq.request.responseType).toEqual('json');
      mockReq.flush({});
    });
  });

  describe('Google Pay', () => {
    it('should get google merchant config', () => {
      service.getGooglePayMerchantConfiguration(userId, cartId).subscribe();

      const mockReq = httpMock.expectOne(req => {
        return (
          req.method === 'GET' &&
          req.urlWithParams ===
          `users/current/carts/cartId/google/merchant-configuration`
        );
      });

      expect(mockReq.cancelled).toBeFalsy();
      expect(mockReq.request.responseType).toEqual('json');
      mockReq.flush({});
    });

    it('should authorise payment', () => {
      const token = 'very long token';
      const billingAddress: Address = {
        firstName: 'John',
        lastName: 'Doe',
      };
      const saved = false;
      service.authoriseGooglePayPayment(userId, cartId, token, billingAddress, saved).subscribe();

      const mockReq = httpMock.expectOne(req => {
        return (
          req.method === 'POST' &&
          req.urlWithParams ===
          `users/current/carts/cartId/google/placeOrder`
        );
      });

      expect(mockReq.cancelled).toBeFalsy();
      expect(mockReq.request.responseType).toEqual('json');
      mockReq.flush({});
    });
  });

  describe('getKlarnaInitParams', () => {
    it('should get init klarna params', () => {
      service.getKlarnaInitParams(userId, cartId).subscribe();

      const mockReq = httpMock.expectOne(req => {
        return (
          req.method === 'GET' &&
          req.urlWithParams ===
          `users/current/carts/cartId/klarna/clientToken`
        );
      });

      expect(mockReq.cancelled).toBeFalsy();
      expect(mockReq.request.responseType).toEqual('json');
      mockReq.flush({});
    });
  });

  describe('Apple Pay', () => {
    it('should get payment request', () => {
      service.requestApplePayPaymentRequest(userId, cartId).subscribe();

      const mockReq = httpMock.expectOne(req => {
        return (
          req.method === 'GET' &&
          req.urlWithParams ===
          `users/current/carts/cartId/applepay/paymentRequest`
        );
      });

      expect(mockReq.cancelled).toBeFalsy();
      expect(mockReq.request.responseType).toEqual('json');
      mockReq.flush({});
    });

    it('should get merchant session', () => {
      const validationURL = 'very long text from apple';
      service.validateApplePayMerchant(userId, cartId, validationURL).subscribe();

      const mockReq = httpMock.expectOne(req => {
        return (
          req.method === 'POST' &&
          req.urlWithParams ===
          `users/current/carts/cartId/applepay/requestSession` &&
          req.body.validationURL === validationURL
        );
      });

      expect(mockReq.cancelled).toBeFalsy();
      expect(mockReq.request.responseType).toEqual('json');
      mockReq.flush({});
    });

    it('should validate payment', () => {
      const request = {secret: true};
      service.authorizeApplePayPayment(userId, cartId, request).subscribe();

      const mockReq = httpMock.expectOne(req => {
        return (
          req.method === 'POST' &&
          req.urlWithParams ===
          `users/current/carts/cartId/applepay/placeOrder` &&
          req.body.secret
        );
      });

      expect(mockReq.cancelled).toBeFalsy();
      expect(mockReq.request.responseType).toEqual('json');
      mockReq.flush({});
    });
  });
});
