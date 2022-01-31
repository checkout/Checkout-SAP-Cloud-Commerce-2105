package com.checkout.hybris.addon.converters.populators;

import com.checkout.hybris.addon.forms.PaymentDataForm;
import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.facades.beans.KlarnaPaymentInfoData;
import com.google.common.collect.ImmutableMap;
import de.hybris.bootstrap.annotations.UnitTest;
import org.junit.Test;

import static com.checkout.hybris.addon.converters.populators.CheckoutComKlarnaPaymentInfoDataReversePopulator.AUTHORIZATION_TOKEN_KEY;
import static org.junit.Assert.assertEquals;

@UnitTest
public class CheckoutComKlarnaPaymentInfoDataReversePopulatorTest {

    private static final String KLARNA_AUTH_TOKEN_VALUE = "12345678901_abdajkdjal";

    private CheckoutComKlarnaPaymentInfoDataReversePopulator testObj = new CheckoutComKlarnaPaymentInfoDataReversePopulator();

    private PaymentDataForm source = new PaymentDataForm();
    private KlarnaPaymentInfoData target = new KlarnaPaymentInfoData();

    @Test
    public void populate_ShouldPopulateTargetCorrectly() {
        source.setFormAttributes(ImmutableMap.of(AUTHORIZATION_TOKEN_KEY, KLARNA_AUTH_TOKEN_VALUE));

        testObj.populate(source, target);

        assertEquals(CheckoutComPaymentType.KLARNA.name(), target.getType());
        assertEquals(KLARNA_AUTH_TOKEN_VALUE, target.getAuthorizationToken());
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