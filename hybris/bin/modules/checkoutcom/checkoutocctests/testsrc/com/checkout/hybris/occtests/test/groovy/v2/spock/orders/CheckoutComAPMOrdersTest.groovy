package com.checkout.hybris.occtests.test.groovy.v2.spock.orders

import com.checkout.hybris.occtests.test.groovy.v2.spock.paymentdetails.AbstractCheckoutComPaymentsTest
import de.hybris.bootstrap.annotations.ManualTest
import org.apache.commons.lang.StringUtils
import spock.lang.Unroll

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.*
import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.ContentType.XML
import static org.apache.http.HttpStatus.SC_BAD_REQUEST
import static org.apache.http.HttpStatus.SC_CREATED

@ManualTest
@Unroll
class CheckoutComAPMOrdersTest extends AbstractCheckoutComPaymentsTest {

    def "Authorized customer gets redirect url when places an order in ABC Site with #currency currency with a billing address from #billingCountry with #APM: #format"() {
        given: "authorized customer"
        def customerWithCart = createAndAuthorizeCustomerWithCart(restClient, format)
        def customer = customerWithCart[0]
        def cart = customerWithCart[1]
        def address = createAddress(restClient, customer)
        setDeliveryAddressForCart(restClient, customer, cart.code, address.id, format)
        addProductToCartOnline(restClient, customer, cart.code, PRODUCT_POWER_SHOT_A480)
        setDeliveryModeForCart(restClient, customer, cart.code, DELIVERY_STANDARD, format)
        createBillingAddress(customer.id, cart.code, billingAddressPostBody)
        createAPMPaymentInfo(restClient, customer, cart.code, paymentInfoPostBody)

        when: "authorized customer places order"
        def response = placeCheckoutComOrder(customer, cart.code, currency, format)

        then: "customer is redirected to #APM"
        with(response) {
            if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
            status == SC_CREATED
            isNotEmpty(data.redirectUrl)
            StringUtils.contains((String) data.redirectUrl, getConfigurationProperty(redirectKey))
        }

        where:
        format | APM        | currency          | billingCountry | paymentInfoPostBody                      | billingAddressPostBody        | redirectKey
        JSON   | MULTIBANCO | EUR_CURRENCY_CODE | 'Portugal'     | DEFAULT_CHECKOUT_MULTIBANCO_PAYMENT_JSON | PORTUGAL_BILLING_ADDRESS_JSON | 'checkoutocctests.checkout.multibanco.sandbox'
        XML    | MULTIBANCO | EUR_CURRENCY_CODE | 'Portugal'     | DEFAULT_CHECKOUT_MULTIBANCO_PAYMENT_JSON | PORTUGAL_BILLING_ADDRESS_JSON | 'checkoutocctests.checkout.multibanco.sandbox'
        JSON   | PAYPAL     | EUR_CURRENCY_CODE | 'Germany'      | DEFAULT_CHECKOUT_PAYPAL_PAYMENT_JSON     | DEFAULT_BILLING_ADDRESS_JSON  | 'checkoutocctests.checkout.paypal.sandbox'
        XML    | PAYPAL     | EUR_CURRENCY_CODE | 'Germany'      | DEFAULT_CHECKOUT_PAYPAL_PAYMENT_JSON     | DEFAULT_BILLING_ADDRESS_JSON  | 'checkoutocctests.checkout.paypal.sandbox'
        JSON   | SOFORT     | EUR_CURRENCY_CODE | 'Germany'      | DEFAULT_CHECKOUT_SOFORT_PAYMENT_JSON     | DEFAULT_BILLING_ADDRESS_JSON  | 'checkoutocctests.checkout.sofort.sandbox'
        XML    | SOFORT     | EUR_CURRENCY_CODE | 'Germany'      | DEFAULT_CHECKOUT_SOFORT_PAYMENT_JSON     | DEFAULT_BILLING_ADDRESS_JSON  | 'checkoutocctests.checkout.sofort.sandbox'
        JSON   | BANCONTACT | EUR_CURRENCY_CODE | 'Belgium'      | DEFAULT_CHECKOUT_BANCONTACT_PAYMENT_JSON | BELGIUM_BILLING_ADDRESS_JSON  | 'checkoutocctests.checkout.bancontact.sandbox'
        XML    | BANCONTACT | EUR_CURRENCY_CODE | 'Belgium'      | DEFAULT_CHECKOUT_BANCONTACT_PAYMENT_JSON | BELGIUM_BILLING_ADDRESS_JSON  | 'checkoutocctests.checkout.bancontact.sandbox'
//        JSON   | KNET       | KWD_CURRENCY_CODE | 'Kuwait'       | DEFAULT_CHECKOUT_KNET_PAYMENT_JSON       | KW_BILLING_ADDRESS_JSON       | 'checkoutocctests.checkout.knet.sandbox'
//        XML    | KNET       | KWD_CURRENCY_CODE | 'Kuwait'       | DEFAULT_CHECKOUT_KNET_PAYMENT_JSON       | KW_BILLING_ADDRESS_JSON       | 'checkoutocctests.checkout.knet.sandbox'
        JSON   | POLI       | AUD_CURRENCY_CODE | 'Australia'    | DEFAULT_CHECKOUT_POLI_PAYMENT_JSON       | AU_BILLING_ADDRESS_JSON       | 'checkoutocctests.checkout.poli.sandbox'
        XML    | POLI       | AUD_CURRENCY_CODE | 'Australia'    | DEFAULT_CHECKOUT_POLI_PAYMENT_JSON       | AU_BILLING_ADDRESS_JSON       | 'checkoutocctests.checkout.poli.sandbox'
//        JSON   | QPAY       | QAR_CURRENCY_CODE | 'Qatar'        | DEFAULT_CHECKOUT_QPAY_PAYMENT_JSON       | QATAR_BILLING_ADDRESS_JSON    | 'checkoutocctests.checkout.qpay.sandbox'
//        XML    | QPAY       | QAR_CURRENCY_CODE | 'Qatar'        | DEFAULT_CHECKOUT_QPAY_PAYMENT_JSON       | QATAR_BILLING_ADDRESS_JSON    | 'checkoutocctests.checkout.qpay.sandbox'
        JSON   | ALIPAY     | USD_CURRENCY_CODE | 'USA'          | DEFAULT_CHECKOUT_ALIPAY_PAYMENT_JSON     | US_BILLING_ADDRESS_JSON       | 'checkoutocctests.checkout.alipay.sandbox'
        XML    | ALIPAY     | USD_CURRENCY_CODE | 'USA'          | DEFAULT_CHECKOUT_ALIPAY_PAYMENT_JSON     | US_BILLING_ADDRESS_JSON       | 'checkoutocctests.checkout.alipay.sandbox'
        JSON   | ALIPAY     | USD_CURRENCY_CODE | 'China'        | DEFAULT_CHECKOUT_ALIPAY_PAYMENT_JSON     | CN_BILLING_ADDRESS_JSON       | 'checkoutocctests.checkout.alipay.sandbox'
        XML    | ALIPAY     | USD_CURRENCY_CODE | 'China'        | DEFAULT_CHECKOUT_ALIPAY_PAYMENT_JSON     | CN_BILLING_ADDRESS_JSON       | 'checkoutocctests.checkout.alipay.sandbox'
        JSON   | P24        | EUR_CURRENCY_CODE | 'Germany'      | DEFAULT_CHECKOUT_P24_PAYMENT_JSON        | POLAND_BILLING_ADDRESS_JSON   | 'checkoutocctests.checkout.p24.sandbox'
        XML    | P24        | EUR_CURRENCY_CODE | 'Germany'      | DEFAULT_CHECKOUT_P24_PAYMENT_JSON        | POLAND_BILLING_ADDRESS_JSON   | 'checkoutocctests.checkout.p24.sandbox'
        JSON   | IDEAL      | EUR_CURRENCY_CODE | 'Netherlands'  | DEFAULT_CHECKOUT_IDEAL_PAYMENT_JSON      | NL_BILLING_ADDRESS_JSON       | 'checkoutocctests.checkout.ideal.sandbox'
        XML    | IDEAL      | EUR_CURRENCY_CODE | 'Netherlands'  | DEFAULT_CHECKOUT_IDEAL_PAYMENT_JSON      | NL_BILLING_ADDRESS_JSON       | 'checkoutocctests.checkout.ideal.sandbox'
        JSON   | EPS        | EUR_CURRENCY_CODE | 'Austria'      | DEFAULT_CHECKOUT_EPS_PAYMENT_JSON        | AT_BILLING_ADDRESS_JSON       | 'checkoutocctests.checkout.eps.sandbox'
        XML    | EPS        | EUR_CURRENCY_CODE | 'Austria'      | DEFAULT_CHECKOUT_EPS_PAYMENT_JSON        | AT_BILLING_ADDRESS_JSON       | 'checkoutocctests.checkout.eps.sandbox'
        JSON   | OXXO       | MXN_CURRENCY_CODE | 'Mexico'       | DEFAULT_CHECKOUT_OXXO_PAYMENT_JSON       | MX_BILLING_ADDRESS_JSON       | 'checkoutocctests.checkout.oxxo.sandbox'
        XML    | OXXO       | MXN_CURRENCY_CODE | 'Mexico'       | DEFAULT_CHECKOUT_OXXO_PAYMENT_JSON       | MX_BILLING_ADDRESS_JSON       | 'checkoutocctests.checkout.oxxo.sandbox'
    }

