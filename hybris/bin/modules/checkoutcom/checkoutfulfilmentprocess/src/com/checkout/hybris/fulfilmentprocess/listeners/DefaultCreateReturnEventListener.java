/*submits
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 *
 */

package com.checkout.hybris.fulfilmentprocess.listeners;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.commerceservices.enums.SiteChannel;
import de.hybris.platform.commerceservices.event.AbstractSiteEventListener;
import de.hybris.platform.commerceservices.event.CreateReturnEvent;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.returns.model.ReturnProcessModel;
import de.hybris.platform.returns.model.ReturnRequestModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.util.ServicesUtil;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.store.services.BaseStoreService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;


/**
 * Listener for order returns. Creates and starts the return process based on the return request.
 */
public class DefaultCreateReturnEventListener extends AbstractSiteEventListener<CreateReturnEvent> {

    protected static final Logger LOG = LogManager.getLogger(DefaultCreateReturnEventListener.class);
    protected static final String SEPARATOR = "-";

    private BusinessProcessService businessProcessService;
    private BaseStoreService baseStoreService;
    private ModelService modelService;
    private Set<SiteChannel> supportedSiteChannels;

    public DefaultCreateReturnEventListener(final BusinessProcessService businessProcessService,
                                            final BaseStoreService baseStoreService, final ModelService modelService,
                                            final Set<SiteChannel> supportedSiteChannels) {
        this.businessProcessService = businessProcessService;
        this.baseStoreService = baseStoreService;
        this.modelService = modelService;
        setSupportedSiteChannels(supportedSiteChannels);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onSiteEvent(final CreateReturnEvent event) {
        final ReturnRequestModel returnRequest = event.getReturnRequest();
        ServicesUtil.validateParameterNotNullStandardMessage("event.returnRequest", returnRequest);

        final BaseStoreModel store = getBaseStore(returnRequest);

        if (store == null) {
            LOG.info("Unable to start return process for return request [{}]. Store not set on Order linked to the return request and no current base store defined in session.",
                    returnRequest.getCode());
        } else {
            final String processDefinitionName = store.getCreateReturnProcessCode();
            if (processDefinitionName == null || processDefinitionName.isEmpty()) {
                LOG.error("Unable to start return process for return request [{}]. Store [{}] has missing CreateReturnProcessCode",
                        returnRequest.getCode(), store.getUid());
            } else {
                startBusinessProcess(processDefinitionName, returnRequest);
            }
        }
    }

    /**
     * Starts the business process given a process definition and a return request
     *
     * @param processDefinitionName the process definition name of the process to start
     * @param returnRequest the return request
     */
    protected void startBusinessProcess(final String processDefinitionName, final ReturnRequestModel returnRequest) {
        final String processCode = processDefinitionName + SEPARATOR + returnRequest.getCode() + SEPARATOR
                + System.currentTimeMillis();
        final ReturnProcessModel businessProcess = businessProcessService.createProcess(processCode,
                processDefinitionName);
        businessProcess.setReturnRequest(returnRequest);
        modelService.save(businessProcess);
        businessProcessService.startProcess(businessProcess);
        LOG.info("Started the process [{}]", processCode);
    }

    /**
     * Tries to retrieve the base store from the order. If not it uses {@see BaseStoreService}
     * to get the current base store
     *
     * @param returnRequest the return request
     * @return the base store
     */
    protected BaseStoreModel getBaseStore(final ReturnRequestModel returnRequest) {
        BaseStoreModel store = returnRequest.getOrder().getStore();
        if (store == null) {
            store = baseStoreService.getCurrentBaseStore();
        }
        return store;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean shouldHandleEvent(final CreateReturnEvent event) {
        final ReturnRequestModel returnRequest = event.getReturnRequest();
        ServicesUtil.validateParameterNotNullStandardMessage("event.return", returnRequest);
        final BaseSiteModel site = returnRequest.getOrder().getSite();
        ServicesUtil.validateParameterNotNullStandardMessage("event.return.site", site);
        return supportedSiteChannels.contains(site.getChannel());
    }

    public void setSupportedSiteChannels(final Set<SiteChannel> supportedSiteChannels) {
        this.supportedSiteChannels = supportedSiteChannels;
    }

}
