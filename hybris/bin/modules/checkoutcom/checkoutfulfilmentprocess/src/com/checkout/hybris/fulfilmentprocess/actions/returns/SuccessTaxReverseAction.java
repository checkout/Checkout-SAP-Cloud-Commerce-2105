/*
 * Copyright (c) 2019 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.checkout.hybris.fulfilmentprocess.actions.returns;

import de.hybris.platform.processengine.action.AbstractProceduralAction;
import de.hybris.platform.returns.model.ReturnProcessModel;
import org.apache.log4j.Logger;


/**
 * Mock implementation to execute when tax was reversed successfully.
 */
public class SuccessTaxReverseAction extends AbstractProceduralAction<ReturnProcessModel> {
	private static final Logger LOG = Logger.getLogger(SuccessTaxReverseAction.class);

	@Override
	public void executeAction(final ReturnProcessModel process)
	{
		LOG.info("Process: " + process.getCode() + " in step " + getClass().getSimpleName());
	}
}
