package com.checkout.hybris.facades.payment.ach.magicvalues.impl;

import com.checkout.hybris.facades.beans.AchBankInfoDetailsData;
import de.hybris.bootstrap.annotations.UnitTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class ReturnR07MagicPostalValueAchCheckoutStrategyTest {

    @InjectMocks
    private ReturnR07MagicPostalValueAchCheckoutStrategy testObj;

    private static final String MAGIC_VALUE = "R00007";

    @Test
    public void createAchBankInfoDetailsData_shouldReturnAnObjectWithTheAttributesFieldWithTheMagicValues() {
        final AchBankInfoDetailsData achBankInfoDetailsData = testObj.createAchBankInfoDetailsData();

        assertThat(achBankInfoDetailsData).hasFieldOrPropertyWithValue("accountHolderName", "Tom Black");
        assertThat(achBankInfoDetailsData).hasFieldOrPropertyWithValue("accountType", "CHECKING");
        assertThat(achBankInfoDetailsData).hasFieldOrPropertyWithValue("accountNumber", "082000549");
        assertThat(achBankInfoDetailsData).hasFieldOrPropertyWithValue("bankRouting", "121122676");
        assertThat(achBankInfoDetailsData).hasFieldOrPropertyWithValue("mask", "0000000549");
        assertThat(achBankInfoDetailsData).hasFieldOrPropertyWithValue("institutionName", "Bank of america");
        assertThat(achBankInfoDetailsData).hasFieldOrPropertyWithValue("companyName", "Widget Inc");
    }

    @Test
    public void isApplicable_shouldReturnTrueForPostalCodeR00007() {
        final boolean result = testObj.isApplicable(MAGIC_VALUE);

        assertThat(result).isTrue();
    }

}
