package com.checkout.hybris.occtests.test.groovy.v2.spock.apmConfiguration

import com.checkout.hybris.occtests.test.groovy.v2.spock.paymentdetails.AbstractCheckoutComPaymentsTest
import de.hybris.bootstrap.annotations.ManualTest
import spock.lang.Unroll

import static groovyx.net.http.ContentType.JSON
import static org.apache.http.HttpStatus.SC_OK

@ManualTest
@Unroll
class CheckoutComKlarnaTest extends AbstractCheckoutComPaymentsTest {

    def "Get Klarna client token data"() {
        given: "authorized customer with cart"
        def customerAndCart = createAndAuthorizeCustomerWithCart(restClient, JSON)
        def customer = customerAndCart[0]
        def cart = customerAndCart[1]
        def address = createAddress(restClient, customer)
        setDeliveryAddressForCart(restClient, customer, cart.code, address.id, JSON)
        addProductToCartOnline(restClient, customer, cart.code, PRODUCT_POWER_SHOT_A480)
        setDeliveryModeForCart(restClient, customer, cart.code, DELIVERY_STANDARD, JSON)
        createBillingAddress(customer.id, cart.code)
        useABC()

        when: "API call to get klarna token data is made"
        def response = restClient.get(
                path: getBasePathWithSite() + '/users/' + customer.id + '/carts/' + cart.code + '/klarna/clientToken',
                contentType: JSON)

        then: "the token data is returned"
        with(response) {
            if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
            status == SC_OK
            isNotEmpty(data.clientToken)
            isNotEmpty(data.paymentMethodCategories)
            data.success == true
            data.instanceId == "e2yCheckoutCom"
        }
    }
}
