package com.checkout.hybris.facades.payment.converters.populators;

import com.checkout.hybris.core.model.CheckoutComFawryPaymentInfoModel;
import com.checkout.hybris.facades.beans.FawryPaymentInfoData;
import de.hybris.bootstrap.annotations.UnitTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

@UnitTest
public class CheckoutComFawryPaymentInfoReversePopulatorTest {

    private static final String MOBILE_NUMBER = "05653252";

    private CheckoutComFawryPaymentInfoReversePopulator testObj = new CheckoutComFawryPaymentInfoReversePopulator();

    private FawryPaymentInfoData source = new FawryPaymentInfoData();
    private CheckoutComFawryPaymentInfoModel target = new CheckoutComFawryPaymentInfoModel();

    @Test
    public void populate_ShouldPopulateTargetCorrectly() {
        source.setMobileNumber(MOBILE_NUMBER);

        testObj.populate(source, target);

        assertEquals(MOBILE_NUMBER, target.getMobileNumber());
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