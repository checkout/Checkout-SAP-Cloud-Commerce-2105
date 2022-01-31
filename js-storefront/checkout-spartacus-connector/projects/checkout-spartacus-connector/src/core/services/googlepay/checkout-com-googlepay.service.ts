import { Injectable } from '@angular/core';
import { StateWithCheckoutCom } from '../../store/checkout-com.state';
import { select, Store } from '@ngrx/store';
import { Observable } from 'rxjs';
import { GooglePayMerchantConfiguration, GooglePayPaymentRequest } from '../../model/GooglePay';
import { AuthoriseGooglePayPayment, GetGooglePayMerchantConfiguration } from '../../store/checkout-com.actions';
import { getGooglePayMerchantConfiguration } from '../../store/checkout-com.selectors';

@Injectable({
  providedIn: 'root'
})
export class CheckoutComGooglepayService {

  constructor(
    protected checkoutComStore: Store<StateWithCheckoutCom>
  ) { }

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
    const token = JSON.parse(
      paymentRequest.paymentMethodData.tokenizationData.token
    );

    this.checkoutComStore.dispatch(
      new AuthoriseGooglePayPayment({
        userId,
        cartId,
        token,
        billingAddress,
        savePaymentMethod
      })
    );
  }

  createInitialPaymentRequest(
    merchantConfiguration: GooglePayMerchantConfiguration
  ): any {
    return {
      apiVersion: 2,
      apiVersionMinor: 0,
      allowedPaymentMethods: [
        merchantConfiguration.baseCardPaymentMethod
      ]
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
}
