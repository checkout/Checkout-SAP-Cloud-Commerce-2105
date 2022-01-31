package com.checkout.hybris.addon.converters.populators;

import com.checkout.hybris.addon.forms.PaymentDataForm;
import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.facades.beans.FawryPaymentInfoData;
import com.google.common.collect.ImmutableMap;
import de.hybris.bootstrap.annotations.UnitTest;
import org.junit.Test;

import static com.checkout.hybris.addon.converters.populators.CheckoutComFawryPaymentInfoDataReversePopulator.MOBILE_NUMBER_KEY;
import static org.junit.Assert.assertEquals;

@UnitTest
public class CheckoutComFawryPaymentInfoDataReversePopulatorTest {

    private static final String MOBILE_NUMBER_VALUE = "12345678912";

    private CheckoutComFawryPaymentInfoDataReversePopulator testObj = new CheckoutComFawryPaymentInfoDataReversePopulator();

    private PaymentDataForm source = new PaymentDataForm();
    private FawryPaymentInfoData target = new FawryPaymentInfoData();

    @Test
    public void populate_ShouldPopulateTargetCorrectly() {
        source.setFormAttributes(ImmutableMap.of(MOBILE_NUMBER_KEY, MOBILE_NUMBER_VALUE));

        testObj.populate(source, target);

        assertEquals(CheckoutComPaymentType.FAWRY.name(), target.getType());
        assertEquals(MOBILE_NUMBER_VALUE, target.getMobileNumber());
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
