package com.checkout.hybris.facades.payment.converters.populators;

import com.checkout.hybris.core.authorisation.AuthorizeResponse;
import com.checkout.hybris.facades.beans.AuthorizeResponseData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

/**
 * Populates the AuthorizeResponseData from the AuthorizeResponse from the payment pending response
 */
public class CheckoutComAuthorizeResponsePopulator implements Populator<AuthorizeResponse, AuthorizeResponseData> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void populate(final AuthorizeResponse source, final AuthorizeResponseData target) throws ConversionException {
        validateParameterNotNull(source, "Parameter source cannot be null.");
        validateParameterNotNull(target, "Parameter target cannot be null.");

        target.setIsDataRequired(source.getIsDataRequired());
        target.setIsRedirect(source.getIsRedirect());
        target.setIsSuccess(source.getIsSuccess());
        target.setRedirectUrl(source.getRedirectUrl());
    }
}
