import {Injectable} from '@angular/core';
import {
  Address,
  ConverterService,
  InterceptorUtil,
  Occ,
  OCC_USER_ID_ANONYMOUS,
  OccEndpointsService,
  Order,
  ORDER_NORMALIZER,
  PAYMENT_DETAILS_NORMALIZER,
  PaymentDetails,
  USE_CLIENT_TOKEN,
} from '@spartacus/core';
import {PAYMENT_DETAILS_SERIALIZER} from '@spartacus/checkout/core';
import {CheckoutComAdapter} from '../checkout-com.adapter';
import {Observable} from 'rxjs';
import {HttpClient, HttpHeaders, HttpParams} from '@angular/common/http';
import {ApmPaymentDetails, CheckoutComPaymentDetails} from '../../../storefrontlib/interfaces';
import {map, pluck} from 'rxjs/operators';
import {ApmData, AvailableApmResponseData} from '../../model/ApmData';
import {APM_NORMALIZER, APM_PAYMENT_DETAILS_NORMALIZER} from '../converters';
import {
  GooglePayMerchantConfiguration,
  IntermediatePaymentData,
  PaymentDataRequestUpdate,
  PlaceOrderResponse
} from '../../model/GooglePay';
import {KlarnaInitParams} from '../../interfaces';
import {
  ApplePayAuthorization,
  ApplePayPaymentContact,
  ApplePayPaymentRequest,
  ApplePayShippingContactUpdate,
  ApplePayShippingMethod,
  ApplePayShippingMethodUpdate
} from '../../model/ApplePay';

@Injectable({
  providedIn: 'root'
})
export class CheckoutComOccAdapter implements CheckoutComAdapter {

  constructor(protected http: HttpClient,
              protected occEndpoints: OccEndpointsService,
              protected converter: ConverterService,
  ) {
  }

  getMerchantKey(userId: string): Observable<string> {
    return this.http.get<string>(
      this.occEndpoints.buildUrl('merchantKey'),
      {
        responseType: 'text' as 'json',
        headers: this.getHeadersForUserId(userId)
      }
    );
  }

  setPaymentAddress(cartId: string, userId: string, address: Address): Observable<any> {
    return this.http.post(
      this.occEndpoints.buildUrl('setPaymentAddress', {
        urlParams: {cartId, userId},
      }),
      {
        ...address
      },
      {headers: this.getHeadersForUserId(userId)}
    );
  }

  createPaymentDetails(userId: string, cartId: string, paymentDetails: CheckoutComPaymentDetails): Observable<PaymentDetails> {
    return this.http.post<CheckoutComPaymentDetails>(
      this.occEndpoints.buildUrl('setPaymentDetails', {
        urlParams: {cartId, userId},
      }),
      this.converter.convert(paymentDetails, PAYMENT_DETAILS_SERIALIZER),
      {headers: this.getHeadersForUserId(userId)}
    ).pipe(map((response) => this.converter.convert(response, PAYMENT_DETAILS_NORMALIZER)));
  }

  createApmPaymentDetails(userId: string, cartId: string, paymentDetails: ApmPaymentDetails): Observable<PaymentDetails> {
    return this.http.post<ApmPaymentDetails>(
      this.occEndpoints.buildUrl('setApmPaymentDetails', {
        urlParams: {cartId, userId},
      }),
      paymentDetails,
      {headers: this.getHeadersForUserId(userId)}
    ).pipe(map(_ => this.converter.convert(paymentDetails, APM_PAYMENT_DETAILS_NORMALIZER)));
  }

  placeOrder(userId: string, cartId: string, termsChecked: boolean): Observable<Order> {
    const params = new HttpParams()
      .set('fields', 'FULL')
      .set('termsChecked', termsChecked.toString());

    return this.http
      .post<Occ.Order>(
        this.occEndpoints.buildUrl('directPlaceOrder', {
          urlParams: {cartId, userId},
        }),
        {},
        {headers: this.getHeadersForUserId(userId), params}
      )
      .pipe(this.converter.pipeable(ORDER_NORMALIZER));
  }

  authorizeRedirectPlaceOrder(userId: string, cartId: string, sessionId: string): Observable<Order> {
    return this.http.post<Occ.Order>(
      this.occEndpoints.buildUrl(`redirectPlaceOrder`, {
        urlParams: {cartId, userId},
      }),
      {'cko-session-id': sessionId},
      {headers: this.getHeadersForUserId(userId)}
    ).pipe(this.converter.pipeable(ORDER_NORMALIZER));
  }

