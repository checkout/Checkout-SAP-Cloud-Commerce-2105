/*
 * Copyright (c) 2020 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.checkout.hybris.occtests.setup;

import de.hybris.platform.commerceservices.setup.SetupImpexService;
import de.hybris.platform.commerceservices.setup.SetupSyncJobService;
import de.hybris.platform.commercewebservicestests.setup.TestSetupUtils;
import de.hybris.platform.core.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to be used in test suites to manage tests (e.g. start server, load data).
 */
public class CheckoutTestSetupUtils extends TestSetupUtils {

    private static final Logger LOG = LoggerFactory.getLogger(CheckoutTestSetupUtils.class);

    private static final String WS_TEST = "wsTest";

    public static void loadExtensionDataInJunit() {
        Registry.setCurrentTenantByID("junit");
        LOG.info("Importing checkout sample data");
        getSetupImpexService().importImpexFile("/checkoutocctests/import/sampledata/merchant-config.impex", true, false);
        getSetupImpexService().importImpexFile("/checkoutocctests/import/sampledata/essential-data.impex", true, false);
        getSetupImpexService().importImpexFile("/checkoutocctests/import/sampledata/cms-content.impex", true, false);
        getSetupImpexService().importImpexFile("/checkoutocctests/import/sampledata/store.impex", true, false);
        getSetupImpexService().importImpexFile("/checkoutocctests/import/sampledata/product-prices.impex", true, false);
        getSetupImpexService().importImpexFile("/checkoutocctests/import/sampledata/essentialdata-OAuthClientDetails.impex", true, false);
        getSetupSyncJobService().executeCatalogSyncJob(String.format("%sProductCatalog", WS_TEST));
    }

    private static SetupImpexService getSetupImpexService() {
        return Registry.getApplicationContext().getBean("setupImpexService", SetupImpexService.class);
    }

    private static SetupSyncJobService getSetupSyncJobService() {
        return Registry.getApplicationContext().getBean("setupSyncJobService", SetupSyncJobService.class);
    }
}
