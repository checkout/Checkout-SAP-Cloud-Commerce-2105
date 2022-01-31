package com.checkout.hybris.facades.payment.token.request.converters.populators;

import com.checkout.hybris.facades.beans.ApplePayAdditionalAuthInfo;
import com.checkout.tokens.WalletTokenRequest;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

import java.util.HashMap;
import java.util.Map;

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.APPLEPAY;
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

/**
 * Populates the WalletTokenRequest from ApplePayAdditionalAuthInfo
 */
public class CheckoutComApplePayTokenRequestPopulator implements Populator<ApplePayAdditionalAuthInfo, WalletTokenRequest> {

    protected static final String TRANSACTION_ID_REQUEST_KEY = "transactionId";
    protected static final String PUBLIC_KEY_HASH_REQUEST_KEY = "publicKeyHash";
    protected static final String EPHEMERAL_PUBLIC_KEY_REQUEST_KEY = "ephemeralPublicKey";
    protected static final String SIGNATURE_REQUEST_KEY = "signature";
    protected static final String DATA_REQUEST_KEY = "data";
    protected static final String VERSION_REQUEST_KEY = "version";
    protected static final String HEADER_KEY = "header";

    /**
     * {@inheritDoc}
     */
    @Override
    public void populate(final ApplePayAdditionalAuthInfo source, final WalletTokenRequest target) throws ConversionException {
        validateParameterNotNull(source, "ApplePayAdditionalAuthInfo cannot be null.");
        validateParameterNotNull(target, "WalletTokenRequest cannot be null.");

        target.setType(APPLEPAY.name().toLowerCase());
        target.setTokenData(createTokenData(source));
    }

    /**
     * Creates the TokenData map for apple pay
     *
     * @param source the populated ApplePayAdditionalAuthInfo
     * @return the TokenData map
     */
    protected Map<String, Object> createTokenData(final ApplePayAdditionalAuthInfo source) {
        final Map<String, Object> tokenData = new HashMap<>();
        tokenData.put(VERSION_REQUEST_KEY, source.getVersion());
        tokenData.put(DATA_REQUEST_KEY, source.getData());
        tokenData.put(SIGNATURE_REQUEST_KEY, source.getSignature());
        final Map<String, Object> header = new HashMap<>();
        if (source.getHeader() != null) {
            header.put(EPHEMERAL_PUBLIC_KEY_REQUEST_KEY, source.getHeader().getEphemeralPublicKey());
            header.put(PUBLIC_KEY_HASH_REQUEST_KEY, source.getHeader().getPublicKeyHash());
            header.put(TRANSACTION_ID_REQUEST_KEY, source.getHeader().getTransactionId());
        }
        tokenData.put(HEADER_KEY, header);
        return tokenData;
    }
}