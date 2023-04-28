package com.checkout.hybris.facades.payment.converters.populators;

import com.checkout.hybris.core.payment.ach.service.CheckoutComPlaidLinkService;
import com.checkout.hybris.facades.beans.AchBankInfoDetailsData;
import com.checkout.hybris.facades.beans.PlaidLinkCreationResponse;
import com.plaid.client.model.*;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Objects;

/**
 * Populator that fills the details to convert a {@link ItemPublicTokenExchangeResponse} into a
 * {@link AchBankInfoDetailsData}
 */
public class CheckoutComPublicTokenExchangeResponseToAchBankInfoDetailPopulator implements Populator<Pair<ItemPublicTokenExchangeResponse, PlaidLinkCreationResponse>, AchBankInfoDetailsData> {

    protected final CheckoutComPlaidLinkService checkoutComPlaidLinkService;

    public CheckoutComPublicTokenExchangeResponseToAchBankInfoDetailPopulator(final CheckoutComPlaidLinkService checkoutComPlaidLinkService) {
        this.checkoutComPlaidLinkService = checkoutComPlaidLinkService;
    }

    @Override
    public void populate(final Pair<ItemPublicTokenExchangeResponse, PlaidLinkCreationResponse> itemPublicTokenExchangeResponsePlaidLinkCreationResponsePair, final AchBankInfoDetailsData achBankInfoDetailsData) throws ConversionException {
        populateExchangeResponse(
            itemPublicTokenExchangeResponsePlaidLinkCreationResponsePair.getLeft(),
            itemPublicTokenExchangeResponsePlaidLinkCreationResponsePair.getRight(),
            achBankInfoDetailsData);
    }

    public void populateExchangeResponse(final ItemPublicTokenExchangeResponse sourceExchangeResponse,
                                         final PlaidLinkCreationResponse sourceCreationResponse,
                                         final AchBankInfoDetailsData target) throws ConversionException {
        final String accessToken = sourceExchangeResponse.getAccessToken();
        try {
            final AuthGetResponse authGetResponse = checkoutComPlaidLinkService.getACHInfo(accessToken);
            final String selectedBankId = getBankAccountIdFromCreationResponse(sourceCreationResponse);
            if (bankAccountExists(authGetResponse, selectedBankId)) {
                populateAchBankInfoDetailsData(
                    accessToken, authGetResponse, selectedBankId, target);
            } else {
                throw new InvalidParameterException(
                    "There is no bank account that fits the with the param sent by the frontend");
            }
        }
        catch (final IOException e) {
            throw new ConversionException(
                "Could not convert ItemPublicTokenExchangeResponse into ACH bank info data. Reason: " + e.getMessage());
        }
    }

    @NotNull
    private void populateAchBankInfoDetailsData(final String accessToken,
                                                final AuthGetResponse authGetResponse,
                                                final String selectedBankId,
                                                final AchBankInfoDetailsData target) throws IOException {
        final NumbersACH numbersACH;
        final IdentityGetResponse accountHolderInfo;
        final AccountBase accountBase;
        accountBase = getBankAccountBase(authGetResponse, selectedBankId);
        numbersACH = getNumbersAch(authGetResponse, selectedBankId);
        accountHolderInfo = getAccountHolderInfo(accessToken, selectedBankId);
        final String accountId = accountBase.getAccountId();

        final String accountHolderName = accountHolderInfo.getAccounts().get(0).getOwners().get(0).getNames()
                                                          .get(0);

        final Owner owner = accountHolderInfo.getAccounts().get(0).getOwners().get(0);
        final String accountHolderEmail = owner.getEmails().stream().filter(Email::getPrimary).findFirst()
                                               .map(Email::getData)
                                               .orElseThrow(() -> new IOException(
                                                   "Could not find account holder email for account " + accountId));

        target.setAccountType(Objects.requireNonNull(accountBase.getSubtype()).getValue());
        target.setAccountNumber(numbersACH.getAccount());
        target.setAccountHolderName(accountHolderName);
        target.setAccountHolderEmail(accountHolderEmail);
        target.setBankRouting(numbersACH.getRouting());
        target.setMask(accountBase.getMask());
        target.setInstitutionName(accountBase.getOfficialName());
    }

    private IdentityGetResponse getAccountHolderInfo(final String token, final String bankAccountId) throws
        IOException {
        return checkoutComPlaidLinkService.getAccountHolderInfo(token,
                                                                bankAccountId);
    }

    private NumbersACH getNumbersAch(final AuthGetResponse authGetResponse, final String accountId) {
        return authGetResponse.getNumbers().getAch().stream()
                              .filter(account -> account.getAccountId().equals(accountId)).findAny()
                              .orElse(null);
    }

    private AccountBase getBankAccountBase(final AuthGetResponse authGetResponse,
                                           final String bankAccountId) {
        return authGetResponse.getAccounts().stream()
                              .filter(account -> account.getAccountId().equals(bankAccountId))
                              .findAny().orElse(null);
    }

    private boolean bankAccountExists(final AuthGetResponse authGetResponse,
                                      final String bankAccountId) {
        return authGetResponse.getAccounts().stream()
                              .anyMatch(account -> account.getAccountId().equals(bankAccountId));
    }

    private String getBankAccountIdFromCreationResponse(final PlaidLinkCreationResponse plaidLinkCreationResponse) {
        return plaidLinkCreationResponse.getMetadata().getAccount_id();
    }
}