  requestAvailableApms(userId: string, cartId: string): Observable<ApmData[]> {
    return this.http.get<AvailableApmResponseData>(
      this.occEndpoints.buildUrl('availableApms', {
        urlParams: {cartId, userId},
      }),
      {headers: this.getHeadersForUserId(userId)}
    ).pipe(
      pluck('availableApmConfigurations'),
      this.converter.pipeableMany(APM_NORMALIZER));
  }

  getGooglePayMerchantConfiguration(
    userId: string,
    cartId: string
  ): Observable<GooglePayMerchantConfiguration> {
    return this.http.get<GooglePayMerchantConfiguration>(
      this.occEndpoints.buildUrl(
        `googlePayMerchantConfig`,
        {
          urlParams: {cartId, userId},
        },
      ),
      {headers: this.getHeadersForUserId(userId)}
    );
  }

  authoriseGooglePayPayment(
    userId: string,
    cartId: string,
    token: string,
    billingAddress: any,
    saved: boolean,
    shippingAddress?: any,
    email?: string
  ): Observable<PlaceOrderResponse> {
    return this.http.post<PlaceOrderResponse>(
      this.occEndpoints.buildUrl('googlePayPlaceOrder', {
        urlParams: {userId, cartId},
      }),
      {
        token,
        billingAddress,
        saved,
        shippingAddress,
        email
      },
      {headers: this.getHeadersForUserId(userId)}
    );
  }

  getKlarnaInitParams(userId: string, cartId: string): Observable<KlarnaInitParams> {
    return this.http.get(
      this.occEndpoints.buildUrl('klarnaClientToken', {
        urlParams: {cartId, userId},
      }),
      {headers: this.getHeadersForUserId(userId)});
  }

  public requestApplePayPaymentRequest(
    userId: string,
    cartId: string,
  ): Observable<ApplePayPaymentRequest> {
    return this.http.get<ApplePayPaymentRequest>(
      this.occEndpoints.buildUrl('applePayPaymentRequest', {
        urlParams: {cartId, userId}
      }),
      {headers: this.getHeadersForUserId(userId)},
    );
  }

  validateApplePayMerchant(
    userId: string,
    cartId: string,
    validationURL: string
  ): Observable<any> {
    return this.http.post<any>(
      this.occEndpoints.buildUrl('applePayRequestSession', {
        urlParams: {cartId, userId},
      }),
      {
        validationURL
      },
      {headers: this.getHeadersForUserId(userId)}
    );
  }

  authorizeApplePayPayment(
    userId: string,
    cartId: string,
    request: any
  ): Observable<ApplePayAuthorization> {
    return this.http.post<ApplePayAuthorization>(
      this.occEndpoints.buildUrl('applePayPlaceOrder', {
        urlParams: {cartId, userId},
      }),
      {
        ...request
      },
      {headers: this.getHeadersForUserId(userId)}
    );
  }

  selectApplePayDeliveryAddress(userId: string, cartId: string, shippingContact: ApplePayPaymentContact): Observable<ApplePayShippingContactUpdate> {
    return this.http.post<ApplePayShippingContactUpdate>(
      this.occEndpoints.buildUrl('applePaySetDeliveryAddress', {
        urlParams: {cartId, userId},
      }),
      {
        ...shippingContact
      },
      {headers: this.getHeadersForUserId(userId)}
    );
  }

  selectApplePayDeliveryMethod(userId: string, cartId: string, shippingMethod: ApplePayShippingMethod): Observable<ApplePayShippingMethodUpdate> {
    return this.http.post<ApplePayShippingMethodUpdate>(
      this.occEndpoints.buildUrl('applePaySetDeliveryMethod', {
        urlParams: {cartId, userId},
      }),
      {
        ...shippingMethod
      },
      {headers: this.getHeadersForUserId(userId)}
    );
  }

  setGooglePayDeliveryInfo(userId: string, cartId: string, paymentData: IntermediatePaymentData): Observable<PaymentDataRequestUpdate> {
    return this.http.post<PaymentDataRequestUpdate>(
      this.occEndpoints.buildUrl('googlePaySetDeliveryInfo', {
        urlParams: {cartId, userId},
      }),
      {
        ...paymentData
      },
      {headers: this.getHeadersForUserId(userId)}
    );
  }

  protected getHeadersForUserId(userId: string, contentType: string = 'application/json') {
    let headers = new HttpHeaders({
      'Content-Type': contentType,
    });
    if (userId === OCC_USER_ID_ANONYMOUS) {
      headers = InterceptorUtil.createHeader(USE_CLIENT_TOKEN, true, headers);
    }
    return headers;
  }
}
