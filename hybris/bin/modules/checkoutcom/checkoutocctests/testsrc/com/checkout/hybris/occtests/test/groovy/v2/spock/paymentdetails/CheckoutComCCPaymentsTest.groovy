package com.checkout.hybris.occtests.test.groovy.v2.spock.paymentdetails

import de.hybris.bootstrap.annotations.ManualTest
import spock.lang.Unroll

import static groovyx.net.http.ContentType.*
import static org.apache.http.HttpStatus.*

@ManualTest
@Unroll
class CheckoutComCCPaymentsTest extends AbstractCheckoutComPaymentsTest {

    def "Customer creates a billing address and attaches it to the cart when request: #requestFormat"() {
        given: "a logged in user with created cart"
        def customerWithCart = createAndAuthorizeCustomerWithCart(restClient)
        def customer = customerWithCart[0]
        def cart = customerWithCart[1]

        when: "user attempts to create a new address"
        def response = restClient.post(
                path: getBasePathWithSite() + '/users/' + customer.id + '/carts/' + cart.code + '/checkoutoccbillingaddress',
                body: postBody,
                requestContentType: requestFormat)

        then: "the address is added and proper address id is returned"
        with(response) {
            status == SC_CREATED
        }

        where:
        requestFormat | postBody
        JSON          | DEFAULT_BILLING_ADDRESS_JSON
        XML           | DEFAULT_BILLING_ADDRESS_XML
    }

    def "Customer creates an address and attaches it to the cart as shipping/billing address when request: #requestFormat"() {
        given: "a logged in user with created cart"
        def customerWithCart = createAndAuthorizeCustomerWithCart(restClient)
        def customer = customerWithCart[0]
        def cart = customerWithCart[1]

        when: "user attempts to create a new address"
        def response = restClient.post(
                path: getBasePathWithSite() + '/users/' + customer.id + '/carts/' + cart.code + '/addresses/checkoutcomdeliverypayment',
                body: postBody,
                requestContentType: requestFormat)

        then: "the address is added and proper address id is returned"
        with(response) {
            status == SC_CREATED
        }

        where:
        requestFormat | postBody
        JSON          | DEFAULT_BILLING_ADDRESS_JSON
        XML           | DEFAULT_BILLING_ADDRESS_XML
    }

    def "Customer can not create a new delivery/billing address with incomplete fields: #requestFormat"() {
        given: "a logged in user with created cart"
        def customerWithCart = createAndAuthorizeCustomerWithCart(restClient)
        def customer = customerWithCart[0]
        def cart = customerWithCart[1]

        when: "user attempts to create a new address"
        def response = restClient.post(
                path: getBasePathWithSite() + '/users/' + customer.id + '/carts/' + cart.code + '/addresses/checkoutcomdeliverypayment',
                body: postBody,
                requestContentType: requestFormat)

        then: "he is not able to do so"
        with(response) {
            status == SC_BAD_REQUEST
            data.errors.size() > 0
            data.errors[0].type == 'ValidationError'
            data.errors[0].reason == 'missing'
            data.errors[0].subject == 'country.isocode'
            data.errors[0].message == 'This field is required and must to be between 1 and 2 characters long.'
        }

        where:
        requestFormat | postBody
        JSON          | INVALID_ADDRESS_JSON
        XML           | INVALID_ADDRESS_XML
    }

    def "Customer attaches an existing address to the cart as shipping/billing address when request: #requestFormat"() {
        given: "a logged in user with created cart"
        def customerWithCart = createAndAuthorizeCustomerWithCart(restClient)
        def customer = customerWithCart[0]
        def addressId = createAddress(customer, DEFAULT_BILLING_ADDRESS_JSON)
        def cart = customerWithCart[1]

        when: "user attempts to attach an existing address"
        def response = restClient.put(
                path: getBasePathWithSite() + '/users/' + customer.id + '/carts/' + cart.code + '/addresses/checkoutcomdeliverypayment',
                query: ['addressId': addressId],
                requestContentType: requestFormat)

        then: "the address is attached"
        with(response) {
            status == SC_OK
        }

        where:
        requestFormat << [XML, JSON]
    }

