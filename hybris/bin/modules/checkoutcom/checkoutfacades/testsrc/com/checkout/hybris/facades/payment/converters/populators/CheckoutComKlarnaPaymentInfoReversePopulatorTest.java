package com.checkout.hybris.facades.payment.converters.populators;

import com.checkout.hybris.core.apm.services.CheckoutComAPMConfigurationService;
import com.checkout.hybris.core.model.CheckoutComKlarnaAPMPaymentInfoModel;
import com.checkout.hybris.facades.beans.KlarnaPaymentInfoData;
import de.hybris.bootstrap.annotations.UnitTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.KLARNA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComKlarnaPaymentInfoReversePopulatorTest {

    private static final String KLARNA_AUTH_TOKEN_VALUE = "klarna_token";

    @InjectMocks
    private CheckoutComKlarnaPaymentInfoReversePopulator testObj;

    @Mock
    private CheckoutComAPMConfigurationService checkoutComAPMConfigurationServiceMock;

    private KlarnaPaymentInfoData source = new KlarnaPaymentInfoData();
    private CheckoutComKlarnaAPMPaymentInfoModel target = new CheckoutComKlarnaAPMPaymentInfoModel();

    @Test
    public void populate_ShouldPopulateTargetCorrectly() {
        when(checkoutComAPMConfigurationServiceMock.isApmUserDataRequired(KLARNA.name())).thenReturn(true);
        source.setType(KLARNA.name());
        source.setAuthorizationToken(KLARNA_AUTH_TOKEN_VALUE);

        testObj.populate(source, target);

        assertEquals(KLARNA_AUTH_TOKEN_VALUE, target.getAuthorizationToken());
        assertFalse(target.getDeferred());
    }

    @Test(expected = IllegalArgumentException.class)
    public void populate_WhenSourceNull_ShouldThrowException() {
        testObj.populate(null, target);
    }

    @Test(expected = IllegalArgumentException.class)
    public void populate_WhenTargetNull_ShouldThrowException() {
        testObj.populate(source, null);
    }
}