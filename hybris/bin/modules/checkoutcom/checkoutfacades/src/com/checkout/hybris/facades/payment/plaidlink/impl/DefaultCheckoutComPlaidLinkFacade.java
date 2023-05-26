package com.checkout.hybris.facades.payment.plaidlink.impl;

import com.checkout.hybris.core.payment.ach.service.CheckoutComPlaidLinkService;
import com.checkout.hybris.facades.beans.AchBankInfoDetailsData;
import com.checkout.hybris.facades.beans.PlaidLinkCreationResponse;
import com.checkout.hybris.facades.payment.plaidlink.CheckoutComPlaidLinkFacade;
import com.plaid.client.model.ItemPublicTokenExchangeResponse;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.order.CartService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;

/**
 * Default implementation of {@link CheckoutComPlaidLinkFacade}
 */
public class DefaultCheckoutComPlaidLinkFacade implements CheckoutComPlaidLinkFacade {

    protected static final String ACH = "ACH";

    protected final CartService cartService;
    protected final CheckoutComPlaidLinkService checkoutComPlaidLinkService;
    protected final Converter<Pair<ItemPublicTokenExchangeResponse, PlaidLinkCreationResponse>,
        AchBankInfoDetailsData> publicTokenExchangeResponseToAchBankInfoDetailConverter;

    public DefaultCheckoutComPlaidLinkFacade(final CartService cartService,
                                             final CheckoutComPlaidLinkService checkoutComPlaidLinkService,
                                             final Converter<Pair<ItemPublicTokenExchangeResponse,
                                                 PlaidLinkCreationResponse>, AchBankInfoDetailsData> publicTokenExchangeResponseToAchBankInfoDetailConverter) {
        this.cartService = cartService;
        this.checkoutComPlaidLinkService = checkoutComPlaidLinkService;
        this.publicTokenExchangeResponseToAchBankInfoDetailConverter =
            publicTokenExchangeResponseToAchBankInfoDetailConverter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String linkTokenCreate() throws IOException {
        final CartModel sessionCart = cartService.getSessionCart();
        final CustomerModel currentCustomer = (CustomerModel) sessionCart.getUser();

        return checkoutComPlaidLinkService.linkTokenCreate(currentCustomer).getLinkToken();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AchBankInfoDetailsData getBankAccountDetailsData(final PlaidLinkCreationResponse plaidLinkCreationResponse) throws IOException {
        final ItemPublicTokenExchangeResponse itemPublicTokenExchangeResponse =
            checkoutComPlaidLinkService.itemPublicTokenExchange(plaidLinkCreationResponse.getPublicToken());
        return publicTokenExchangeResponseToAchBankInfoDetailConverter.convert(
            Pair.of(itemPublicTokenExchangeResponse, plaidLinkCreationResponse));
    }
}
