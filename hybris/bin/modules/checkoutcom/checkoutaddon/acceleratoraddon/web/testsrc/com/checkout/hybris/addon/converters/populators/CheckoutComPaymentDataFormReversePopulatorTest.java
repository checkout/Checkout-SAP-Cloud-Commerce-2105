package com.checkout.hybris.addon.converters.populators;

import com.checkout.hybris.addon.forms.PaymentDataForm;
import com.checkout.hybris.facades.beans.APMPaymentInfoData;
import com.google.common.collect.ImmutableMap;
import de.hybris.bootstrap.annotations.UnitTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

@UnitTest
public class CheckoutComPaymentDataFormReversePopulatorTest {

    private static final String TYPE_VALUE = "TYPE";

    private CheckoutComPaymentDataFormReversePopulator testObj = new CheckoutComPaymentDataFormReversePopulator();

    private PaymentDataForm source = new PaymentDataForm();
    private APMPaymentInfoData target = new APMPaymentInfoData();

    @Test
    public void populate_ShouldPopulateTargetCorrectly() {
        source.setFormAttributes(ImmutableMap.of("type", TYPE_VALUE));

        testObj.populate(source, target);

        assertEquals(TYPE_VALUE, target.getType());
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