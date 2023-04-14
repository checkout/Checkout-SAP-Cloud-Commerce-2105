package com.checkout.hybris.facades.payment.converters.populators;

import com.checkout.hybris.core.payment.ach.service.CheckoutComPlaidLinkService;
import com.checkout.hybris.facades.beans.AchBankInfoDetailsData;
import com.checkout.hybris.facades.beans.PlaidLinkCreationResponse;
import com.plaid.client.model.*;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComPublicTokenExchangeResponseToAchBankInfoDetailPopulatorTest {

    private static final String MASK = "mask";
    private static final String ACCOUNT_ID = "accountId";
    private static final String OWNER_NAME = "ownerName";
    private static final String ACCESS_TOKEN = "accessToken";
    private static final String BANK_ROUTING = "bankRouting";
    private static final String EMAIL_ADDRESS = "emailAddress";
    private static final String OFFICIAL_NAME = "officialName";
    private static final String ACCOUNT_NUMBER = "accountNumber";
    private static final String ERROR_MSG = "Could not convert ItemPublicTokenExchangeResponse into ACH bank info " +
        "data. Reason: ";

    @InjectMocks
    private CheckoutComPublicTokenExchangeResponseToAchBankInfoDetailPopulator testObj;

    @Mock
    private CheckoutComPlaidLinkService checkoutComPlaidLinkServiceMock;

    @Mock
    private Owner ownerMock;
    @Mock
    private NumbersACH numbersACHMock;
    @Mock
    private AccountBase accountBaseMock;
    @Mock
    private Email email1Mock, email2Mock;
    @Mock
    private AuthGetNumbers authGetNumbersMock;
    @Mock
    private AuthGetResponse authGetResponseMock;
    @Mock
    private AccountIdentity accountIdentityMock;
    @Mock
    private IdentityGetResponse identityGetResponseMock;
    @Mock
    private ItemPublicTokenExchangeResponse itemPublicTokenExchangeResponseMock;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private PlaidLinkCreationResponse plaidLinkCreationResponseMock;

    private final AchBankInfoDetailsData achBankInfoDetailsDataStub = new AchBankInfoDetailsData();

    @Before
    public void setUp() throws Exception {
        when(plaidLinkCreationResponseMock.getMetadata().getAccount_id()).thenReturn(
            CheckoutComPublicTokenExchangeResponseToAchBankInfoDetailPopulatorTest.ACCOUNT_ID);
        when(itemPublicTokenExchangeResponseMock.getAccessToken()).thenReturn(ACCESS_TOKEN);
        when(checkoutComPlaidLinkServiceMock.getACHInfo(ACCESS_TOKEN)).thenReturn(authGetResponseMock);
        when(authGetResponseMock.getAccounts()).thenReturn(List.of(accountBaseMock));
        when(authGetResponseMock.getNumbers()).thenReturn(authGetNumbersMock);
        when(authGetNumbersMock.getAch()).thenReturn(List.of(numbersACHMock));
        when(numbersACHMock.getAccountId()).thenReturn(ACCOUNT_ID);
        when(numbersACHMock.getAccount()).thenReturn(ACCOUNT_NUMBER);
        when(numbersACHMock.getRouting()).thenReturn(BANK_ROUTING);
        when(accountBaseMock.getAccountId()).thenReturn(ACCOUNT_ID);
        when(accountBaseMock.getSubtype()).thenReturn(AccountSubtype.EBT);
        when(accountBaseMock.getMask()).thenReturn(MASK);
        when(accountBaseMock.getOfficialName()).thenReturn(OFFICIAL_NAME);
        when(checkoutComPlaidLinkServiceMock.getAccountHolderInfo(ACCESS_TOKEN, ACCOUNT_ID)).thenReturn(identityGetResponseMock);
        when(identityGetResponseMock.getAccounts()).thenReturn(List.of(accountIdentityMock));
        when(accountIdentityMock.getOwners()).thenReturn(List.of(ownerMock));
        when(ownerMock.getNames()).thenReturn(List.of(OWNER_NAME));
        when(ownerMock.getEmails()).thenReturn(List.of(email1Mock, email2Mock));
        when(email1Mock.getPrimary()).thenReturn(false);
        when(email2Mock.getPrimary()).thenReturn(true);
        when(email2Mock.getData()).thenReturn(EMAIL_ADDRESS);
    }

    @Test
    public void populate_shouldPopulateFields() {
        testObj.populate(Pair.of(itemPublicTokenExchangeResponseMock, plaidLinkCreationResponseMock),
                         achBankInfoDetailsDataStub);

        assertThat(achBankInfoDetailsDataStub.getAccountType()).isEqualTo(AccountSubtype.EBT.getValue());
        assertThat(achBankInfoDetailsDataStub.getAccountNumber()).isEqualTo(ACCOUNT_NUMBER);
        assertThat(achBankInfoDetailsDataStub.getAccountHolderName()).isEqualTo(OWNER_NAME);
        assertThat(achBankInfoDetailsDataStub.getAccountHolderEmail()).isEqualTo(EMAIL_ADDRESS);
        assertThat(achBankInfoDetailsDataStub.getBankRouting()).isEqualTo(BANK_ROUTING);
        assertThat(achBankInfoDetailsDataStub.getMask()).isEqualTo(MASK);
        assertThat(achBankInfoDetailsDataStub.getInstitutionName()).isEqualTo(OFFICIAL_NAME);
    }

    @Test
    public void populate_whenACHInfoIsNotAccessible_shouldThrowException() throws IOException {
        when(checkoutComPlaidLinkServiceMock.getACHInfo(ACCESS_TOKEN)).thenThrow(IOException.class);

        final Throwable throwable = catchThrowable(
            () -> testObj.populate(Pair.of(itemPublicTokenExchangeResponseMock, plaidLinkCreationResponseMock),
                                   achBankInfoDetailsDataStub));

        assertThat(throwable)
            .isInstanceOf(ConversionException.class)
            .hasMessageContaining(ERROR_MSG);
        checkNullFields();
    }

    @Test
    public void populate_whenAccountHolderInfoIsNotAccessible_shouldThrowException() throws IOException {
        when(checkoutComPlaidLinkServiceMock.getAccountHolderInfo(ACCESS_TOKEN, ACCOUNT_ID)).thenThrow(
            IOException.class);

        final Throwable throwable = catchThrowable(
            () -> testObj.populate(Pair.of(itemPublicTokenExchangeResponseMock, plaidLinkCreationResponseMock),
                                   achBankInfoDetailsDataStub));

        assertThat(throwable)
            .isInstanceOf(ConversionException.class)
            .hasMessageContaining(ERROR_MSG);
        checkNullFields();
    }

    @Test
    public void populate_whenNoMailIsPrimary_shouldThrowException() {
        when(email2Mock.getPrimary()).thenReturn(false);

        final Throwable throwable = catchThrowable(
            () -> testObj.populate(Pair.of(itemPublicTokenExchangeResponseMock, plaidLinkCreationResponseMock),
                                   achBankInfoDetailsDataStub));

        assertThat(throwable)
            .isInstanceOf(ConversionException.class)
            .hasMessageContaining(ERROR_MSG);
        checkNullFields();
    }

    private void checkNullFields() {
        assertThat(achBankInfoDetailsDataStub.getAccountType()).isNull();
        assertThat(achBankInfoDetailsDataStub.getAccountNumber()).isNull();
        assertThat(achBankInfoDetailsDataStub.getAccountHolderName()).isNull();
        assertThat(achBankInfoDetailsDataStub.getAccountHolderEmail()).isNull();
        assertThat(achBankInfoDetailsDataStub.getBankRouting()).isNull();
        assertThat(achBankInfoDetailsDataStub.getMask()).isNull();
        assertThat(achBankInfoDetailsDataStub.getInstitutionName()).isNull();
    }
}
