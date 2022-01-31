package com.checkout.hybris.facades.payment.token.request.converters.populators;

import com.checkout.hybris.facades.beans.GooglePayPaymentToken;
import com.checkout.tokens.WalletTokenRequest;
import de.hybris.bootstrap.annotations.UnitTest;
import org.junit.Before;
import org.junit.Test;

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.GOOGLEPAY;
import static com.checkout.hybris.facades.payment.token.request.converters.populators.CheckoutComApplePayTokenRequestPopulator.SIGNATURE_REQUEST_KEY;
import static com.checkout.hybris.facades.payment.token.request.converters.populators.CheckoutComGooglePayTokenRequestPopulator.PROTOCOL_VERSION_REQUEST_KEY;
import static com.checkout.hybris.facades.payment.token.request.converters.populators.CheckoutComGooglePayTokenRequestPopulator.SIGNED_MESSAGE_REQUEST_KEY;
import static org.junit.Assert.assertEquals;

@UnitTest
public class CheckoutComGooglePayTokenRequestPopulatorTest {

    private static final String SIGNATURE = "signature";
    public static final String PROTOCOL_VERSION = "protocol_version";
    public static final String SIGNATURE_MESSAGE = "signature_message";

    private CheckoutComGooglePayTokenRequestPopulator testObj = new CheckoutComGooglePayTokenRequestPopulator();

    private GooglePayPaymentToken source = new GooglePayPaymentToken();
    private WalletTokenRequest target = new WalletTokenRequest();

    @Before
    public void setUp() {
        source.setProtocolVersion(PROTOCOL_VERSION);
        source.setSignedMessage(SIGNATURE_MESSAGE);
        source.setSignature(SIGNATURE);
    }

    @Test
    public void populate_WhenEverythingIsFine_ShouldPopulateTheRequest() {
        testObj.populate(source, target);

        assertEquals(PROTOCOL_VERSION, target.getTokenData().get(PROTOCOL_VERSION_REQUEST_KEY));
        assertEquals(SIGNATURE_MESSAGE, target.getTokenData().get(SIGNED_MESSAGE_REQUEST_KEY));
        assertEquals(SIGNATURE, target.getTokenData().get(SIGNATURE_REQUEST_KEY));
        assertEquals(GOOGLEPAY.name().toLowerCase(), target.getType());
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