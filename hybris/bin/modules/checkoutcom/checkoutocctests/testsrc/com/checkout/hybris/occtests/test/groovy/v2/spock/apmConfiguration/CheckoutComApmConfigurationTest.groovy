package com.checkout.hybris.occtests.test.groovy.v2.spock.apmConfiguration

import com.checkout.hybris.occtests.test.groovy.v2.spock.paymentdetails.AbstractCheckoutComPaymentsTest
import de.hybris.bootstrap.annotations.ManualTest
import org.apache.commons.collections.CollectionUtils
import spock.lang.Unroll

import static org.apache.http.HttpStatus.SC_OK

@ManualTest
@Unroll
class CheckoutComApmConfigurationTest extends AbstractCheckoutComPaymentsTest {

    def "Get available apms for the cart"() {
        given: "a logged in user with a cart"
        def customerWithCart = createAndAuthorizeCustomerWithCart(restClient)
        def customer = customerWithCart[0]
        def cart = customerWithCart[1]
        createBillingAddress(customer.id, cart.code)

        when: "A request to get all available apms for the cart is made"
        def response = restClient.get(
                path: getBasePathWithSite() + '/users/' + customer.id + '/carts/' + cart.code + '/apm/available',
                query: ['fields': FIELD_SET_LEVEL_FULL],
        )

        then: "the available apms are returned"
        with(response) {
            if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
            status == SC_OK
            CollectionUtils.isNotEmpty(responseData.availableApmConfigurations)
            def paypal = responseData.availableApmConfigurations.find { it.code == "PAYPAL" }
            paypal.name == "PayPal"
            paypal.isRedirect == true
            paypal.isUserDataRequired == false
            paypal.media != null
        }
    }
}
