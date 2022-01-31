package com.checkout.hybris.facades.order.converters.populators;

import com.checkout.hybris.core.model.CheckoutComCreditCardPaymentInfoModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commercefacades.order.data.CCPaymentInfoData;
import org.junit.Assert;
import org.junit.Test;

@UnitTest
public class CheckoutComCreditCardPaymentInfoPopulatorTest {

    private static final String TOKEN = "token";

    private CheckoutComCreditCardPaymentInfoPopulator testObj = new CheckoutComCreditCardPaymentInfoPopulator();

    private CCPaymentInfoData target = new CCPaymentInfoData();

    @Test(expected = IllegalArgumentException.class)
    public void populate_WithNullSource_ShouldThrowException() {
        testObj.populate(null, target);
    }

    @Test(expected = IllegalArgumentException.class)
    public void populate_WithNullTarget_ShouldThrowException() {
        final CheckoutComCreditCardPaymentInfoModel source = new CheckoutComCreditCardPaymentInfoModel();
        source.setCardToken(TOKEN);

        testObj.populate(source, null);
    }

    @Test
    public void populate_ShouldPopulateEverythingCorrectly() {
        final CheckoutComCreditCardPaymentInfoModel source = new CheckoutComCreditCardPaymentInfoModel();
        source.setCardToken(TOKEN);

        testObj.populate(source, target);

        Assert.assertEquals(TOKEN, target.getPaymentToken());
    }
}
