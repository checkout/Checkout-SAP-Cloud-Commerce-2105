package com.checkout.hybris.core.payment.ach.service;

import com.plaid.client.model.*;
import de.hybris.platform.core.model.user.CustomerModel;

import java.io.IOException;

/**
 * Service for ACH functionalities.
 */
public interface CheckoutComPlaidLinkService {


    LinkTokenCreateResponse linkTokenCreate(CustomerModel customer) throws IOException;

    ItemPublicTokenExchangeResponse itemPublicTokenExchange(String publicToken) throws IOException;

    AuthGetResponse getACHInfo(String accessToken) throws IOException;

    AccountsGetResponse getAccountInfo(final String accessToken, final String accountId) throws IOException;

    IdentityGetResponse getAccountHolderInfo(final String accessToken, final String accountId) throws IOException;
}
