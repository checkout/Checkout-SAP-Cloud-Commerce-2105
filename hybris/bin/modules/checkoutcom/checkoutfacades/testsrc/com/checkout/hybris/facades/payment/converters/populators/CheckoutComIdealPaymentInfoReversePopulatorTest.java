package com.checkout.hybris.facades.payment.converters.populators;

import com.checkout.hybris.core.model.CheckoutComIdealPaymentInfoModel;
import com.checkout.hybris.facades.beans.IdealPaymentInfoData;
import de.hybris.bootstrap.annotations.UnitTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

@UnitTest
public class CheckoutComIdealPaymentInfoReversePopulatorTest {

    private static final String BIC = "544558485";

    private CheckoutComIdealPaymentInfoReversePopulator testObj = new CheckoutComIdealPaymentInfoReversePopulator();

    private IdealPaymentInfoData source = new IdealPaymentInfoData();
    private CheckoutComIdealPaymentInfoModel target = new CheckoutComIdealPaymentInfoModel();

    @Test
    public void populate_ShouldPopulateTargetCorrectly() {
        source.setBic(BIC);

        testObj.populate(source, target);

        assertEquals(BIC, target.getBic());
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