    def "Customer can not attach a non existing address to the cart: #requestFormat"() {
        given: "a logged in user with created cart"
        def customerWithCart = createAndAuthorizeCustomerWithCart(restClient)
        def customer = customerWithCart[0]
        def addressId = INVALID_ADDRESS_ID
        def cart = customerWithCart[1]

        when: "user attempts to attach an existing address"
        def response = restClient.put(
                path: getBasePathWithSite() + '/users/' + customer.id + '/carts/' + cart.code + '/addresses/checkoutcomdeliverypayment',
                query: ['addressId': addressId],
                requestContentType: requestFormat)

        then: "he is not able to do so"
        with(response) {
            status == SC_BAD_REQUEST
            data.errors.size() > 0
            data.errors[0].type == 'CartAddressError'
            data.errors[0].reason == 'notValid'
            data.errors[0].subject == addressId
            data.errors[0].message == 'Address given by id ' + addressId + ' is not valid'
        }

        where:
        requestFormat << [XML, JSON]
    }

    def "User creates credit card payment info when request: #requestFormat"() {
        given: "a logged in user with created cart"
        def customerWithCart = createAndAuthorizeCustomerWithCart(restClient)
        def customer = customerWithCart[0]
        def cart = customerWithCart[1]
        createBillingAddress(customer.id, cart.code)

        when: "user attempts to store payment info for his cart"
        def response = restClient.post(
                path: getBasePathWithSite() + '/users/' + customer.id + '/carts/' + cart.code + '/checkoutcompaymentdetails',
                body: postBody,
                query: ["fields": FIELD_SET_LEVEL_FULL],
                requestContentType: requestFormat)

        then: "his payment details are saved"
        with(response) {
            if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
            status == SC_CREATED
        }

        where:
        requestFormat | postBody
        JSON          | DEFAULT_CHECKOUT_CC_PAYMENT_JSON
        XML           | DEFAULT_CHECKOUT_CC_PAYMENT_XML
    }

    def "User can not create credit card payment info without payment token when request: #requestFormat"() {
        given: "a logged in user with created cart"
        def customerWithCart = createAndAuthorizeCustomerWithCart(restClient)
        def customer = customerWithCart[0]
        def cart = customerWithCart[1]
        createBillingAddress(customer.id, cart.code)

        when: "user attempts to store payment info for his cart"
        def response = restClient.post(
                path: getBasePathWithSite() + '/users/' + customer.id + '/carts/' + cart.code + '/checkoutcompaymentdetails',
                body: postBody,
                query: ["fields": FIELD_SET_LEVEL_FULL],
                requestContentType: requestFormat)

        then: "error message is thrown"
        with(response) {
            status == SC_BAD_REQUEST
            data.errors[0].message == 'This field is required.'
            data.errors[0].subject == 'paymentToken'
        }

        where:
        requestFormat | postBody
        JSON          | BAD_CHECKOUT_CC_PAYMENT_JSON
        XML           | BAD_CHECKOUT_CC_PAYMENT_XML
    }

    def "Customer can not create credit card payment info for another customer when request: #requestFormat"() {
        given: "two users, one with cart"
        def customer1 = registerCustomerWithTrustedClient(restClient, requestFormat)
        def cart1 = createCart(restClient, customer1, requestFormat)
        createBillingAddress(customer1.id, cart1.code)
        registerAndAuthorizeCustomer(restClient, requestFormat)

        when: "user attempts to create info for another user"
        def response = restClient.post(
                path: getBasePathWithSite() + '/users/' + customer1.id + '/carts/' + cart1.code + '/checkoutcompaymentdetails',
                body: postBody,
                query: ["fields": FIELD_SET_LEVEL_FULL],
                requestContentType: requestFormat)

        then: "he is not able to do so"
        with(response) { status == SC_FORBIDDEN }

        where:
        requestFormat | postBody
        JSON          | SECOND_PAYMENT_NON_DEFAULT_JSON
        XML           | SECOND_PAYMENT_NON_DEFAULT_XML
    }

    def "Customer manager creates credit card payment info when request: #requestFormat"() {
        given: "a user with created cart and logged in customer manager"
        def customerWithCart = createAndAuthorizeCustomerWithCart(restClient)
        def customer = customerWithCart[0]
        def cart = customerWithCart[1]
        createBillingAddress(customer.id, cart.code)
        authorizeCustomerManager(restClient)

        when: "manager attempts to store payment info for customer's cart"
        def response = restClient.post(
                path: getBasePathWithSite() + '/users/' + customer.id + '/carts/' + cart.code + '/checkoutcompaymentdetails',
                body: postBody,
                query: ["fields": FIELD_SET_LEVEL_FULL],
                requestContentType: requestFormat)
        then: "the payment details are saved and first payment info is marked as default"
        with(response) {
            status == SC_CREATED
        }

        where:
        requestFormat | postBody
        JSON          | DEFAULT_CHECKOUT_CC_PAYMENT_JSON
        XML           | DEFAULT_CHECKOUT_CC_PAYMENT_XML
    }

