/*
 * Copyright (c) 2020 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.checkout.hybris.occtests.test.groovy.v2.spock

import com.checkout.hybris.occtests.setup.CheckoutTestSetupUtils
import com.checkout.hybris.occtests.test.groovy.v2.spock.apmConfiguration.CheckoutComApmConfigurationTest
import com.checkout.hybris.occtests.test.groovy.v2.spock.apmConfiguration.CheckoutComApplePayTest
import com.checkout.hybris.occtests.test.groovy.v2.spock.apmConfiguration.CheckoutComGooglePayTest
import com.checkout.hybris.occtests.test.groovy.v2.spock.apmConfiguration.CheckoutComKlarnaTest
import com.checkout.hybris.occtests.test.groovy.v2.spock.merchant.CheckoutComMerchantTest
import com.checkout.hybris.occtests.test.groovy.v2.spock.orders.*
import com.checkout.hybris.occtests.test.groovy.v2.spock.paymentdetails.*
import de.hybris.bootstrap.annotations.IntegrationTest
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@RunWith(Suite.class)
@Suite.SuiteClasses([CheckoutComMerchantTest, CheckoutComCCPaymentsTest, CheckoutComAPMPaymentsTest,
        CheckoutComApmConfigurationTest, CheckoutComCCOrdersTest, CheckoutComAPMOrdersTest, CheckoutComApplePayTest,
        CheckoutComGooglePayTest, CheckoutComKlarnaTest])
@IntegrationTest
class AllSpockTests {

    private static final Logger LOG = LoggerFactory.getLogger(AllSpockTests.class)

    @BeforeClass
    public static void setUpClass() {
        CheckoutTestSetupUtils.loadData();
        CheckoutTestSetupUtils.loadExtensionDataInJunit();
        CheckoutTestSetupUtils.startServer();
    }

    @AfterClass
    public static void tearDown() {
        CheckoutTestSetupUtils.stopServer();
        CheckoutTestSetupUtils.cleanData();
    }

    @Test
    public static void testing() {
        //dummy test class
    }
}