    def "Authorized customer gets redirect url when places an order in NAS Site with #currency currency with a billing address from #billingCountry with #APM: #format"() {
        given: "authorized customer"
        def customerWithCart = createAndAuthorizeCustomerWithCart(restClient, format)
        def customer = customerWithCart[0]
        def cart = customerWithCart[1]
        def address = createAddress(restClient, customer)
        setDeliveryAddressForCart(restClient, customer, cart.code, address.id, format)
        addProductToCartOnline(restClient, customer, cart.code, PRODUCT_POWER_SHOT_A480)
        setDeliveryModeForCart(restClient, customer, cart.code, DELIVERY_STANDARD, format)
        createBillingAddress(customer.id, cart.code, billingAddressPostBody)
        createAPMPaymentInfo(restClient, customer, cart.code, paymentInfoPostBody)
        useNAS()

        when: "authorized customer places order"
        def response = placeCheckoutComOrder(customer, cart.code, currency, format)

        then: "customer is redirected to #APM"
        with(response) {
            if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
            status == SC_CREATED
            isNotEmpty(data.redirectUrl)
            StringUtils.contains((String) data.redirectUrl, getConfigurationProperty(redirectKey))
        }

        where:
        format | APM        | currency          | billingCountry | paymentInfoPostBody                      | billingAddressPostBody       | redirectKey
        JSON   | GIROPAY    | EUR_CURRENCY_CODE | 'Germany'      | DEFAULT_CHECKOUT_GIROPAY_PAYMENT_JSON    | DEFAULT_BILLING_ADDRESS_JSON | 'checkoutocctests.checkout.giropay.sandbox'
        XML    | GIROPAY    | EUR_CURRENCY_CODE | 'Germany'      | DEFAULT_CHECKOUT_GIROPAY_PAYMENT_JSON    | DEFAULT_BILLING_ADDRESS_JSON | 'checkoutocctests.checkout.giropay.sandbox'
        JSON   | SOFORT     | EUR_CURRENCY_CODE | 'Germany'      | DEFAULT_CHECKOUT_SOFORT_PAYMENT_JSON     | DEFAULT_BILLING_ADDRESS_JSON | 'checkoutocctests.checkout.sofort.nas.sandbox'
        XML    | SOFORT     | EUR_CURRENCY_CODE | 'Germany'      | DEFAULT_CHECKOUT_SOFORT_PAYMENT_JSON     | DEFAULT_BILLING_ADDRESS_JSON | 'checkoutocctests.checkout.sofort.nas.sandbox'
        JSON   | P24        | EUR_CURRENCY_CODE | 'Germany'      | DEFAULT_CHECKOUT_P24_PAYMENT_JSON        | POLAND_BILLING_ADDRESS_JSON  | 'checkoutocctests.checkout.p24.sandbox'
        XML    | P24        | EUR_CURRENCY_CODE | 'Germany'      | DEFAULT_CHECKOUT_P24_PAYMENT_JSON        | POLAND_BILLING_ADDRESS_JSON  | 'checkoutocctests.checkout.p24.sandbox'
        JSON   | IDEAL      | EUR_CURRENCY_CODE | 'Netherlands'  | DEFAULT_CHECKOUT_IDEAL_PAYMENT_JSON      | NL_BILLING_ADDRESS_JSON      | 'checkoutocctests.checkout.ideal.sandbox'
        XML    | IDEAL      | EUR_CURRENCY_CODE | 'Netherlands'  | DEFAULT_CHECKOUT_IDEAL_PAYMENT_JSON      | NL_BILLING_ADDRESS_JSON      | 'checkoutocctests.checkout.ideal.sandbox '
        JSON   | EPS        | EUR_CURRENCY_CODE | 'Austria'      | DEFAULT_CHECKOUT_EPS_PAYMENT_JSON        | AT_BILLING_ADDRESS_JSON      | 'checkoutocctests.checkout.eps.sandbox'
        XML    | EPS        | EUR_CURRENCY_CODE | 'Austria'      | DEFAULT_CHECKOUT_EPS_PAYMENT_JSON        | AT_BILLING_ADDRESS_JSON      | 'checkoutocctests.checkout.eps.sandbox'
        JSON   | BANCONTACT | EUR_CURRENCY_CODE | 'Belgium'      | DEFAULT_CHECKOUT_BANCONTACT_PAYMENT_JSON | BELGIUM_BILLING_ADDRESS_JSON | 'checkoutocctests.checkout.bancontact.sandbox'
        XML    | BANCONTACT | EUR_CURRENCY_CODE | 'Belgium'      | DEFAULT_CHECKOUT_BANCONTACT_PAYMENT_JSON | BELGIUM_BILLING_ADDRESS_JSON | 'checkoutocctests.checkout.bancontact.sandbox'
    }

