package com.checkout.hybris.occtests.test.groovy.v2.spock.orders

import com.checkout.hybris.occtests.test.groovy.v2.spock.paymentdetails.AbstractCheckoutComPaymentsTest
import de.hybris.bootstrap.annotations.ManualTest
import spock.lang.Unroll

import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.ContentType.XML
import static org.apache.http.HttpStatus.SC_BAD_REQUEST
import static org.apache.http.HttpStatus.SC_CREATED

@ManualTest
@Unroll
class CheckoutComCCOrdersTest extends AbstractCheckoutComPaymentsTest {

    def "Authorized customer places an order with 3ds: #format"() {
        given: "an authorized customer"
        def customerWithCart = createAndAuthorizeCustomerWithCart(restClient, format)
        def customer = customerWithCart[0]
        def cart = customerWithCart[1]
        def address = createAddress(restClient, customer)
        setDeliveryAddressForCart(restClient, customer, cart.code, address.id, format)
        addProductToCartOnline(restClient, customer, cart.code, PRODUCT_POWER_SHOT_A480)
        setDeliveryModeForCart(restClient, customer, cart.code, DELIVERY_STANDARD, format)
        createBillingAddress(customer.id, cart.code)
        createPaymentInfo(restClient, customer, cart.code)
        activate3ds()

        when: "authorized customer places order"
        def response = placeCheckoutComOrder(customer, cart.code, EUR_CURRENCY_CODE, format)

        then: "redirect URL is returned"
        with(response) {
            if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
            status == SC_CREATED
            isNotEmpty(data.redirectUrl)
        }

        where:
        format << [XML, JSON]
    }

    def "Authorized customer places an order without 3ds: #format"() {
        given: "an authorized customer"
        def customerWithCart = createAndAuthorizeCustomerWithCart(restClient, format)
        def customer = customerWithCart[0]
        def cart = customerWithCart[1]
        def address = createAddress(restClient, customer)
        setDeliveryAddressForCart(restClient, customer, cart.code, address.id, format)
        addProductToCartOnline(restClient, customer, cart.code, PRODUCT_POWER_SHOT_A480)
        setDeliveryModeForCart(restClient, customer, cart.code, DELIVERY_STANDARD, format)
        createBillingAddress(customer.id, cart.code)
        createPaymentInfo(restClient, customer, cart.code)
        deactivate3ds()

        when: "authorized customer places order"
        def response = placeCheckoutComOrder(customer, cart.code, EUR_CURRENCY_CODE, format)

        then: "order is created"
        with(response) {
            if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
            status == SC_CREATED
            isNotEmpty(data.code)
            data.paymentType == "CARD"
        }

        where:
        format << [XML, JSON]
    }

    def "Authorized customer places an order with invalid card: #format"() {
        given: "an authorized customer"
        def customerWithCart = createAndAuthorizeCustomerWithCart(restClient, format)
        def customer = customerWithCart[0]
        def cart = customerWithCart[1]
        def address = createAddress(restClient, customer)
        setDeliveryAddressForCart(restClient, customer, cart.code, address.id, format)
        addProductToCartOnline(restClient, customer, cart.code, PRODUCT_POWER_SHOT_A480)
        setDeliveryModeForCart(restClient, customer, cart.code, DELIVERY_STANDARD, format)
        createBillingAddress(customer.id, cart.code)
        createPaymentInfo(restClient, customer, cart.code, INVALID_CHECKOUT_CC_PAYMENT_JSON, INVALID_GET_CC_TOKEN_JSON)

        when: "authorized customer places order"
        def response = placeCheckoutComOrder(customer, cart.code, EUR_CURRENCY_CODE, format)

        then: "error message is thrown"
        with(response) {
            status == SC_BAD_REQUEST
            data.errors[0].message == 'The application has encountered an error'
            data.errors[0].type == 'PaymentAuthorizationError'
        }

        where:
        format << [XML, JSON]
    }

