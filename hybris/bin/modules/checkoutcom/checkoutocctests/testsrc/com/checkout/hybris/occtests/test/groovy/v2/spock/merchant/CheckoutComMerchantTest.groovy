package com.checkout.hybris.occtests.test.groovy.v2.spock.merchant

import de.hybris.bootstrap.annotations.ManualTest
import de.hybris.platform.commercewebservicestests.test.groovy.webservicetests.v2.spock.AbstractSpockFlowTest
import org.apache.http.HttpStatus
import spock.lang.Unroll

@ManualTest
@Unroll
class CheckoutComMerchantTest extends AbstractSpockFlowTest {

    def "An authorized trusted client gets current merchant public key"() {
        given: "An authorized trusted client"
        authorizeTrustedClient(restClient)

        when: "Calling the api endpoint for the merchant key"
        def response = getMerchantPublicKey()

        then: "The key is returned"
        with(response) {
            status == HttpStatus.SC_OK
            responseData != null
        }
    }

    def "An authorized client gets current merchant public key"() {
        given: "A authorized client"
        authorizeClient(restClient)

        when: "Calling the api endpoint for the merchant key"
        def response = getMerchantPublicKey()

        then: "The key is returned"
        with(response) {
            status == HttpStatus.SC_OK
            responseData != null
        }
    }

    def getMerchantPublicKey() {
        return restClient.get(path: getBasePathWithSite() + '/merchantKey')
    }

}
