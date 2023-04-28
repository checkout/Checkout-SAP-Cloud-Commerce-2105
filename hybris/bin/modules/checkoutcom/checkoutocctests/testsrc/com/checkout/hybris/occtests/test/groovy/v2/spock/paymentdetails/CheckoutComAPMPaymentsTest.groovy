package com.checkout.hybris.occtests.test.groovy.v2.spock.paymentdetails

import de.hybris.bootstrap.annotations.ManualTest
import spock.lang.Unroll

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.*
import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.ContentType.XML
import static org.apache.http.HttpStatus.SC_BAD_REQUEST
import static org.apache.http.HttpStatus.SC_CREATED

@ManualTest
@Unroll
class CheckoutComAPMPaymentsTest extends AbstractCheckoutComPaymentsTest {

    def "User creates #APM payment info with when request: #requestFormat"() {
        given: "a logged in user with created cart"
        def customerWithCart = createAndAuthorizeCustomerWithCart(restClient, requestFormat)
        def customer = customerWithCart[0]
        def cart = customerWithCart[1]
        createBillingAddress(customer.id, cart.code)

        when: "user attempts to store payment info for his cart"
        def response = addCheckoutComAPMPaymentDetailsToCart(customer, cart, postBody, requestFormat)

        then: "his payment details are saved"
        with(response) {
            if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
            status == SC_CREATED
        }

        where:
        requestFormat | APM        | postBody
        JSON          | MULTIBANCO | DEFAULT_CHECKOUT_MULTIBANCO_PAYMENT_JSON
        XML           | MULTIBANCO | DEFAULT_CHECKOUT_MULTIBANCO_PAYMENT_XML
        JSON          | BANCONTACT | DEFAULT_CHECKOUT_BANCONTACT_PAYMENT_JSON
        XML           | BANCONTACT | DEFAULT_CHECKOUT_BANCONTACT_PAYMENT_XML
        JSON          | GIROPAY    | DEFAULT_CHECKOUT_GIROPAY_PAYMENT_JSON
        XML           | GIROPAY    | DEFAULT_CHECKOUT_GIROPAY_PAYMENT_XML
        JSON          | SOFORT     | DEFAULT_CHECKOUT_SOFORT_PAYMENT_JSON
        XML           | SOFORT     | DEFAULT_CHECKOUT_SOFORT_PAYMENT_XML
        JSON          | PAYPAL     | DEFAULT_CHECKOUT_PAYPAL_PAYMENT_JSON
        XML           | PAYPAL     | DEFAULT_CHECKOUT_PAYPAL_PAYMENT_XML
        JSON          | SEPA       | DEFAULT_CHECKOUT_SEPA_PAYMENT_JSON
        XML           | SEPA       | DEFAULT_CHECKOUT_SEPA_PAYMENT_XML
        JSON          | FAWRY      | DEFAULT_CHECKOUT_FAWRY_PAYMENT_JSON
        XML           | FAWRY      | DEFAULT_CHECKOUT_FAWRY_PAYMENT_XML
        JSON          | IDEAL      | DEFAULT_CHECKOUT_IDEAL_PAYMENT_JSON
        XML           | IDEAL      | DEFAULT_CHECKOUT_IDEAL_PAYMENT_XML
        JSON          | KLARNA     | DEFAULT_CHECKOUT_KLARNA_PAYMENT_JSON
        XML           | KLARNA     | DEFAULT_CHECKOUT_KLARNA_PAYMENT_XML
        JSON          | KNET       | DEFAULT_CHECKOUT_KNET_PAYMENT_JSON
        XML           | KNET       | DEFAULT_CHECKOUT_KNET_PAYMENT_XML
        JSON          | BENEFITPAY | DEFAULT_CHECKOUT_BENEFIT_PAYMENT_JSON
        XML           | BENEFITPAY | DEFAULT_CHECKOUT_BENEFIT_PAYMENT_XML
        JSON          | POLI       | DEFAULT_CHECKOUT_POLI_PAYMENT_JSON
        XML           | POLI       | DEFAULT_CHECKOUT_POLI_PAYMENT_XML
        JSON          | QPAY       | DEFAULT_CHECKOUT_QPAY_PAYMENT_JSON
        XML           | QPAY       | DEFAULT_CHECKOUT_QPAY_PAYMENT_XML
        JSON          | ALIPAY     | DEFAULT_CHECKOUT_ALIPAY_PAYMENT_JSON
        XML           | ALIPAY     | DEFAULT_CHECKOUT_ALIPAY_PAYMENT_XML
        XML           | P24        | DEFAULT_CHECKOUT_P24_PAYMENT_XML
        JSON          | P24        | DEFAULT_CHECKOUT_P24_PAYMENT_JSON
        JSON          | EPS        | DEFAULT_CHECKOUT_EPS_PAYMENT_JSON
        XML           | EPS        | DEFAULT_CHECKOUT_EPS_PAYMENT_XML
        JSON          | OXXO       | DEFAULT_CHECKOUT_OXXO_PAYMENT_JSON
        XML           | OXXO       | DEFAULT_CHECKOUT_OXXO_PAYMENT_XML
    }

