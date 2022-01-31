package com.checkout.hybris.facades.payment.converters.populators;

import com.checkout.hybris.core.model.CheckoutComAchPaymentInfoModel;
import com.checkout.hybris.facades.beans.AchPaymentInfoData;
import de.hybris.bootstrap.annotations.UnitTest;
import org.junit.Test;

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.ACH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@UnitTest
public class CheckoutComAchPaymentInfoReversePopulatorTest {

    private static final String ACCOUNT_HOLDER_NAME = "Account Name";
    private static final String ACCOUNT_NUMBER = "1234567890";
    private static final String ROUTING_NUMBER = "3456789";
    private static final String COMPANY_NAME = "Company Name";

    private CheckoutComAchPaymentInfoReversePopulator testObj = new CheckoutComAchPaymentInfoReversePopulator();

    private AchPaymentInfoData source = new AchPaymentInfoData();
    private CheckoutComAchPaymentInfoModel target = new CheckoutComAchPaymentInfoModel();

    @Test
    public void populate_ShouldPopulateTargetCorrectly() {
        source.setAccountHolderName(ACCOUNT_HOLDER_NAME);
        source.setAccountNumber(ACCOUNT_NUMBER);
        source.setAccountType("Checking");
        source.setType(ACH.name());
        source.setRoutingNumber(ROUTING_NUMBER);
        source.setCompanyName(COMPANY_NAME);

        testObj.populate(source, target);

        assertTrue(target.getUserDataRequired());
        assertEquals(ACCOUNT_HOLDER_NAME, target.getAccountHolderName());
        assertEquals(ACCOUNT_NUMBER, target.getAccountNumber());
        assertEquals(ROUTING_NUMBER, target.getRoutingNumber());
        assertEquals(COMPANY_NAME, target.getCompanyName());
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