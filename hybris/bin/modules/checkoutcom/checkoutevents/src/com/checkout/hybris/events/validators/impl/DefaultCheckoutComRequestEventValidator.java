package com.checkout.hybris.events.validators.impl;

import com.checkout.hybris.core.merchant.services.CheckoutComMerchantConfigurationService;
import com.checkout.hybris.events.services.CheckoutComPaymentEventService;
import com.checkout.hybris.events.validators.CheckoutComRequestEventValidator;
import com.google.gson.Gson;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * Default implementation of {@link CheckoutComRequestEventValidator}
 */
public class DefaultCheckoutComRequestEventValidator implements CheckoutComRequestEventValidator {

    protected static final Logger LOG = LogManager.getLogger(DefaultCheckoutComRequestEventValidator.class);

    protected static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    protected final CheckoutComMerchantConfigurationService checkoutComMerchantConfigurationService;
    protected final CheckoutComPaymentEventService checkoutComPaymentEventService;

    public DefaultCheckoutComRequestEventValidator(final CheckoutComMerchantConfigurationService checkoutComMerchantConfigurationService,
                                                   final CheckoutComPaymentEventService checkoutComPaymentEventService) {
        this.checkoutComMerchantConfigurationService = checkoutComMerchantConfigurationService;
        this.checkoutComPaymentEventService = checkoutComPaymentEventService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCkoSignatureValid(final String ckoSignature, final String eventBody) throws InvalidKeyException, NoSuchAlgorithmException {
        if (StringUtils.isBlank(ckoSignature) || StringUtils.isBlank(eventBody)) {
            LOG.error("ckoSignature or event body null.");
            return false;
        }
        return ckoSignature.equalsIgnoreCase(createEventBodyHash(eventBody));
    }

    private String createEventBodyHash(final String eventBody) throws NoSuchAlgorithmException, InvalidKeyException {
        final Mac hmac = Mac.getInstance(HmacAlgorithms.HMAC_SHA_256.getName());
        final String siteId = checkoutComPaymentEventService.getSiteIdForTheEvent(new Gson().fromJson(eventBody, Map.class));
        final byte[] secretKeyForSite = checkoutComMerchantConfigurationService.getSecretKeyForSite(siteId).getBytes();
        final SecretKeySpec secretKey = new SecretKeySpec(secretKeyForSite, HmacAlgorithms.HMAC_SHA_256.getName());
        hmac.init(secretKey);
        final byte[] bytes = hmac.doFinal(eventBody.getBytes());
        return new String(convertBytesToHex(bytes));
    }

    private char[] convertBytesToHex(final byte[] bytes) {
        final char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return hexChars;
    }
}
