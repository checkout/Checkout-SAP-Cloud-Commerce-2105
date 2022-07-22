package com.checkout.hybris.core.setup;

import com.checkout.hybris.core.constants.CheckoutservicesConstants;
import de.hybris.platform.commerceservices.dataimport.impl.CoreDataImportService;
import de.hybris.platform.commerceservices.dataimport.impl.SampleDataImportService;
import de.hybris.platform.commerceservices.setup.AbstractSystemSetup;
import de.hybris.platform.core.initialization.SystemSetup;
import de.hybris.platform.core.initialization.SystemSetup.Process;
import de.hybris.platform.core.initialization.SystemSetup.Type;
import de.hybris.platform.core.initialization.SystemSetupContext;
import de.hybris.platform.core.initialization.SystemSetupParameter;
import de.hybris.platform.core.initialization.SystemSetupParameterMethod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import java.util.ArrayList;
import java.util.List;


/**
 * This class provides hooks into the system's initialization and update processes.
 */
@SystemSetup(extension = CheckoutservicesConstants.EXTENSIONNAME)
public class InitialDataSystemSetup extends AbstractSystemSetup {
    @SuppressWarnings("unused")
    protected static final Logger LOG = LogManager.getLogger(InitialDataSystemSetup.class);

    private static final String IMPORT_CORE_DATA = "importCoreData";

    private CoreDataImportService coreDataImportService;
    private SampleDataImportService sampleDataImportService;

    /**
     * Generates the Dropdown and Multi-select boxes for the project data import
     */
    @Override
    @SystemSetupParameterMethod
    public List<SystemSetupParameter> getInitializationOptions() {
        final List<SystemSetupParameter> params = new ArrayList<>();

        params.add(createBooleanSystemSetupParameter(IMPORT_CORE_DATA, "Import Core Data", true));
        // Add more Parameters here as you require

        return params;
    }

    /**
     * Implement this method to create initial objects. This method will be called by system creator during
     * initialization and system update. Be sure that this method can be called repeatedly.
     *
     * @param context the context provides the selected parameters and values
     */
    @SystemSetup(type = Type.ESSENTIAL, process = Process.ALL)
    public void createEssentialData(final SystemSetupContext context) {
        // Add Essential Data here as you require
    }

    /**
     * Implement this method to create data that is used in your project. This method will be called during the system
     * initialization. <br>
     * Add import data for each site you have configured
     *
     * <pre>
     * final List<ImportData> importData = new ArrayList<ImportData>();
     *
     * final ImportData sampleImportData = new ImportData();
     * sampleImportData.setProductCatalogName(SAMPLE_PRODUCT_CATALOG_NAME);
     * sampleImportData.setContentCatalogNames(Arrays.asList(SAMPLE_CONTENT_CATALOG_NAME));
     * sampleImportData.setStoreNames(Arrays.asList(SAMPLE_STORE_NAME));
     * importData.add(sampleImportData);
     *
     * getCoreDataImportService().execute(this, context, importData);
     * getEventService().publishEvent(new CoreDataImportedEvent(context, importData));
     *
     * getSampleDataImportService().execute(this, context, importData);
     * getEventService().publishEvent(new SampleDataImportedEvent(context, importData));
     * </pre>
     *
     * @param context the context provides the selected parameters and values
     */
    @SystemSetup(type = Type.PROJECT, process = Process.ALL)
    public void createProjectData(final SystemSetupContext context) {
        /*
         * Add import data for each site you have configured
         */
    }

    public CoreDataImportService getCoreDataImportService() {
        return coreDataImportService;
    }

    @Required
    public void setCoreDataImportService(final CoreDataImportService coreDataImportService) {
        this.coreDataImportService = coreDataImportService;
    }

    public SampleDataImportService getSampleDataImportService() {
        return sampleDataImportService;
    }

    @Required
    public void setSampleDataImportService(final SampleDataImportService sampleDataImportService) {
        this.sampleDataImportService = sampleDataImportService;
    }
}
