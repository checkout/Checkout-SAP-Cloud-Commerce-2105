package com.checkout.hybris.facades.payment.token.request.converters.mappers;

import com.checkout.hybris.facades.beans.WalletPaymentAdditionalAuthInfo;
import com.checkout.hybris.facades.enums.WalletPaymentType;
import com.checkout.tokens.WalletTokenRequest;

/**
 * Converts a wallet the payload from wallet widget into a specific WalletTokenRequest
 */
public interface CheckoutComMappedPaymentTokenRequestConverter {

    /**
     * Converts the given payload from wallet widget into a specific WalletTokenRequest needed to trigger the
     * generate token request
     *
     * @param walletPaymentAdditionalAuthInfo the payload from wallet widget
     * @param walletPaymentType               the wallet payment type
     * @return a specific WalletTokenRequest for the type
     */
    WalletTokenRequest convertWalletTokenRequest(WalletPaymentAdditionalAuthInfo walletPaymentAdditionalAuthInfo, WalletPaymentType walletPaymentType);
}