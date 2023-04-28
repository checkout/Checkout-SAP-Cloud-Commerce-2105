package com.checkout.hybris.facades.payment.ach.impl;

import com.checkout.hybris.core.payment.ach.service.CheckoutComPlaidLinkService;
import com.checkout.hybris.facades.beans.AchBankInfoDetailsData;
import com.checkout.hybris.facades.beans.PlaidLinkCreationResponse;
import com.checkout.hybris.facades.payment.plaidlink.impl.DefaultCheckoutComPlaidLinkFacade;
import com.plaid.client.model.ItemPublicTokenExchangeResponse;
import com.plaid.client.model.LinkTokenCreateResponse;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.order.CartService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultCheckoutComPlaidLinkFacadeTest {

    private static final String PUBLIC_TOKEN = "publicToken";

    private static final String LINK_TOKEN = "linkToken";

    @InjectMocks
    private DefaultCheckoutComPlaidLinkFacade testObj;

    @Mock
    private CartService cartServiceMock;
    @Mock
    private CheckoutComPlaidLinkService checkoutComPlaidLinkServiceMock;
    @Mock
    private Converter<Pair<ItemPublicTokenExchangeResponse, PlaidLinkCreationResponse>,
        AchBankInfoDetailsData> publicTokenExchangeResponseToAchBankInfoDetailConverterMock;
    @Mock
    private CartModel cartMock;
    @Mock
    private CustomerModel userMock;
    @Mock
    private AchBankInfoDetailsData achBankInfoDetailsDataMock;
    private final LinkTokenCreateResponse linkTokenCreateResponse = new LinkTokenCreateResponse();
    private final ItemPublicTokenExchangeResponse itemPublicTokenExchangeResponse =
        new ItemPublicTokenExchangeResponse();
    private final PlaidLinkCreationResponse plaidLinkCreationResponse = new PlaidLinkCreationResponse();


    @Before
    public void setUp() throws Exception {
        plaidLinkCreationResponse.setPublicToken(PUBLIC_TOKEN);
        itemPublicTokenExchangeResponse.setAccessToken(PUBLIC_TOKEN);
        linkTokenCreateResponse.setLinkToken(LINK_TOKEN);
        when(checkoutComPlaidLinkServiceMock.itemPublicTokenExchange(PUBLIC_TOKEN)).thenReturn(
            itemPublicTokenExchangeResponse);
    }

    @Test
    public void linkTokenCreate_whenCartExists_shouldReturnTheLinkTokenForCurrentUserInSession() throws IOException {
        when(cartMock.getUser()).thenReturn(userMock);
        when(cartServiceMock.getSessionCart()).thenReturn(cartMock);
        when(checkoutComPlaidLinkServiceMock.linkTokenCreate(userMock)).thenReturn(linkTokenCreateResponse);

        final String result = testObj.linkTokenCreate();

        assertThat(result).isEqualTo(LINK_TOKEN);
    }

    @Test
    public void setBankAccountPaymentInfoOnCart_whenLinkTokenCreateRaisesIOException_shouldRaiseAnIOException() throws IOException {
        when(cartMock.getUser()).thenReturn(userMock);
        when(cartServiceMock.getSessionCart()).thenReturn(cartMock);
        doThrow(IOException.class).when(checkoutComPlaidLinkServiceMock).linkTokenCreate(userMock);

        final Throwable throwable = catchThrowable(() -> testObj.linkTokenCreate());

        assertThat(throwable).isInstanceOf(IOException.class);
    }

    @Test
    public void getBankAccountDetailsData_shouldQueryInfoWithPublicTokenAndConvertAccountDetails() throws IOException {
        when(checkoutComPlaidLinkServiceMock.itemPublicTokenExchange(PUBLIC_TOKEN)).thenReturn(
            itemPublicTokenExchangeResponse);
        when(publicTokenExchangeResponseToAchBankInfoDetailConverterMock.convert(
            argThat(new PairMatcher(Pair.of(itemPublicTokenExchangeResponse, plaidLinkCreationResponse))))).thenReturn(
            achBankInfoDetailsDataMock);
        final AchBankInfoDetailsData result = testObj.getBankAccountDetailsData(plaidLinkCreationResponse);

        assertThat(result).isEqualTo(achBankInfoDetailsDataMock);
    }

    private class PairMatcher extends ArgumentMatcher<Pair<ItemPublicTokenExchangeResponse,
        PlaidLinkCreationResponse>> {
        private final Pair<ItemPublicTokenExchangeResponse,
            PlaidLinkCreationResponse> originalPair;

        public PairMatcher(final Pair<ItemPublicTokenExchangeResponse,
            PlaidLinkCreationResponse> pair) {
            this.originalPair = pair;
        }

        @Override
        public boolean matches(final Object o) {
            final Pair<ItemPublicTokenExchangeResponse, PlaidLinkCreationResponse> pair =
                (Pair<ItemPublicTokenExchangeResponse, PlaidLinkCreationResponse>) o;
            return pair.getLeft().equals(originalPair.getLeft())
                && pair.getRight().equals(originalPair.getRight());
        }
    }
}