    def "Authorized customer places an order when comes from redirect url without checkout session id: #format"() {
        given: "authorized customer"
        def customerWithCart = createAndAuthorizeCustomerWithCart(restClient, format)
        def customer = customerWithCart[0]
        def cart = customerWithCart[1]
        def address = createAddress(restClient, customer)
        setDeliveryAddressForCart(restClient, customer, cart.code, address.id, format)
        addProductToCartOnline(restClient, customer, cart.code, PRODUCT_POWER_SHOT_A480)
        setDeliveryModeForCart(restClient, customer, cart.code, DELIVERY_STANDARD, format)
        createBillingAddress(customer.id, cart.code)
        createAPMPaymentInfo(restClient, customer, cart.code, DEFAULT_CHECKOUT_PAYPAL_PAYMENT_JSON)

        when: "authorized customer places order"
        def response = placeCheckoutComOrderWithCkoSessionId(customer, cart.code, EMPTY_CKO_SESSION_ID_JSON, format)

        then: "error message is thrown"
        with(response) {
            if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
            status == SC_BAD_REQUEST
            data.errors[0].message == 'The application has encountered an error'
            data.errors[0].type == 'PlaceOrderError'
        }

        where:
        format << [XML, JSON]
    }

    def "Authorized customer places an order when comes from redirect url with an invalid checkout session id: #format"() {
        given: "authorized customer"
        def customerWithCart = createAndAuthorizeCustomerWithCart(restClient, format)
        def customer = customerWithCart[0]
        def cart = customerWithCart[1]
        def address = createAddress(restClient, customer)
        setDeliveryAddressForCart(restClient, customer, cart.code, address.id, format)
        addProductToCartOnline(restClient, customer, cart.code, PRODUCT_POWER_SHOT_A480)
        setDeliveryModeForCart(restClient, customer, cart.code, DELIVERY_STANDARD, format)
        createBillingAddress(customer.id, cart.code)
        createAPMPaymentInfo(restClient, customer, cart.code, DEFAULT_CHECKOUT_PAYPAL_PAYMENT_JSON)

        when: "authorized customer places order"
        def response = placeCheckoutComOrderWithCkoSessionId(customer, cart.code, INVALID_CKO_SESSION_ID_JSON, format)

        then: "error message is thrown"
        with(response) {
            if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
            status == SC_BAD_REQUEST
            data.errors[0].message == 'The application has encountered an error'
            data.errors[0].type == 'PlaceOrderError'
        }

        where:
        format << [XML, JSON]
    }

    def "Authorized customer places an order with MADA 3ds flow card with 3DS Active: #format"() {
        given: "an authorized customer"
        def customerWithCart = createAndAuthorizeCustomerWithCart(restClient, format)
        def customer = customerWithCart[0]
        def cart = customerWithCart[1]
        def address = createAddress(restClient, customer)
        setDeliveryAddressForCart(restClient, customer, cart.code, address.id, format)
        addProductToCartOnline(restClient, customer, cart.code, PRODUCT_POWER_SHOT_A480)
        setDeliveryModeForCart(restClient, customer, cart.code, DELIVERY_STANDARD, format)
        createBillingAddress(customer.id, cart.code)
        createPaymentInfo(restClient, customer, cart.code, MADA_3DS_CHECKOUT_CC_PAYMENT_JSON, MADA_3DS_GET_CC_TOKEN_JSON)
        activate3ds()

        when: "authorized customer places order"
        def response = placeCheckoutComOrder(customer, cart.code, EUR_CURRENCY_CODE, format)

        then: "redirect URL is returned"
        with(response) {
            if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
            status == SC_CREATED
            isNotEmpty(data.redirectUrl)
        }

        where:
        format << [XML, JSON]
    }

    def "Authorized customer places an order with MADA card 3DS flow with 3DS Inactive: #format"() {
        given: "an authorized customer"
        def customerWithCart = createAndAuthorizeCustomerWithCart(restClient, format)
        def customer = customerWithCart[0]
        def cart = customerWithCart[1]
        def address = createAddress(restClient, customer)
        setDeliveryAddressForCart(restClient, customer, cart.code, address.id, format)
        addProductToCartOnline(restClient, customer, cart.code, PRODUCT_POWER_SHOT_A480)
        setDeliveryModeForCart(restClient, customer, cart.code, DELIVERY_STANDARD, format)
        createBillingAddress(customer.id, cart.code)
        createPaymentInfo(restClient, customer, cart.code, MADA_3DS_CHECKOUT_CC_PAYMENT_JSON, MADA_3DS_GET_CC_TOKEN_JSON)
        deactivate3ds()

        when: "authorized customer places order"
        def response = placeCheckoutComOrder(customer, cart.code, EUR_CURRENCY_CODE, format)

        then: "redirect URL is returned"
        with(response) {
            if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
            status == SC_CREATED
            isNotEmpty(data.redirectUrl)
        }

        where:
        format << [XML, JSON]
    }

