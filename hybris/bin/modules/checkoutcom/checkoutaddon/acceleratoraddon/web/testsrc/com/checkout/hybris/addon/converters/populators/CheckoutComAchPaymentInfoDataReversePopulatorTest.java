package com.checkout.hybris.addon.converters.populators;

import com.checkout.hybris.addon.forms.PaymentDataForm;
import com.checkout.hybris.facades.beans.AchPaymentInfoData;
import de.hybris.bootstrap.annotations.UnitTest;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.checkout.hybris.addon.converters.populators.CheckoutComAchPaymentInfoDataReversePopulator.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@UnitTest
public class CheckoutComAchPaymentInfoDataReversePopulatorTest {

    private static final String ACCOUNT_NAME_VALUE = "Account Name";
    private static final String ACCOUNT_TYPE_CHECKING_VALUE = "Checking";
    private static final String ACCOUNT_NUMBER_VALUE = "098765432";
    private static final String ROUTING_NUMBER_VALUE = "098765";
    private static final String COMPANY_NAME_VALUE = "Company Name";
    private static final String BANK_CODE_VALUE = "123";
    private static final String PAYMENT_METHOD_VALUE = "Payment Method";

    private CheckoutComAchPaymentInfoDataReversePopulator testObj = new CheckoutComAchPaymentInfoDataReversePopulator();

    private PaymentDataForm source = new PaymentDataForm();
    private AchPaymentInfoData target = new AchPaymentInfoData();
    private Map<String, Object> formAttributes;

    @Before
    public void setUp() {
        formAttributes = new HashMap<>();
        formAttributes.put(ACCOUNT_HOLDER_NAME, ACCOUNT_NAME_VALUE);
        formAttributes.put(ACCOUNT_TYPE_KEY, ACCOUNT_TYPE_CHECKING_VALUE);
        formAttributes.put(ACCOUNT_NUMBER, ACCOUNT_NUMBER_VALUE);
        formAttributes.put(ROUTING_NUMBER_KEY, ROUTING_NUMBER_VALUE);
        formAttributes.put(BANK_CODE_KEY, BANK_CODE_VALUE);
        formAttributes.put(PAYMENT_METHOD_KEY, PAYMENT_METHOD_VALUE);
        source.setFormAttributes(formAttributes);
    }

    @Test
    public void populate_WhenAccountTypeChecking_ShouldPopulateTargetCorrectlyWithNoCompanyName() {
        testObj.populate(source, target);

        assertEquals(ACCOUNT_NAME_VALUE, target.getAccountHolderName());
        assertEquals(ACCOUNT_NUMBER_VALUE, target.getAccountNumber());
        assertEquals(ACCOUNT_TYPE_CHECKING_VALUE, target.getAccountType());
        assertEquals(ROUTING_NUMBER_VALUE, target.getRoutingNumber());
        assertEquals(BANK_CODE_VALUE, target.getBankCode());
        assertEquals(PAYMENT_METHOD_VALUE, target.getPaymentMethod());
        assertNull(target.getCompanyName());
    }

    @Test
    public void populate_WhenAccountTypeCorpSavings_ShouldPopulateTargetCorrectly() {
        formAttributes.put(ACCOUNT_TYPE_KEY, CORP_SAVINGS_ACCOUNT_TYPE_VALUE);
        formAttributes.put(COMPANY_NAME_KEY, COMPANY_NAME_VALUE);

        testObj.populate(source, target);

        assertEquals(ACCOUNT_NAME_VALUE, target.getAccountHolderName());
        assertEquals(ACCOUNT_NUMBER_VALUE, target.getAccountNumber());
        assertEquals(CORP_SAVINGS_ACCOUNT_TYPE_VALUE, target.getAccountType());
        assertEquals(ROUTING_NUMBER_VALUE, target.getRoutingNumber());
        assertEquals(COMPANY_NAME_VALUE, target.getCompanyName());
        assertEquals(BANK_CODE_VALUE, target.getBankCode());
        assertEquals(PAYMENT_METHOD_VALUE, target.getPaymentMethod());
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