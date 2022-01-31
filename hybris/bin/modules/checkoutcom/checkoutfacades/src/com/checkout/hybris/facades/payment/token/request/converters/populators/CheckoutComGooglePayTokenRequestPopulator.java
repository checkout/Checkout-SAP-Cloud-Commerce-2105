package com.checkout.hybris.facades.payment.token.request.converters.populators;

import com.checkout.hybris.facades.beans.GooglePayPaymentToken;
import com.checkout.tokens.WalletTokenRequest;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

import java.util.HashMap;
import java.util.Map;

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.GOOGLEPAY;
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

/**
 * Populates the WalletTokenRequest from GooglePayPaymentToken
 */
public class CheckoutComGooglePayTokenRequestPopulator implements Populator<GooglePayPaymentToken, WalletTokenRequest> {

    protected static final String PROTOCOL_VERSION_REQUEST_KEY = "protocolVersion";
    protected static final String SIGNATURE_REQUEST_KEY = "signature";
    protected static final String SIGNED_MESSAGE_REQUEST_KEY = "signedMessage";

    /**
     * {@inheritDoc}
     */
    @Override
    public void populate(final GooglePayPaymentToken source, final WalletTokenRequest target) throws ConversionException {
        validateParameterNotNull(source, "GooglePayPaymentToken cannot be null.");
        validateParameterNotNull(target, "WalletTokenRequest cannot be null.");

        target.setType(GOOGLEPAY.name().toLowerCase());
        target.setTokenData(createTokenData(source));
    }

    /**
     * Creates the TokenData map for google pay
     *
     * @param source the populated GooglePayPaymentToken
     * @return the TokenData map
     */
    protected Map<String, Object> createTokenData(final GooglePayPaymentToken source) {
        final Map<String, Object> tokenData = new HashMap<>();
        tokenData.put(SIGNED_MESSAGE_REQUEST_KEY, source.getSignedMessage());
        tokenData.put(SIGNATURE_REQUEST_KEY, source.getSignature());
        tokenData.put(PROTOCOL_VERSION_REQUEST_KEY, source.getProtocolVersion());
        return tokenData;
    }
}