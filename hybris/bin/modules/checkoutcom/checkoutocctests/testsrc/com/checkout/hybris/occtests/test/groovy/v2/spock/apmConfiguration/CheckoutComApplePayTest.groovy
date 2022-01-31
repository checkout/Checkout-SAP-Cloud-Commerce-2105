package com.checkout.hybris.occtests.test.groovy.v2.spock.apmConfiguration

import com.checkout.hybris.occtests.test.groovy.v2.spock.paymentdetails.AbstractCheckoutComPaymentsTest
import de.hybris.bootstrap.annotations.ManualTest
import spock.lang.Unroll

import static groovyx.net.http.ContentType.JSON
import static org.apache.http.HttpStatus.SC_OK

@ManualTest
@Unroll
class CheckoutComApplePayTest extends AbstractCheckoutComPaymentsTest {

    def "Get ApplePay configuration"() {
        given: "authorized customer with cart"
        def customerAndCart = createAndAuthorizeCustomerWithCart(restClient, JSON)
        def customer = customerAndCart[0]
        def cart = customerAndCart[1]
        def address = createAddress(restClient, customer)
        setDeliveryAddressForCart(restClient, customer, cart.code, address.id, JSON)
        addProductToCartOnline(restClient, customer, cart.code, PRODUCT_POWER_SHOT_A480)
        setDeliveryModeForCart(restClient, customer, cart.code, DELIVERY_STANDARD, JSON)
        createBillingAddress(customer.id, cart.code)

        when: "API call to get applePay configuration is made"
        def response = restClient.get(
                path: getBasePathWithSite() + '/users/' + customer.id + '/carts/' + cart.code + '/applepay/paymentRequest',
                contentType: JSON)

        then: "the configuration is returned"
        with(response) {
            if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
            status == SC_OK
            data.countryCode == "GB"
            data.currencyCode == "EUR"
            isNotEmpty(data.merchantCapabilities)
            isNotEmpty(data.supportedNetworks)
            isNotEmpty(data.requiredBillingContactFields)
            data.requiredBillingContactFields[0] == "postalAddress"
            data.total.amount == "29.2"
            data.total.label == "e2yCheckoutCom"
            data.total.type == "final"
        }
    }
}
