package com.checkout.hybris.facades.payment.token.request.converters.populators;

import com.checkout.hybris.facades.beans.ApplePayAdditionalAuthInfo;
import com.checkout.hybris.facades.beans.ApplePayHeader;
import com.checkout.tokens.WalletTokenRequest;
import de.hybris.bootstrap.annotations.UnitTest;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.APPLEPAY;
import static com.checkout.hybris.facades.payment.token.request.converters.populators.CheckoutComApplePayTokenRequestPopulator.*;
import static org.junit.Assert.assertEquals;

@UnitTest
public class CheckoutComApplePayTokenRequestPopulatorTest {

    private static final String TRANSACTION_ID = "transactionId";
    private static final String PUBLIC_KEY_HASH = "PublicKeyHash";
    private static final String EPHEMERAL_PUBLICKEY = "EphemeralPublicKey";
    private static final String SIGNATURE = "signature";
    private static final String DATA = "data";
    private static final String VERSION = "version";

    private CheckoutComApplePayTokenRequestPopulator testObj = new CheckoutComApplePayTokenRequestPopulator();

    private ApplePayAdditionalAuthInfo source = new ApplePayAdditionalAuthInfo();
    private WalletTokenRequest target = new WalletTokenRequest();

    @Before
    public void setUp() {
        source.setVersion(VERSION);
        source.setData(DATA);
        source.setSignature(SIGNATURE);
        final ApplePayHeader applePayHeader = new ApplePayHeader();
        applePayHeader.setEphemeralPublicKey(EPHEMERAL_PUBLICKEY);
        applePayHeader.setPublicKeyHash(PUBLIC_KEY_HASH);
        applePayHeader.setTransactionId(TRANSACTION_ID);
        source.setHeader(applePayHeader);
    }

    @Test
    public void populate_WhenEverythingIsFine_ShouldPopulateTheRequest() {
        testObj.populate(source, target);

        assertEquals(VERSION, target.getTokenData().get(VERSION_REQUEST_KEY));
        assertEquals(DATA, target.getTokenData().get(DATA_REQUEST_KEY));
        assertEquals(SIGNATURE, target.getTokenData().get(SIGNATURE_REQUEST_KEY));
        assertEquals(EPHEMERAL_PUBLICKEY, ((Map) target.getTokenData().get(HEADER_KEY)).get(EPHEMERAL_PUBLIC_KEY_REQUEST_KEY));
        assertEquals(PUBLIC_KEY_HASH, ((Map) target.getTokenData().get(HEADER_KEY)).get(PUBLIC_KEY_HASH_REQUEST_KEY));
        assertEquals(TRANSACTION_ID, ((Map) target.getTokenData().get(HEADER_KEY)).get(TRANSACTION_ID_REQUEST_KEY));
        assertEquals(APPLEPAY.name().toLowerCase(), target.getType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void populate_WhenSourceNull_ShouldThrowException() {
        testObj.populate(null, target);
    }

    @Test(expected = IllegalArgumentException.class)
    public void populate_WhenTargetNull_ShouldThrowException() {
        testObj.populate(source, null);
    }
}