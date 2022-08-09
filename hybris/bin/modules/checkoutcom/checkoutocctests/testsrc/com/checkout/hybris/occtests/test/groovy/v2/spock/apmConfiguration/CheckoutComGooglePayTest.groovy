package com.checkout.hybris.occtests.test.groovy.v2.spock.apmConfiguration

import com.checkout.hybris.occtests.test.groovy.v2.spock.paymentdetails.AbstractCheckoutComPaymentsTest
import de.hybris.bootstrap.annotations.ManualTest
import spock.lang.Unroll

import static groovyx.net.http.ContentType.JSON
import static org.apache.http.HttpStatus.SC_OK

@ManualTest
@Unroll
class CheckoutComGooglePayTest extends AbstractCheckoutComPaymentsTest {

    def "Get GooglePay configuration"() {
        given: "authorized customer with cart"
        def customerAndCart = createAndAuthorizeCustomerWithCart(restClient, JSON)
        def customer = customerAndCart[0]
        def cart = customerAndCart[1]
        def address = createAddress(restClient, customer)
        setDeliveryAddressForCart(restClient, customer, cart.code, address.id, JSON)
        addProductToCartOnline(restClient, customer, cart.code, PRODUCT_POWER_SHOT_A480)
        setDeliveryModeForCart(restClient, customer, cart.code, DELIVERY_STANDARD, JSON)
        createBillingAddress(customer.id, cart.code)

        when: "API call to get googlePay configuration is made"
        def response = restClient.get(
                path: getBasePathWithSite() + '/users/' + customer.id + '/carts/' + cart.code + '/google/merchant-configuration',
                contentType: JSON)

        then: "the configuration is returned"
        with(response) {
            if (isNotEmpty(data) && isNotEmpty(data.errors)) println(data)
            status == SC_OK

            isNotEmpty(data.baseCardPaymentMethod)
            data.baseCardPaymentMethod.type == 'CARD'
            isNotEmpty(data.baseCardPaymentMethod.parameters)
            isNotEmpty(data.baseCardPaymentMethod.parameters.allowedAuthMethods)
            isNotEmpty(data.baseCardPaymentMethod.parameters.billingAddressParameters)
            data.baseCardPaymentMethod.parameters.billingAddressParameters.format == 'FULL'
            data.baseCardPaymentMethod.parameters.billingAddressRequired == Boolean.TRUE

            isNotEmpty(data.clientSettings)
            data.clientSettings.environment == 'TEST'

            isNotEmpty(data.transactionInfo)
            data.transactionInfo.currencyCode == 'EUR'
            data.transactionInfo.totalPriceStatus == 'ESTIMATED'

            data.gateway == 'checkoutltd'
            isNotEmpty(data.gatewayMerchantId)
            data.merchantId == '01234567890123456789'
            data.merchantName == 'e2yCheckoutCom'
        }
    }
}