    def "Authorized customer places an order with #currency currency with a billing address from #billingCountry with #APM: #format"() {
        given: "authorized customer"
        def customerWithCart = createAndAuthorizeCustomerWithCart(restClient, format)
        def customer = customerWithCart[0]
        def cart = customerWithCart[1]
        def address = createAddress(restClient, customer)
        setDeliveryAddressForCart(restClient, customer, cart.code, address.id, format)
        addProductToCartOnline(restClient, customer, cart.code, PRODUCT_POWER_SHOT_A480)
        setDeliveryModeForCart(restClient, customer, cart.code, DELIVERY_STANDARD, format)
        createBillingAddress(customer.id, cart.code, billingAddressPostBody)
        createAPMPaymentInfo(restClient, customer, cart.code, paymentInfoPostBody)

        when: "authorized customer places order"
        def response = placeCheckoutComOrder(customer, cart.code, currency, format)

        then: "the order is placed"
        with(response) {
            if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
            status == SC_CREATED
            isNotEmpty(data.code)
            data.paymentType == APM.name()
            if (data.paymentType == BENEFITPAY.name()) {
                isNotEmpty(data.qrCodeData)
            }
        }

        where:
        format | APM   | currency          | billingCountry | paymentInfoPostBody                 | billingAddressPostBody
        JSON   | FAWRY | EGP_CURRENCY_CODE | 'Germany'      | DEFAULT_CHECKOUT_FAWRY_PAYMENT_JSON | DEFAULT_BILLING_ADDRESS_JSON
        XML    | FAWRY | EGP_CURRENCY_CODE | 'Germany'      | DEFAULT_CHECKOUT_FAWRY_PAYMENT_JSON | DEFAULT_BILLING_ADDRESS_JSON
//        JSON   | BENEFITPAY | BHD_CURRENCY_CODE | 'Bahrain'      | DEFAULT_CHECKOUT_BENEFIT_PAYMENT_JSON | BH_BILLING_ADDRESS_JSON
//        XML    | BENEFITPAY | BHD_CURRENCY_CODE | 'Bahrain'      | DEFAULT_CHECKOUT_BENEFIT_PAYMENT_JSON | BH_BILLING_ADDRESS_JSON
//        JSON   | SEPA       | EUR_CURRENCY_CODE | 'Austria'      | DEFAULT_CHECKOUT_SEPA_PAYMENT_JSON    | AT_BILLING_ADDRESS_JSON
//        XML    | SEPA       | EUR_CURRENCY_CODE | 'Austria'      | DEFAULT_CHECKOUT_SEPA_PAYMENT_JSON    | AT_BILLING_ADDRESS_JSON
    }

