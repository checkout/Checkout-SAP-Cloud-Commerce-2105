package com.checkout.hybris.facades.payment.converters.populators;

import com.checkout.hybris.core.model.CheckoutComAchPaymentInfoModel;
import com.checkout.hybris.facades.beans.AchPaymentInfoData;
import de.hybris.bootstrap.annotations.UnitTest;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
public class CheckoutComAchPaymentInfoReversePopulatorTest {

    private static final String BANK_CODE = "123";
    private static final String MASK = "******7890";
    private static final String ROUTING_NUMBER = "3456789";
    private static final String COMPANY_NAME = "Company Name";
    private static final String ACCOUNT_NUMBER = "1234567890";
    private static final String PAYMENT_METHOD = "Payment Method";
    private static final String ACCOUNT_HOLDER_NAME = "Account Name";

    private final CheckoutComAchPaymentInfoReversePopulator testObj = new CheckoutComAchPaymentInfoReversePopulator();

    private final AchPaymentInfoData source = new AchPaymentInfoData();
    private final CheckoutComAchPaymentInfoModel target = new CheckoutComAchPaymentInfoModel();

    @Test
    public void populate_ShouldPopulateTargetCorrectly() {
        source.setAccountHolderName(ACCOUNT_HOLDER_NAME);
        source.setAccountNumber(ACCOUNT_NUMBER);
        source.setAccountType("Checking");
        source.setBankCode(BANK_CODE);
        source.setPaymentMethod(PAYMENT_METHOD);
        source.setRoutingNumber(ROUTING_NUMBER);
        source.setCompanyName(COMPANY_NAME);
        source.setMask(MASK);

        testObj.populate(source, target);

        assertThat(target.getAccountHolderName()).isEqualTo(ACCOUNT_HOLDER_NAME);
        assertThat(target.getAccountNumber()).isEqualTo(ACCOUNT_NUMBER);
        assertThat(target.getBankCode()).isEqualTo(BANK_CODE);
        assertThat(target.getPaymentMethod()).isEqualTo(PAYMENT_METHOD);
        assertThat(target.getRoutingNumber()).isEqualTo(ROUTING_NUMBER);
        assertThat(target.getCompanyName()).isEqualTo(COMPANY_NAME);
        assertThat(target.getMask()).isEqualTo(MASK);
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
