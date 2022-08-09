/*
 * Copyright (c) 2019 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.checkout.hybris.fulfilmentprocess.jalo;

import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.extension.ExtensionManager;
import com.checkout.hybris.fulfilmentprocess.constants.CheckoutfulfilmentprocessConstants;

public class CheckoutfulfilmentprocessManager extends GeneratedCheckoutfulfilmentprocessManager
{
	public static final CheckoutfulfilmentprocessManager getInstance()
	{
		ExtensionManager em = JaloSession.getCurrentSession().getExtensionManager();
		return (CheckoutfulfilmentprocessManager) em.getExtension(CheckoutfulfilmentprocessConstants.EXTENSIONNAME);
	}
	
}
