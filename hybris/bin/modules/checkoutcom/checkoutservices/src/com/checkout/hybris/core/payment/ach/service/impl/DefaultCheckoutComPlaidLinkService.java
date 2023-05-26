package com.checkout.hybris.core.payment.ach.service.impl;

import com.checkout.hybris.core.enums.AchEnv;
import com.checkout.hybris.core.merchant.services.CheckoutComMerchantConfigurationService;
import com.checkout.hybris.core.model.CheckoutComACHConfigurationModel;
import com.checkout.hybris.core.payment.ach.service.CheckoutComPlaidLinkService;
import com.plaid.client.ApiClient;
import com.plaid.client.model.*;
import com.plaid.client.request.PlaidApi;
import de.hybris.platform.core.model.user.CustomerModel;

import java.io.IOException;
import java.util.List;
import java.util.Map;


/**
 * Default implementation of {@link DefaultCheckoutComPlaidLinkService}
 */
public class DefaultCheckoutComPlaidLinkService implements CheckoutComPlaidLinkService {

    protected static final String CLIENT_ID = "clientId";
    protected static final String SECRET = "secret";

     protected final CheckoutComMerchantConfigurationService checkoutComMerchantConfigurationService;

    public DefaultCheckoutComPlaidLinkService(final CheckoutComMerchantConfigurationService checkoutComMerchantConfigurationService) {
        this.checkoutComMerchantConfigurationService = checkoutComMerchantConfigurationService;
    }

    protected PlaidApi getPlaidLinkClient() {
        final CheckoutComACHConfigurationModel achConfiguration = checkoutComMerchantConfigurationService.getACHConfiguration();
        final ApiClient apiClient = new ApiClient(Map.of(CLIENT_ID, achConfiguration.getClientId(),
                SECRET, achConfiguration.getSecret()));
        if (AchEnv.DEVELOPMENT.equals(achConfiguration.getAchEnv())) {
            apiClient.setPlaidAdapter(ApiClient.Development);
        } else if (AchEnv.PRODUCTION.equals(achConfiguration.getAchEnv())) {
            apiClient.setPlaidAdapter(ApiClient.Production);
        } else {
            apiClient.setPlaidAdapter(ApiClient.Sandbox);
        }
        return apiClient.createService(PlaidApi.class);
    }

    @Override
    public LinkTokenCreateResponse linkTokenCreate(final CustomerModel customer) throws IOException {
        final LinkTokenCreateRequest linkTokenCreateRequest = getLinkTokenCreateRequest(customer);

        return getPlaidLinkClient().linkTokenCreate(linkTokenCreateRequest).execute().body();
    }

    protected LinkTokenCreateRequest getLinkTokenCreateRequest(final CustomerModel customer) {
        final CheckoutComACHConfigurationModel achConfiguration =
                checkoutComMerchantConfigurationService.getACHConfiguration();
        final LinkTokenCreateRequestUser user = new LinkTokenCreateRequestUser()
                .clientUserId(customer.getCustomerID());
        final DepositoryFilter depositoryFilter = new DepositoryFilter()
                .accountSubtypes(List.of(AccountSubtype.SAVINGS, AccountSubtype.CHECKING));
        final LinkTokenAccountFilters accountFilters = new LinkTokenAccountFilters()
                .depository(depositoryFilter);

        return new LinkTokenCreateRequest()
                .user(user)
                .clientName(achConfiguration.getClientName())
                .products(List.of(Products.AUTH))
                .accountFilters(accountFilters)
                .countryCodes(List.of(CountryCode.US))
                .language(achConfiguration.getLanguage().getIsocode());
    }

    @Override
    public ItemPublicTokenExchangeResponse itemPublicTokenExchange(final String publicToken) throws IOException {
        final ItemPublicTokenExchangeRequest itemPublicTokenExchangeRequest = getItemPublicTokenExchangeRequest(
                publicToken);

        return getPlaidLinkClient().itemPublicTokenExchange(itemPublicTokenExchangeRequest).execute().body();
    }

    protected ItemPublicTokenExchangeRequest getItemPublicTokenExchangeRequest(final String publicToken) {
        return new ItemPublicTokenExchangeRequest()
                .publicToken(publicToken);
    }

    @Override
    public AuthGetResponse getACHInfo(final String accessToken) throws IOException {
        final AuthGetRequest authGetRequest = getAuthGetRequest(accessToken);

        return getPlaidLinkClient().authGet(authGetRequest).execute().body();
    }

    protected AuthGetRequest getAuthGetRequest(final String accessToken) {
        return new AuthGetRequest()
                .accessToken(accessToken);
    }

    @Override
    public AccountsGetResponse getAccountInfo(final String accessToken, final String accountId) throws IOException {
        final AccountsGetRequest request = getAccountsGetRequest(accessToken, accountId);

        return getPlaidLinkClient().accountsGet(request).execute().body();
    }

    protected AccountsGetRequest getAccountsGetRequest(final String accessToken, final String accountId) {
        final AccountsGetRequestOptions accountsGetRequestOptions = new AccountsGetRequestOptions()
                .accountIds(List.of(accountId));
        return new AccountsGetRequest()
                .accessToken(accessToken)
                .options(accountsGetRequestOptions);
    }

    @Override
    public IdentityGetResponse getAccountHolderInfo(final String accessToken, final String accountId) throws IOException {
        final IdentityGetRequest identityGetRequest = getIdentityGetRequest(accessToken, accountId);

        return getPlaidLinkClient().identityGet(identityGetRequest).execute().body();
    }

    protected IdentityGetRequest getIdentityGetRequest(final String accessToken, final String accountId) {
        final IdentityGetRequestOptions identityGetRequestOptions = new IdentityGetRequestOptions()
                .accountIds(List.of(accountId));

        return new IdentityGetRequest()
                .accessToken(accessToken)
                .options(identityGetRequestOptions);
    }
}