    def "User can not create #APM payment info with invalid #subjectAttr when request: #requestFormat"() {
        given: "a logged in user with created cart"
        def customerWithCart = createAndAuthorizeCustomerWithCart(restClient)
        def customer = customerWithCart[0]
        def cart = customerWithCart[1]
        createBillingAddress(customer.id, cart.code)

        when: "user attempts to store payment info for his cart"
        def response = addCheckoutComAPMPaymentDetailsToCart(customer, cart, postBody, requestFormat)

        then: "error message is thrown"
        with(response) {
            status == SC_BAD_REQUEST
            data.errors[0].message == expectedErrorMessage
            data.errors[0].subject == subjectAttr
        }

        where:
        requestFormat | APM   | postBody                                    | subjectAttr         | expectedErrorMessage
        JSON          | SEPA  | NO_IBAN_SEPA_PAYMENT_JSON                   | 'accountIban'       | 'This field is required.'
        XML           | SEPA  | NO_IBAN_CHECKOUT_SEPA_PAYMENT_XML           | 'accountIban'       | 'This field is required.'
        JSON          | SEPA  | NO_FIRSTNAME_CHECKOUT_SEPA_PAYMENT_JSON     | 'firstName'         | 'This field is required.'
        XML           | SEPA  | NO_FIRSTNAME_CHECKOUT_SEPA_PAYMENT_XML      | 'firstName'         | 'This field is required.'
        JSON          | SEPA  | NO_LASTNAME_CHECKOUT_SEPA_PAYMENT_JSON      | 'lastName'          | 'This field is required.'
        XML           | SEPA  | NO_LASTNAMECHECKOUT_SEPA_PAYMENT_XML        | 'lastName'          | 'This field is required.'
        JSON          | SEPA  | NO_PAYMENT_TYPE_CHECKOUT_SEPA_PAYMENT_JSON  | 'paymentType'       | 'This field is required.'
        XML           | SEPA  | NO_PAYMENT_TYPE_CHECKOUT_SEPA_PAYMENT_XML   | 'paymentType'       | 'This field is required.'
        JSON          | SEPA  | NO_ADDRESS_LINE_CHECKOUT_SEPA_PAYMENT_JSON  | 'addressLine1'      | 'This field is required.'
        XML           | SEPA  | NO_ADDRESS_LINE_CHECKOUT_SEPA_PAYMENT_XML   | 'addressLine1'      | 'This field is required.'
        JSON          | SEPA  | NO_CITY_CHECKOUT_SEPA_PAYMENT_JSON          | 'city'              | 'This field is required.'
        XML           | SEPA  | NO_CITY_CHECKOUT_SEPA_PAYMENT_XML           | 'city'              | 'This field is required.'
        JSON          | SEPA  | NO_POSTAL_CODE_CHECKOUT_SEPA_PAYMENT_JSON   | 'postalCode'        | 'This field is required.'
        XML           | SEPA  | NO_POSTAL_CODE_CHECKOUT_SEPA_PAYMENT_XML    | 'postalCode'        | 'This field is required.'
        JSON          | SEPA  | NO_COUNTRY_CHECKOUT_SEPA_PAYMENT_JSON       | 'country'           | 'This field is required.'
        XML           | SEPA  | NO_COUNTRY_CHECKOUT_SEPA_PAYMENT_XML        | 'country'           | 'This field is required.'
        JSON          | FAWRY | BAD_CHECKOUT_FAWRY_PAYMENT_JSON             | 'mobileNumber'      | 'Mobile number is required for this payment'
        XML           | FAWRY | BAD_CHECKOUT_FAWRY_PAYMENT_XML              | 'mobileNumber'      | 'Mobile number is required for this payment'
        JSON          | FAWRY | WRONG_NUMBER_CHECKOUT_FAWRY_PAYMENT_JSON    | 'mobileNumber'      | 'The mobile number must have length 11 digits and must not include the country code'
        XML           | FAWRY | WRONG_NUMBER_CHECKOUT_FAWRY_PAYMENT_XML     | 'mobileNumber'      | 'The mobile number must have length 11 digits and must not include the country code'
        JSON          | IDEAL | WRONG_BIC_CHECKOUT_IDEAL_PAYMENT_JSON       | 'bic'               | 'BIC field is mandatory and must 8 or 11 characters long'
        XML           | IDEAL | WRONG_BIC_CHECKOUT_IDEAL_PAYMENT_XML        | 'bic'               | 'BIC field is mandatory and must 8 or 11 characters long'
        JSON          | IDEAL | BAD_CHECKOUT_IDEAL_PAYMENT_JSON             | 'bic'               | 'BIC field is mandatory and must 8 or 11 characters long'
        XML           | IDEAL | BAD_CHECKOUT_IDEAL_PAYMENT_XML              | 'bic'               | 'BIC field is mandatory and must 8 or 11 characters long'
        JSON          | OXXO  | MISSING_DOCUMENT_CHECKOUT_OXXO_PAYMENT_JSON | 'document'          | 'Document is required for this payment'
        XML           | OXXO  | MISSING_DOCUMENT_CHECKOUT_OXXO_PAYMENT_XML  | 'document'          | 'Document is required for this payment'
        JSON          | OXXO  | WRONG_DOCUMENT_CHECKOUT_OXXO_PAYMENT_JSON   | 'document'          | 'Document must have 18 alphanumeric characters'
        XML           | OXXO  | WRONG_DOCUMENT_CHECKOUT_OXXO_PAYMENT_XML    | 'document'          | 'Document must have 18 alphanumeric characters'
    }
}
