package com.checkout.hybris.facades.payment.token.request.converters.mappers.impl;

import com.checkout.hybris.facades.beans.WalletPaymentAdditionalAuthInfo;
import com.checkout.hybris.facades.enums.WalletPaymentType;
import com.checkout.hybris.facades.payment.token.request.converters.mappers.CheckoutComMappedPaymentTokenRequestConverter;
import com.checkout.tokens.WalletTokenRequest;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * Default implementation of {@link CheckoutComMappedPaymentTokenRequestConverter}
 */
public class DefaultCheckoutComMappedPaymentTokenRequestConverter implements CheckoutComMappedPaymentTokenRequestConverter {

    protected final Map<WalletPaymentType, Converter<WalletPaymentAdditionalAuthInfo, WalletTokenRequest>> convertersMap;

    public DefaultCheckoutComMappedPaymentTokenRequestConverter(final Map<WalletPaymentType, Converter<WalletPaymentAdditionalAuthInfo, WalletTokenRequest>> convertersMap) {
        this.convertersMap = convertersMap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WalletTokenRequest convertWalletTokenRequest(final WalletPaymentAdditionalAuthInfo walletPaymentAdditionalAuthInfo,
                                                        final WalletPaymentType walletPaymentType) {
        Assert.notNull(walletPaymentAdditionalAuthInfo, "WalletPaymentAdditionalAuthInfo cannot be null.");
        Assert.notNull(walletPaymentType, "WalletPaymentType cannot be null.");

        return convertersMap.get(walletPaymentType).convert(walletPaymentAdditionalAuthInfo);
    }
}