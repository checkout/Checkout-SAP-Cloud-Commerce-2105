package com.checkout.hybris.occ.converters.populators;

import com.checkout.hybris.facades.beans.AchPaymentInfoData;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commercewebservicescommons.dto.order.PaymentDetailsWsDTO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.ACH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComAchPaymentInfoDTOToAchPaymentInfoDataPopulatorTest {

    private static final String ACCOUNT_NAME_VALUE = "Account Name";
    private static final String ACCOUNT_TYPE_CHECKING_VALUE = "Checking";
    private static final String ACCOUNT_NUMBER_VALUE = "098765432";
    private static final String PAYMENT_METHOD_VALUE = "Payment Method";
    private static final String BANK_CODE_VALUE = "123";
    private static final String ROUTING_NUMBER_VALUE = "098765";
    private static final String COMPANY_NAME_VALUE = "Company Name";
    private static final String CORP_SAVINGS_ACCOUNT_TYPE_VALUE = "CorpSavings";

    @InjectMocks
    private CheckoutComAchPaymentInfoDTOToAchPaymentInfoDataPopulator testObj;

    private PaymentDetailsWsDTO source = new PaymentDetailsWsDTO();
    private AchPaymentInfoData target = new AchPaymentInfoData();

    @Before
    public void setUp() {
        source.setAccountHolderName(ACCOUNT_NAME_VALUE);
        source.setAccountType(ACCOUNT_TYPE_CHECKING_VALUE);
        source.setAccountNumber(ACCOUNT_NUMBER_VALUE);
        source.setBankCode(BANK_CODE_VALUE);
        source.setPaymentMethod(PAYMENT_METHOD_VALUE);
        source.setRoutingNumber(ROUTING_NUMBER_VALUE);
        source.setType(ACH.name());
    }

    @Test
    public void populate_WhenAccountTypeChecking_ShouldPopulateTargetCorrectlyWithNoCompanyName() {
        testObj.populate(source, target);

        assertEquals(ACCOUNT_NAME_VALUE, target.getAccountHolderName());
        assertEquals(ACCOUNT_NUMBER_VALUE, target.getAccountNumber());
        assertEquals(ACCOUNT_TYPE_CHECKING_VALUE, target.getAccountType());
        assertEquals(ROUTING_NUMBER_VALUE, target.getRoutingNumber());
        assertNull(target.getCompanyName());
    }

    @Test
    public void populate_WhenAccountTypeCorpSavings_ShouldPopulateTargetCorrectly() {
        source.setAccountType(CORP_SAVINGS_ACCOUNT_TYPE_VALUE);
        source.setCompanyName(COMPANY_NAME_VALUE);

        testObj.populate(source, target);

        assertEquals(ACCOUNT_NAME_VALUE, target.getAccountHolderName());
        assertEquals(ACCOUNT_NUMBER_VALUE, target.getAccountNumber());
        assertEquals(CORP_SAVINGS_ACCOUNT_TYPE_VALUE, target.getAccountType());
        assertEquals(ROUTING_NUMBER_VALUE, target.getRoutingNumber());
        assertEquals(COMPANY_NAME_VALUE, target.getCompanyName());
    }

    @Test
    public void populate_WhenAccountTypeChecking_ShouldPopulateTargetCorrectly() {
        testObj.populate(source, target);

        assertEquals(ACCOUNT_NAME_VALUE, target.getAccountHolderName());
        assertEquals(ACCOUNT_NUMBER_VALUE, target.getAccountNumber());
        assertEquals(ACCOUNT_TYPE_CHECKING_VALUE, target.getAccountType());
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