    def "User gets his payment info: #format"() {
        given: "user with payment info"
        def customerWithPaymentInfo = createCustomerWithPaymentInfo(restClient, format)
        def customer = customerWithPaymentInfo[0]
        def info = customerWithPaymentInfo[1]

        when: "user attempts to retrieve his info"
        def response = restClient.get(
                path: getBasePathWithSite() + '/users/' + customer.id + '/paymentdetails/' + info.id,
                query: ["fields": FIELD_SET_LEVEL_FULL],
                contentType: format,
                requestContentType: URLENC)
        then: "he is able to do so"
        with(response) {
            status == SC_OK
            data.accountHolderName != null
            data.paymentToken != null
        }
        where:
        format << [XML, JSON]
    }

    def "User gets his payment info list: #format"() {
        given: "user with payment info"
        def customerWithPaymentInfo = createCustomerWithPaymentInfo(restClient, format)
        def customer = customerWithPaymentInfo[0]

        when: "user attempts to retrieve his info"
        def response = restClient.get(
                path: getBasePathWithSite() + '/users/' + customer.id + '/paymentdetails',
                query: ["fields": FIELD_SET_LEVEL_FULL],
                contentType: format,
                requestContentType: URLENC)

        then: "he is able to do so"
        with(response) {
            status == SC_OK
            data.payments.size() == 1
            data.payments[0].accountHolderName != null
            data.payments[0].paymentToken != null
        }

        where:
        format << [XML, JSON]
    }

    def "Customer can not get payment info of another customer: #format"() {
        given: "two users, one with payment info"
        def customerWithPaymentInfo1 = createCustomerWithPaymentInfo(restClient, format)
        def customer1 = customerWithPaymentInfo1[0]
        def info1 = customerWithPaymentInfo1[1]

        registerAndAuthorizeCustomer(restClient, format)
        //now logged in as customer2

        when: "user attempts to retrieve info of another user"
        def response = restClient.get(
                path: getBasePathWithSite() + '/users/' + customer1.id + '/paymentdetails/' + info1.id,
                query: ["fields": FIELD_SET_LEVEL_FULL],
                contentType: format,
                requestContentType: URLENC)

        then: "he is not able to do so"
        with(response) { status == SC_FORBIDDEN }

        where:
        format << [XML, JSON]
    }

    def "Customer can not get not existing payment details : #format"() {
        given: "registered users"
        def customer = registerAndAuthorizeCustomer(restClient, format)

        when: "user tries to get payment details with wrong identifier"
        def response = restClient.get(
                path: getBasePathWithSite() + '/users/' + customer.id + '/paymentdetails/wrongPaymentId',
                query: ["fields": FIELD_SET_LEVEL_FULL],
                contentType: format,
                requestContentType: URLENC)

        then: "he is not able to do so"
        with(response) {
            status == SC_BAD_REQUEST
            data.errors.size() > 0
            data.errors[0].type == 'ValidationError'
            data.errors[0].reason == 'unknownIdentifier'
            data.errors[0].subject == 'paymentDetailsId'
            data.errors[0].message == 'Payment details [wrongPaymentId] not found.'
        }
        where:
        format << [XML, JSON]
    }

    def "Customer manager can get payment info of customer: #format"() {
        given: "user with payment info and logged in customer manager"
        def customerWithPaymentInfo1 = createCustomerWithPaymentInfo(restClient, format)
        def customer1 = customerWithPaymentInfo1[0]
        def info1 = customerWithPaymentInfo1[1]
        authorizeCustomerManager(restClient)

        when: "customer manager attempts to retrieve info of customer"
        def response = restClient.get(
                path: getBasePathWithSite() + '/users/' + customer1.id + '/paymentdetails/' + info1.id,
                query: ["fields": FIELD_SET_LEVEL_FULL],
                contentType: format,
                requestContentType: URLENC)

        then: "he is able to do so"
        with(response) { status == SC_OK }

        where:
        format << [XML, JSON]
    }
}
