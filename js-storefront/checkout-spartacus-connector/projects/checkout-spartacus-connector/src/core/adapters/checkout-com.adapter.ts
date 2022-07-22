import { Observable } from 'rxjs';
import { Address, Order, PaymentDetails } from '@spartacus/core';
import { ApmPaymentDetails, CheckoutComPaymentDetails } from '../../storefrontlib/interfaces';
import { ApmData } from '../model/ApmData';
import {
  GooglePayMerchantConfiguration,
  IntermediatePaymentData,
  PaymentDataRequestUpdate,
  PlaceOrderResponse
} from '../model/GooglePay';
import { KlarnaInitParams } from '../interfaces';
import {
  ApplePayAuthorization,
  ApplePayPaymentContact,
  ApplePayPaymentRequest,
  ApplePayShippingContactUpdate,
  ApplePayShippingMethod, ApplePayShippingMethodUpdate
} from '../model/ApplePay';

export abstract class CheckoutComAdapter {

  /**
   * Get the merchant key for the current base store
   */
  abstract getMerchantKey(userId: string): Observable<string>;

  /**
   * Set the payment address to the given cart
   * @param cartId current cart id
   * @param userId current user id
   * @param address payment address
   */
  abstract setPaymentAddress(cartId: string, userId: string, address: Address): Observable<any>;

  /**
   * Set the payment details item
   * @param userId current user id
   * @param cartId current cart id
   * @param paymentDetails item content
   */
  abstract createPaymentDetails(userId: string, cartId: string, paymentDetails: CheckoutComPaymentDetails): Observable<PaymentDetails>;

  /**
   * Create the APM Payment Details
   * @param userId current user id
   * @param cartId current cart id
   * @param paymentDetails ApmPaymentDetails
   */
  abstract createApmPaymentDetails(userId: string, cartId: string, paymentDetails: ApmPaymentDetails): Observable<PaymentDetails>;

  /**
   * Place order. Might return a redirect
   * @param userId user id
   * @param cartId cart id
   * @param termsChecked is the terms and conditions are checked. needs validation in backend
   */
  abstract placeOrder(userId: string, cartId: string, termsChecked: boolean): Observable<Order>;

  /**
   * Authorize place order after redirect
   *
   * @param userId current user id
   * @param cartId current cart id
   * @param sessionId checkout.com session id
   */
  abstract authorizeRedirectPlaceOrder(userId: string, cartId: string, sessionId: string): Observable<Order>;

  /**
   * Get all available APM's for the given user and cart
   *
   * @param userId user id
   * @param cartId cart id
   */
  abstract requestAvailableApms(userId: string, cartId: string): Observable<ApmData[]>;

  /**
   * Request current merchant configuration
   *
   * @param userId current user
   * @param cartId current cart
   */
  abstract getGooglePayMerchantConfiguration(
    userId: string,
    cartId: string
  ): Observable<GooglePayMerchantConfiguration>;

  /**
   * Authorise GooglePay payment
   * @param userId current user
   * @param cartId current cart
   * @param token tokenized payment details
   * @param billingAddress billing address
   * @param savePaymentMethod save payment method or not
   * @param shippingAddress optional shipping address (required for GooglePay express button)
   */
  abstract authoriseGooglePayPayment(
    userId: string,
    cartId: string,
    token: string,
    billingAddress: any,
    savePaymentMethod: boolean,
    shippingAddress: any,
  ): Observable<PlaceOrderResponse>;

  abstract requestApplePayPaymentRequest(
    userId: string,
    cartId: string,
    productCode?: string
  ): Observable<ApplePayPaymentRequest>;

  abstract validateApplePayMerchant(
    userId: string,
    cartId: string,
    validationURL: string
  ): Observable<any>;

  abstract authorizeApplePayPayment(
    userId: string,
    cartId: string,
    request: any
  ): Observable<ApplePayAuthorization>;

  /**
   * Request Klarna init params - clientToken, paymentMethodCategories
   * @param userId - user ID
   * @param cartId - cart ID
   */
  abstract getKlarnaInitParams(userId: string, cartId: string): Observable<KlarnaInitParams>;

  /**
   * Set the delivery address given the shipping contact from ApplePay
   *
   * @param userId
   * @param cartId
   * @param shippingContact
   */
  abstract selectApplePayDeliveryAddress(
    userId: string,
    cartId: string,
    shippingContact: ApplePayPaymentContact
  ): Observable<ApplePayShippingContactUpdate>

  /**
   * Set the delivery method (Shipping Method) given the shipping Method
   *
   * @param userId
   * @param cartId
   * @param shippingMethod
   */
  abstract selectApplePayDeliveryMethod(
    userId: string,
    cartId: string,
    shippingMethod: ApplePayShippingMethod
  ): Observable<ApplePayShippingMethodUpdate>;

  /**
   * Set the delivery address for GooglePay and get updated order lines back
   *
   * @param userId
   * @param cartId
   * @param paymentData
   */
  abstract setGooglePayDeliveryInfo(
    userId: string,
    cartId: string,
    paymentData: IntermediatePaymentData,
  ): Observable<PaymentDataRequestUpdate>;
}
