package com.checkout.hybris.facades.payment.wallet;

import com.checkout.hybris.facades.beans.PlaceWalletOrderDataResponse;
import com.checkout.hybris.facades.beans.WalletPaymentAdditionalAuthInfo;
import com.checkout.hybris.facades.enums.WalletPaymentType;

/**
 * Facade to place order using wallet payments
 */
public interface CheckoutComWalletOrderFacade {

    /**
     * Places the order for the given wallet payment type
     *
     * @param walletPaymentAdditionalAuthInfo the create token request object
     * @param walletPaymentType               the wallet specific type
     * @return the populated place order response
     */
    PlaceWalletOrderDataResponse placeWalletOrder(WalletPaymentAdditionalAuthInfo walletPaymentAdditionalAuthInfo,
                                                  WalletPaymentType walletPaymentType);

}
