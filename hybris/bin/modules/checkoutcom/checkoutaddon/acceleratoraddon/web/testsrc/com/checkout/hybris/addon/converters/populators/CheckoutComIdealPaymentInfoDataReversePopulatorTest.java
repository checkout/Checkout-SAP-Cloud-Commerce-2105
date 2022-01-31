package com.checkout.hybris.addon.converters.populators;

import com.checkout.hybris.addon.forms.PaymentDataForm;
import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.facades.beans.IdealPaymentInfoData;
import com.google.common.collect.ImmutableMap;
import de.hybris.bootstrap.annotations.UnitTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

@UnitTest
public class CheckoutComIdealPaymentInfoDataReversePopulatorTest {

    private CheckoutComIdealPaymentInfoDataReversePopulator testObj = new CheckoutComIdealPaymentInfoDataReversePopulator();

    private PaymentDataForm source = new PaymentDataForm();
    private IdealPaymentInfoData target = new IdealPaymentInfoData();

    @Test
    public void populate_ShouldPopulateTargetCorrectly() {
        source.setFormAttributes(ImmutableMap.of("bic", "68453120000"));

        testObj.populate(source, target);

        assertEquals(CheckoutComPaymentType.IDEAL.name(), target.getType());
        assertEquals("68453120000", target.getBic());
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