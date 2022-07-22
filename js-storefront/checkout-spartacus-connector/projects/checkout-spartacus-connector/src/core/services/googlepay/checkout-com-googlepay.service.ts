import { Injectable } from '@angular/core';
import { StateWithCheckoutCom } from '../../store/checkout-com.state';
import { select, Store } from '@ngrx/store';
import { Observable } from 'rxjs';
import {
  GooglePayMerchantConfiguration,
  GooglePayPaymentRequest,
  IntermediatePaymentData
} from '../../model/GooglePay';
import {
  AuthoriseGooglePayPayment,
  GetGooglePayMerchantConfiguration,
  GetGooglePayPaymentDataUpdate
} from '../../store/checkout-com.actions';
import {
  getGooglePayMerchantConfiguration,
  getGooglePayPaymentAuthorizationResult,
  getGooglePayPaymentDataUpdate
} from '../../store/checkout-com.selectors';
import { first } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class CheckoutComGooglepayService {

  constructor(
    protected checkoutComStore: Store<StateWithCheckoutCom>
  ) {
  }

  /**
   * Retrieve the google pay merchant configuration from state
   */
  getMerchantConfigurationFromState(): Observable<GooglePayMerchantConfiguration> {
    return this.checkoutComStore.pipe(select(getGooglePayMerchantConfiguration));
  }

  /**
   * Dispatch call to request merchant information from OCC
   */
  public requestMerchantConfiguration(userId: string, cartId: string) {
    this.checkoutComStore.dispatch(
      new GetGooglePayMerchantConfiguration({userId, cartId})
    );
  }

  authoriseOrder(
    paymentRequest: GooglePayPaymentRequest,
    savePaymentMethod: boolean,
    userId: string,
    cartId: string,
  ) {
    const billingAddress = paymentRequest.paymentMethodData.info.billingAddress;
    const shippingAddress = paymentRequest.shippingAddress;
    const email = paymentRequest.email;
    const token = JSON.parse(
      paymentRequest.paymentMethodData.tokenizationData.token
    );

    this.checkoutComStore.dispatch(
      new AuthoriseGooglePayPayment({
        userId,
        cartId,
        token,
        billingAddress,
        savePaymentMethod,
        shippingAddress,
        email
      })
    );
  }

  updatePaymentData(
    paymentData: IntermediatePaymentData,
    userId: string,
    cartId: string,
  ) {
    this.checkoutComStore.dispatch(new GetGooglePayPaymentDataUpdate({
      cartId,
      userId,
      paymentData,
    }));
  }

  createInitialPaymentRequest(
    merchantConfiguration: GooglePayMerchantConfiguration,
    shippingAddressRequired: boolean,
  ): any {
    return {
      apiVersion: 2,
      apiVersionMinor: 0,
      allowedPaymentMethods: [
        merchantConfiguration.baseCardPaymentMethod
      ],
      shippingAddressRequired,
    };
  }

  createFullPaymentRequest(
    merchantConfiguration: GooglePayMerchantConfiguration
  ): any {
    return {
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
        merchantName: merchantConfiguration.merchantName || '',
        merchantId: merchantConfiguration.merchantId || ''
      },
      transactionInfo: {
        ...merchantConfiguration.transactionInfo,
        // totalPrice: `${merchantConfiguration.transactionInfo.totalPrice}`,
      }
    };
  }

  addPaymentExpressIntents(paymentRequest) {
    return {
      ...paymentRequest,

      callbackIntents: [
        'SHIPPING_ADDRESS',
        'SHIPPING_OPTION',
      ],
      shippingAddressRequired: true,
      emailRequired: true,
      shippingAddressParameters: {},
      shippingOptionRequired: true,
    }
  }

  onPaymentAuthorized(cartId: string, userId: string, paymentData: GooglePayPaymentRequest): Promise<any> {
    this.authoriseOrder(paymentData,false,userId,cartId);

    return this.checkoutComStore.pipe(
      select(getGooglePayPaymentAuthorizationResult),
      first(update => update !== undefined && Object.keys(update).length > 0)
    ).toPromise();
  }

  onPaymentDataChanged(cartId: string, userId: string, paymentData: IntermediatePaymentData): Promise<any> {
    this.updatePaymentData(paymentData, userId, cartId);

    return this.checkoutComStore.pipe(
      select(getGooglePayPaymentDataUpdate),
      first(update => update !== undefined && Object.keys(update).length > 0)
    ).toPromise();
  }

}
