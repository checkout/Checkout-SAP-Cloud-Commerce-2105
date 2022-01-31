package com.checkout.hybris.facades.payment.converters.populators;

import com.checkout.hybris.core.apm.services.CheckoutComAPMConfigurationService;
import com.checkout.hybris.core.model.CheckoutComAPMPaymentInfoModel;
import com.checkout.hybris.facades.beans.APMPaymentInfoData;
import de.hybris.bootstrap.annotations.UnitTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.GIROPAY;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComApmPaymentInfoReversePopulatorTest {

    @InjectMocks
    private CheckoutComApmPaymentInfoReversePopulator testObj;

    @Mock
    private CheckoutComAPMConfigurationService checkoutComAPMConfigurationServiceMock;

    private APMPaymentInfoData source = new APMPaymentInfoData();
    private CheckoutComAPMPaymentInfoModel target = new CheckoutComAPMPaymentInfoModel();

    @Test
    public void populate_ShouldPopulateTargetCorrectly() {
        when(checkoutComAPMConfigurationServiceMock.isApmUserDataRequired(GIROPAY.name())).thenReturn(false);
        source.setType(GIROPAY.name());

        testObj.populate(source, target);

        assertEquals(GIROPAY.name(), target.getType());
        assertFalse(target.getUserDataRequired());
        assertTrue(target.getDeferred());
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