    def "Authorized customer places an order with MADA frictionless flow card with 3DS Active: #format"() {
        given: "an authorized customer"
        def customerWithCart = createAndAuthorizeCustomerWithCart(restClient, format)
        def customer = customerWithCart[0]
        def cart = customerWithCart[1]
        def address = createAddress(restClient, customer)
        setDeliveryAddressForCart(restClient, customer, cart.code, address.id, format)
        addProductToCartOnline(restClient, customer, cart.code, PRODUCT_POWER_SHOT_A480)
        setDeliveryModeForCart(restClient, customer, cart.code, DELIVERY_STANDARD, format)
        createBillingAddress(customer.id, cart.code)
        createPaymentInfo(restClient, customer, cart.code, MADA_FRICTIONLESS_CHECKOUT_CC_PAYMENT_JSON, MADA_FRICTIONLESS_GET_CC_TOKEN_JSON)
        activate3ds()

        when: "authorized customer places order"
        def response = placeCheckoutComOrder(customer, cart.code, EUR_CURRENCY_CODE, format)

        then: "redirect URL is returned"
        with(response) {
            if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
            status == SC_CREATED
            isNotEmpty(data.redirectUrl)
        }

        where:
        format << [XML, JSON]
    }

    def "Authorized customer places an order with MADA frictionless flow with 3DS Inactive: #format"() {
        given: "an authorized customer"
        def customerWithCart = createAndAuthorizeCustomerWithCart(restClient, format)
        def customer = customerWithCart[0]
        def cart = customerWithCart[1]
        def address = createAddress(restClient, customer)
        setDeliveryAddressForCart(restClient, customer, cart.code, address.id, format)
        addProductToCartOnline(restClient, customer, cart.code, PRODUCT_POWER_SHOT_A480)
        setDeliveryModeForCart(restClient, customer, cart.code, DELIVERY_STANDARD, format)
        createBillingAddress(customer.id, cart.code)
        createPaymentInfo(restClient, customer, cart.code, MADA_FRICTIONLESS_CHECKOUT_CC_PAYMENT_JSON, MADA_FRICTIONLESS_GET_CC_TOKEN_JSON)
        deactivate3ds()

        when: "authorized customer places order"
        def response = placeCheckoutComOrder(customer, cart.code, EUR_CURRENCY_CODE, format)

        then: "redirect URL is returned"
        with(response) {
            if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
            status == SC_CREATED
            isNotEmpty(data.redirectUrl)
        }

        where:
        format << [XML, JSON]
    }

    def "Guest customer places an order with 3ds: #format"() {
        given: "a guest customer"
        def userGuid = getGuestUid()
        def cart = prepareCartForGuestOrder(restClient, userGuid, format)
        createPaymentInfo(restClient, ANONYMOUS_USER, cart.guid)
        activate3ds()

        when: "guest customer places order"
        def response = placeCheckoutComOrder(ANONYMOUS_USER, cart.guid, EUR_CURRENCY_CODE, format)

        then: "redirect URL is returned"
        with(response) {
            if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
            status == SC_CREATED
            isNotEmpty(data.redirectUrl)
        }

        where:
        format << [XML, JSON]
    }

    def "Guest customer places an order without 3ds: #format"() {
        given: "a guest customer"
        def userGuid = getGuestUid()
        def cart = prepareCartForGuestOrder(restClient, userGuid, format)
        createPaymentInfo(restClient, ANONYMOUS_USER, cart.guid)
        deactivate3ds()

        when: "guest customer places order"
        def response = placeCheckoutComOrder(ANONYMOUS_USER, cart.guid, EUR_CURRENCY_CODE, format)

        then: "order is created"
        with(response) {
            if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
            status == SC_CREATED
            isNotEmpty(data.code)
        }

        where:
        format << [XML, JSON]
    }

    def "Guest customer places an order with invalid card: #format"() {
        given: "a guest customer"
        def userGuid = getGuestUid()
        def cart = prepareCartForGuestOrder(restClient, userGuid, format)
        createPaymentInfo(restClient, ANONYMOUS_USER, cart.guid, INVALID_CHECKOUT_CC_PAYMENT_JSON, INVALID_GET_CC_TOKEN_JSON)

        when: "guest customer places order"
        def response = placeCheckoutComOrder(ANONYMOUS_USER, cart.guid, EUR_CURRENCY_CODE, format)

        then: "error message is thrown"
        with(response) {
            status == SC_BAD_REQUEST
            data.errors[0].message == 'The application has encountered an error'
            data.errors[0].type == 'PaymentAuthorizationError'
        }

        where:
        format << [XML, JSON]
    }