    def "Authorized customer can't place an order with #currency currency with a billing address from #billingCountry with #APM: #format"() {
        given: "authorized customer"
        def customerWithCart = createAndAuthorizeCustomerWithCart(restClient, format)
        def customer = customerWithCart[0]
        def cart = customerWithCart[1]
        def address = createAddress(restClient, customer)
        setDeliveryAddressForCart(restClient, customer, cart.code, address.id, format)
        addProductToCartOnline(restClient, customer, cart.code, PRODUCT_POWER_SHOT_A480)
        setDeliveryModeForCart(restClient, customer, cart.code, DELIVERY_STANDARD, format)
        createBillingAddress(customer.id, cart.code, billingAddressPostBody)
        createAPMPaymentInfo(restClient, customer, cart.code, paymentInfoPostBody)

        when: "authorized customer places order"
        def response = placeCheckoutComOrder(customer, cart.code, currency, format)

        then: "error message is thrown"
        with(response) {
            status == SC_BAD_REQUEST
            data.errors[0].message == 'The application has encountered an error'
            data.errors[0].type == 'PaymentAuthorizationError'
        }

        where:
        format | APM        | currency          | billingCountry | paymentInfoPostBody                      | billingAddressPostBody
        JSON   | MULTIBANCO | EGP_CURRENCY_CODE | 'Portugal'     | DEFAULT_CHECKOUT_MULTIBANCO_PAYMENT_JSON | PORTUGAL_BILLING_ADDRESS_JSON
        XML    | MULTIBANCO | EGP_CURRENCY_CODE | 'Portugal'     | DEFAULT_CHECKOUT_MULTIBANCO_PAYMENT_JSON | PORTUGAL_BILLING_ADDRESS_JSON
        JSON   | MULTIBANCO | EUR_CURRENCY_CODE | 'Germany'      | DEFAULT_CHECKOUT_MULTIBANCO_PAYMENT_JSON | DEFAULT_BILLING_ADDRESS_JSON
        XML    | MULTIBANCO | EUR_CURRENCY_CODE | 'Germany'      | DEFAULT_CHECKOUT_MULTIBANCO_PAYMENT_JSON | DEFAULT_BILLING_ADDRESS_JSON
        JSON   | BANCONTACT | EGP_CURRENCY_CODE | 'Belgium'      | DEFAULT_CHECKOUT_BANCONTACT_PAYMENT_JSON | BELGIUM_BILLING_ADDRESS_JSON
        XML    | BANCONTACT | EGP_CURRENCY_CODE | 'Belgium'      | DEFAULT_CHECKOUT_BANCONTACT_PAYMENT_JSON | BELGIUM_BILLING_ADDRESS_JSON
        JSON   | BANCONTACT | EGP_CURRENCY_CODE | 'Germany'      | DEFAULT_CHECKOUT_BANCONTACT_PAYMENT_JSON | DEFAULT_BILLING_ADDRESS_JSON
        XML    | BANCONTACT | EGP_CURRENCY_CODE | 'Germany'      | DEFAULT_CHECKOUT_BANCONTACT_PAYMENT_JSON | DEFAULT_BILLING_ADDRESS_JSON
        JSON   | KNET       | EUR_CURRENCY_CODE | 'Kuwait'       | DEFAULT_CHECKOUT_KNET_PAYMENT_JSON       | KW_BILLING_ADDRESS_JSON
        XML    | KNET       | EUR_CURRENCY_CODE | 'Kuwait'       | DEFAULT_CHECKOUT_KNET_PAYMENT_JSON       | KW_BILLING_ADDRESS_JSON
//        JSON   | BENEFITPAY | EGP_CURRENCY_CODE | 'Bahrain'      | DEFAULT_CHECKOUT_BENEFIT_PAYMENT_JSON    | BH_BILLING_ADDRESS_JSON
//        XML    | BENEFITPAY | EGP_CURRENCY_CODE | 'Bahrain'      | DEFAULT_CHECKOUT_BENEFIT_PAYMENT_JSON    | BH_BILLING_ADDRESS_JSON
        JSON   | POLI       | EUR_CURRENCY_CODE | 'Australia'    | DEFAULT_CHECKOUT_POLI_PAYMENT_JSON       | AU_BILLING_ADDRESS_JSON
        XML    | POLI       | EUR_CURRENCY_CODE | 'Australia'    | DEFAULT_CHECKOUT_POLI_PAYMENT_JSON       | AU_BILLING_ADDRESS_JSON
        JSON   | ALIPAY     | EUR_CURRENCY_CODE | 'China'        | DEFAULT_CHECKOUT_ALIPAY_PAYMENT_JSON     | CN_BILLING_ADDRESS_JSON
        XML    | ALIPAY     | EUR_CURRENCY_CODE | 'China'        | DEFAULT_CHECKOUT_ALIPAY_PAYMENT_JSON     | CN_BILLING_ADDRESS_JSON
        JSON   | SEPA       | USD_CURRENCY_CODE | 'Austria'      | DEFAULT_CHECKOUT_SEPA_PAYMENT_JSON       | AT_BILLING_ADDRESS_JSON
        XML    | SEPA       | USD_CURRENCY_CODE | 'Austria'      | DEFAULT_CHECKOUT_SEPA_PAYMENT_JSON       | AT_BILLING_ADDRESS_JSON
        JSON   | IDEAL      | AUD_CURRENCY_CODE | 'Netherlands'  | DEFAULT_CHECKOUT_IDEAL_PAYMENT_JSON      | NL_BILLING_ADDRESS_JSON
        XML    | IDEAL      | AUD_CURRENCY_CODE | 'Netherlands'  | DEFAULT_CHECKOUT_IDEAL_PAYMENT_JSON      | NL_BILLING_ADDRESS_JSON
        JSON   | EPS        | AUD_CURRENCY_CODE | 'Austria'      | DEFAULT_CHECKOUT_EPS_PAYMENT_JSON        | AT_BILLING_ADDRESS_JSON
        XML    | EPS        | AUD_CURRENCY_CODE | 'Austria'      | DEFAULT_CHECKOUT_EPS_PAYMENT_JSON        | AT_BILLING_ADDRESS_JSON
        JSON   | 'Oxxo'     | EUR_CURRENCY_CODE | 'Mexico'       | DEFAULT_CHECKOUT_OXXO_PAYMENT_JSON       | MX_BILLING_ADDRESS_JSON
        XML    | 'Oxxo'     | EUR_CURRENCY_CODE | 'Mexico'       | DEFAULT_CHECKOUT_OXXO_PAYMENT_JSON       | MX_BILLING_ADDRESS_JSON
    }
}
