import { TestBed } from '@angular/core/testing';

import { CheckoutComGooglepayService } from './checkout-com-googlepay.service';
import { ActiveCartService, UserIdService } from '@spartacus/core';
import { Store, StoreModule } from '@ngrx/store';
import { Observable, of } from 'rxjs';
import {
  AuthoriseGooglePayPayment,
  GetGooglePayMerchantConfiguration,
  GetGooglePayPaymentDataUpdate,
  GetGooglePayPaymentDataUpdateSuccess
} from '../../store/checkout-com.actions';
import {
  GooglePayMerchantConfiguration,
  GooglePayPaymentRequest,
  PaymentDataRequestUpdate,
  IntermediatePaymentData,
  CallbackTrigger,
  IntermediateAddress
} from '../../model/GooglePay';

class MockUserIdService {
  getUserId(): Observable<string> {
    return of('userId')
  }
}

class MockActiveCartService {
  getActiveCartId(): Observable<string> {
    return of('cartId')
  }
}

describe('CheckoutComGooglepayService', () => {
  let service: CheckoutComGooglepayService;
  let checkoutComStore;
  const userId = 'userId';
  const cartId = 'cartId';

  const merchantConfiguration: GooglePayMerchantConfiguration = {
    "baseCardPaymentMethod" : {
      "parameters" : {
        "allowedAuthMethods" : [ "PAN_ONLY", "CRYPTOGRAM_3DS" ],
        "allowedCardNetworks" : [ "AMEX", "DISCOVER", "MASTERCARD", "JCB", "VISA", "INTERAC" ],
        "billingAddressParameters" : {
          "format" : "FULL"
        },
        "billingAddressRequired" : true
      },
      "type" : "CARD"
    },
    "clientSettings" : {
      "environment" : "TEST"
    },
    "gateway" : "checkoutltd",
    "gatewayMerchantId" : "pk_test_c59321e8-953d-464d-bcfc-bb8785d05001",
    "merchantId" : "01234567890123456789",
    "merchantName" : "e2yCheckoutCom",
    "transactionInfo" : {
      "currencyCode" : "USD",
      "totalPrice" : '16.99',
      "totalPriceStatus" : "FINAL"
    }
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [StoreModule.forRoot({})],
      providers: [
        {provide: UserIdService, useClass: MockUserIdService},
        {provide: ActiveCartService, useClass: MockActiveCartService},
      ]
    });

    service = TestBed.inject(CheckoutComGooglepayService);

    checkoutComStore = TestBed.inject(Store);
    spyOn(checkoutComStore, 'dispatch').and.callThrough();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it ('should dispatch event on requestMerchantConfiguration', () => {
    service.requestMerchantConfiguration(userId, cartId);

    expect(checkoutComStore.dispatch).toHaveBeenCalledWith(
      new GetGooglePayMerchantConfiguration({
        userId,
        cartId
      })
    );
  });

  it('should authoriseOrder using payment request and billing address',() => {
    const paymentRequest: GooglePayPaymentRequest = {
      apiVersion: 2,
      apiVersionMinor: 1,
      paymentMethodData: {
        tokenizationData: {
          token: `{"fake": "tokenData"}`
        },
        info: {
          billingAddress: {
            mock: 'data'
          }
        }
      },
    };

    const billingAddress = paymentRequest.paymentMethodData.info.billingAddress;
    const token = JSON.parse(
      paymentRequest.paymentMethodData.tokenizationData.token
    );
    const savePaymentMethod = false;

    service.authoriseOrder(paymentRequest, savePaymentMethod, userId, cartId);

    expect(checkoutComStore.dispatch).toHaveBeenCalledWith(
      new AuthoriseGooglePayPayment({
        userId,
        cartId,
        billingAddress,
        token,
        savePaymentMethod,
        shippingAddress: undefined,
        email: undefined
      })
    );
  });

  it('should create initial payment request', () => {

    const result = service.createInitialPaymentRequest(merchantConfiguration, false);
    expect(result).toEqual({
      apiVersion: 2,
      apiVersionMinor: 0,
      allowedPaymentMethods: [
        merchantConfiguration.baseCardPaymentMethod
      ],

      shippingAddressRequired: false,
    });
  });

  it('should create full payment request',() => {
    const result = service.createFullPaymentRequest(merchantConfiguration);

    expect(result).toEqual({
      apiVersion: 2,
      apiVersionMinor: 0,
      allowedPaymentMethods: [
        {
          ...merchantConfiguration.baseCardPaymentMethod,
          tokenizationSpecification: {
            type: 'PAYMENT_GATEWAY',
            parameters: {
              gateway: merchantConfiguration.gateway,
              gatewayMerchantId: merchantConfiguration.gatewayMerchantId
            }
          }
        }
      ],
      merchantInfo: {
        merchantName: merchantConfiguration.merchantName,
        merchantId: merchantConfiguration.merchantId
      },
      transactionInfo: {
        ...merchantConfiguration.transactionInfo,
      }
    })
  });

  it('should updatePayment using intermediate payment requets ', () => {

    const intermediatePaymentRequest: IntermediatePaymentData = {
      callbackTrigger: CallbackTrigger.SHIPPING_ADDRESS,
      shippingAddress: {
        administrativeArea: 'adminitrationArea_test',
        countryCode: 'countryCode_test',
        locality: 'locality_test',
        postalCode: 'postalCode_test'
      },
      shippingOption: undefined
    };

    service.updatePaymentData(intermediatePaymentRequest, userId, cartId);

    expect(checkoutComStore.dispatch).toHaveBeenCalledWith(
      new GetGooglePayPaymentDataUpdate({
        userId,
        cartId,
        paymentData: intermediatePaymentRequest
      })
    );

  });

});
