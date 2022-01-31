package com.checkout.hybris.facades.payment.converters.populators;

import com.checkout.hybris.core.authorisation.AuthorizeResponse;
import com.checkout.hybris.facades.beans.AuthorizeResponseData;
import de.hybris.bootstrap.annotations.UnitTest;
import org.junit.Test;

import static org.junit.Assert.*;

@UnitTest
public class CheckoutComAuthorizeResponsePopulatorTest {

    private static final String REDIRET_URL = "rediret_url";

    private CheckoutComAuthorizeResponsePopulator testObj = new CheckoutComAuthorizeResponsePopulator();

    private AuthorizeResponse source = new AuthorizeResponse();
    private AuthorizeResponseData target = new AuthorizeResponseData();

    @Test(expected = IllegalArgumentException.class)
    public void populate_WhenSourceNull_ShouldThrowException() {
        testObj.populate(null, target);
    }

    @Test(expected = IllegalArgumentException.class)
    public void populate_WhenTargetNull_ShouldThrowException() {
        testObj.populate(source, null);
    }

    @Test
    public void populate_WhenSourceIsCorrect_ShouldPopulateTheTarget() {
        source.setRedirectUrl(REDIRET_URL);
        source.setIsDataRequired(false);
        source.setIsRedirect(true);
        source.setIsSuccess(true);

        testObj.populate(source, target);

        assertTrue(target.getIsRedirect());
        assertTrue(target.getIsSuccess());
        assertFalse(target.getIsDataRequired());
        assertEquals(REDIRET_URL, target.getRedirectUrl());
    }
}