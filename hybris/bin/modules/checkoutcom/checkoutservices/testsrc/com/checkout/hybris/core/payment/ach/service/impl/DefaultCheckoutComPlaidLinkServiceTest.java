package com.checkout.hybris.core.payment.ach.service.impl;

import com.checkout.hybris.core.enums.AchEnv;
import com.checkout.hybris.core.merchant.services.CheckoutComMerchantConfigurationService;
import com.checkout.hybris.core.model.CheckoutComACHConfigurationModel;
import com.plaid.client.model.*;
import com.plaid.client.request.PlaidApi;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.user.CustomerModel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Objects;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultCheckoutComPlaidLinkServiceTest {

	private static final String CUSTOMER_ID = "customer-id";
	private static final String CLIENT_ID = "client-id";
	private static final String SECRET = "secret";
	private static final String CLIENT_NAME = "client-name";
	public static final String ENG_ISOCODE = "eng";
	private static final String PUBLIC_TOKEN = "public-token";
	private static final String ACCESS_TOKEN = "access-token";
	private static final String ACCOUNT_ID = "account-id";
	@Spy
	@InjectMocks
	private DefaultCheckoutComPlaidLinkService testObj;

	@Mock
	private CheckoutComMerchantConfigurationService checkoutComMerchantConfigurationServiceMock;

	@Test
	public void getPlaidLinkClient_createsAPlaidApiObjectWhenEnvironmentIsDevelop() {
		when_ACHConfigurationExists_returnIt(CLIENT_ID, SECRET, CLIENT_NAME, ENG_ISOCODE, AchEnv.DEVELOPMENT);

		final PlaidApi result = testObj.getPlaidLinkClient();

		assertThat(result).isInstanceOf(PlaidApi.class);
	}

	@Test
	public void getPlaidLinkClient_createsAPlaidApiObjectWhenEnvironmentIsProduction() {
		when_ACHConfigurationExists_returnIt(CLIENT_ID, SECRET, CLIENT_NAME, ENG_ISOCODE, AchEnv.PRODUCTION);

		final PlaidApi result = testObj.getPlaidLinkClient();

		assertThat(result).isInstanceOf(PlaidApi.class);
	}

	@Test
	public void getPlaidLinkClient_createsAPlaidApiObjectWhenEnvironmentIsSandbox() {
		when_ACHConfigurationExists_returnIt(CLIENT_ID, SECRET, CLIENT_NAME, ENG_ISOCODE, null);

		final PlaidApi result = testObj.getPlaidLinkClient();

		assertThat(result).isInstanceOf(PlaidApi.class);
	}

	@Test
	public void getLinkTokenCreateRequest_shouldReturnLinkTokenCreateRequestWithCustomerIdFilled() {
		when_ACHConfigurationExists_returnIt(CLIENT_ID, SECRET, CLIENT_NAME, ENG_ISOCODE, AchEnv.DEVELOPMENT);
		final CustomerModel customer = createCustomerWithId(CUSTOMER_ID);

		final LinkTokenCreateRequest result = testObj.getLinkTokenCreateRequest(customer);

		assertThat(result.getUser().getClientUserId()).isEqualTo(CUSTOMER_ID);
		assertThat(result.getClientName()).isEqualTo(CLIENT_NAME);
		assertThat(result.getProducts()).containsExactlyInAnyOrder(Products.AUTH);
		assertThat(Objects.requireNonNull(
								  Objects.requireNonNull(Objects.requireNonNull(result.getAccountFilters()).getDepository()))
						  .getAccountSubtypes()).containsExactlyInAnyOrder(
				AccountSubtype.SAVINGS, AccountSubtype.CHECKING);
		assertThat(result.getCountryCodes()).containsExactlyInAnyOrder(CountryCode.US);
		assertThat(result.getLanguage()).isEqualTo(ENG_ISOCODE);
	}

	@Test
	public void getItemPublicTokenExchangeRequest_shouldReturnItemPublicTokenExchangeRequestWithProvidedPublicToken() {
		final ItemPublicTokenExchangeRequest result = testObj.getItemPublicTokenExchangeRequest(
				PUBLIC_TOKEN);

		assertThat(result.getPublicToken()).isEqualTo(PUBLIC_TOKEN);
	}

	@Test
	public void getAccountsGetRequest_shouldReturnAccountsGetRequestWithAccessTokenAndAccountIdProvided() {
		final AccountsGetRequest accountsGetRequest = testObj.getAccountsGetRequest(ACCESS_TOKEN, ACCOUNT_ID);

		assertThat(accountsGetRequest.getAccessToken()).isEqualTo(ACCESS_TOKEN);
		assertThat(Objects.requireNonNull(accountsGetRequest.getOptions()).getAccountIds()).containsExactlyInAnyOrder(
				ACCOUNT_ID);
	}

	@Test
	public void getAuthGetRequest_shouldReturnAuthGetRequestWithProvidedAccessToken() {
		final AuthGetRequest authGetRequest = testObj.getAuthGetRequest(ACCESS_TOKEN);

		assertThat(authGetRequest.getAccessToken()).isEqualTo(ACCESS_TOKEN);
	}

	@Test
	public void getIdentityGetRequest_shouldReturnIdentityGetRequest_WithAccessTokenAndAccountIdProvided() {
		final IdentityGetRequest result = testObj.getIdentityGetRequest(ACCESS_TOKEN, ACCOUNT_ID);

		assertThat(result.getAccessToken()).isEqualTo(ACCESS_TOKEN);
		assertThat(Objects.requireNonNull(result.getOptions()).getAccountIds()).containsExactlyInAnyOrder(ACCOUNT_ID);
	}

	private static CustomerModel createCustomerWithId(final String customerId) {
		final CustomerModel customer = new CustomerModel();
		customer.setCustomerID(customerId);
		return customer;
	}

	private void when_ACHConfigurationExists_returnIt(final String clientId, final String secret,
													  final String clientName, final String language,
													  final AchEnv environment) {
		final CheckoutComACHConfigurationModel achConfiguration = new CheckoutComACHConfigurationModel();
		achConfiguration.setClientId(clientId);
		achConfiguration.setSecret(secret);
		achConfiguration.setAchEnv(environment);
		achConfiguration.setClientName(clientName);
		final LanguageModel languageModel = new LanguageModel();
		languageModel.setIsocode(language);
		achConfiguration.setLanguage(languageModel);
		when(checkoutComMerchantConfigurationServiceMock.getACHConfiguration()).thenReturn(achConfiguration);
	}
}