    def "Guest customer places an order when comes from redirect url with an invalid checkout session id: #format"() {
        given: "a guest customer"
        def userGuid = getGuestUid()
        def cart = prepareCartForGuestOrder(restClient, userGuid, format)
        createAPMPaymentInfo(restClient, ANONYMOUS_USER, cart.guid, DEFAULT_CHECKOUT_PAYPAL_PAYMENT_JSON)

        when: "guest customer places order"
        def response = placeCheckoutComOrderWithCkoSessionId(ANONYMOUS_USER, cart.guid, INVALID_CKO_SESSION_ID_JSON, format)

        then: "error message is thrown"
        with(response) {
            if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
            status == SC_BAD_REQUEST
            data.errors[0].message == 'The application has encountered an error'
            data.errors[0].type == 'PlaceOrderError'
        }

        where:
        format << [XML, JSON]
    }

    def "Guest customer places an order with MADA 3ds flow card with 3DS Active: #format"() {
        given: "a guest customer"
        def userGuid = getGuestUid()
        def cart = prepareCartForGuestOrder(restClient, userGuid, format)
        createPaymentInfo(restClient, ANONYMOUS_USER, cart.guid, MADA_3DS_CHECKOUT_CC_PAYMENT_JSON, MADA_3DS_GET_CC_TOKEN_JSON)
        activate3ds()

        when: "guest customer places order"
        def response = placeCheckoutComOrder(ANONYMOUS_USER, cart.guid, EUR_CURRENCY_CODE, format)

        then: "redirect URL is returned"
        with(response) {
            if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
            status == SC_CREATED
            isNotEmpty(data.redirectUrl)
        }

        where:
        format << [XML, JSON]
    }

    def "Guest customer places an order with MADA 3ds flow card with 3DS Inactive: #format"() {
        given: "a guest customer"
        def userGuid = getGuestUid()
        def cart = prepareCartForGuestOrder(restClient, userGuid, format)
        createPaymentInfo(restClient, ANONYMOUS_USER, cart.guid, MADA_3DS_CHECKOUT_CC_PAYMENT_JSON, MADA_3DS_GET_CC_TOKEN_JSON)
        deactivate3ds()

        when: "guest customer places order"
        def response = placeCheckoutComOrder(ANONYMOUS_USER, cart.guid, EUR_CURRENCY_CODE, format)

        then: "redirect URL is returned"
        with(response) {
            if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
            status == SC_CREATED
            isNotEmpty(data.redirectUrl)
        }

        where:
        format << [XML, JSON]
    }

    def "Guest customer places an order with MADA 3ds flow frictionless flow card with 3DS Active: #format"() {
        given: "a guest customer"
        def userGuid = getGuestUid()
        def cart = prepareCartForGuestOrder(restClient, userGuid, format)
        createPaymentInfo(restClient, ANONYMOUS_USER, cart.guid, MADA_FRICTIONLESS_CHECKOUT_CC_PAYMENT_JSON, MADA_FRICTIONLESS_GET_CC_TOKEN_JSON)
        activate3ds()

        when: "guest customer places order"
        def response = placeCheckoutComOrder(ANONYMOUS_USER, cart.guid, EUR_CURRENCY_CODE, format)

        then: "redirect URL is returned"
        with(response) {
            if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
            status == SC_CREATED
            isNotEmpty(data.redirectUrl)
        }

        where:
        format << [XML, JSON]
    }

    def "Guest customer places an order with MADA 3ds flow frictionless flow card with 3DS Inactive: #format"() {
        given: "a guest customer"
        def userGuid = getGuestUid()
        def cart = prepareCartForGuestOrder(restClient, userGuid, format)
        createPaymentInfo(restClient, ANONYMOUS_USER, cart.guid, MADA_FRICTIONLESS_CHECKOUT_CC_PAYMENT_JSON, MADA_FRICTIONLESS_GET_CC_TOKEN_JSON)
        deactivate3ds()

        when: "guest customer places order"
        def response = placeCheckoutComOrder(ANONYMOUS_USER, cart.guid, EUR_CURRENCY_CODE, format)

        then: "redirect URL is returned"
        with(response) {
            if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
            status == SC_CREATED
            isNotEmpty(data.redirectUrl)
        }

        where:
        format << [XML, JSON]
    }
}
