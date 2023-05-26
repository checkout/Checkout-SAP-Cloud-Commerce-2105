package com.checkout.hybris.facades.payment.converters.populators;

import com.checkout.hybris.core.enums.AchAccountType;
import com.checkout.hybris.core.model.CheckoutComACHConsentModel;
import com.checkout.hybris.facades.beans.AchBankInfoDetailsData;
import de.hybris.bootstrap.annotations.UnitTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComAchConsentReversePopulatorTest {

    private static final String ACCOUNT = "account";
    private static final String BANK_NAME = "bankName";
    private static final String ACCOUNT_NUMBER = "accountNumber";
    private static final String ROUTING_NUMBER = "routingNumber";
    private static final String ACCOUNT_HOLDER_NAME = "accountHolderName";
    private static final String ACCOUNT_HOLDER_EMAIL = "accountHolderEmail";

    @InjectMocks
    private CheckoutComAchConsentReversePopulator testObj;

    @Mock
    private AchBankInfoDetailsData achBankInfoDetailsDataMock;

    private final CheckoutComACHConsentModel checkoutComACHConsentModelStub = new CheckoutComACHConsentModel();

    @Test(expected = IllegalArgumentException.class)
    public void populate_whenSourceIsNull_shouldThrowIllegalArgumentException() {
        testObj.populate(null, checkoutComACHConsentModelStub);
    }

    @Test(expected = IllegalArgumentException.class)
    public void populate_whenTargetIsNull_shouldThrowIllegalArgumentException() {
        testObj.populate(achBankInfoDetailsDataMock, null);
    }

    @Test
    public void populate_whenACHAccountTypeIsNull_shouldPopulateCommonFields_andSetAccountAsNull() {
        commonPrepareFields();
        when(achBankInfoDetailsDataMock.getAccountType()).thenReturn(null);

        testObj.populate(achBankInfoDetailsDataMock, checkoutComACHConsentModelStub);

        commonCheckFields();
        assertThat(checkoutComACHConsentModelStub.getAccountType()).isNull();
    }

    @Test
    public void populate_whenACHAccountTypeIsNotNull_shouldPopulateCommonFields_andSetAccountAsAccountType() {
        commonPrepareFields();
        when(achBankInfoDetailsDataMock.getAccountType()).thenReturn(ACCOUNT);

        testObj.populate(achBankInfoDetailsDataMock, checkoutComACHConsentModelStub);

        commonCheckFields();
        assertThat(checkoutComACHConsentModelStub.getAccountType()).isEqualTo(AchAccountType.valueOf(ACCOUNT));
    }

    private void commonPrepareFields() {
        when(achBankInfoDetailsDataMock.getAccountHolderName()).thenReturn(ACCOUNT_HOLDER_NAME);
        when(achBankInfoDetailsDataMock.getAccountHolderEmail()).thenReturn(ACCOUNT_HOLDER_EMAIL);
        when(achBankInfoDetailsDataMock.getAccountNumber()).thenReturn(ACCOUNT_NUMBER);
        when(achBankInfoDetailsDataMock.getInstitutionName()).thenReturn(BANK_NAME);
        when(achBankInfoDetailsDataMock.getBankRouting()).thenReturn(ROUTING_NUMBER);
    }

    private void commonCheckFields() {
        assertThat(checkoutComACHConsentModelStub.getPayer()).isEqualTo(ACCOUNT_HOLDER_NAME);
        assertThat(checkoutComACHConsentModelStub.getEmail()).isEqualTo(ACCOUNT_HOLDER_EMAIL);
        assertThat(checkoutComACHConsentModelStub.getAccountNumber()).isEqualTo(ACCOUNT_NUMBER);
        assertThat(checkoutComACHConsentModelStub.getBankName()).isEqualTo(BANK_NAME);
        assertThat(checkoutComACHConsentModelStub.getRoutingNumber()).isEqualTo(ROUTING_NUMBER);
    }
}
