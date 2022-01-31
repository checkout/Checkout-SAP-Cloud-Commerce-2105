package com.checkout.hybris.core.certificate.services.impl;

import de.hybris.bootstrap.annotations.UnitTest;
import org.junit.Test;

import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;

import static org.junit.Assert.assertEquals;

@UnitTest
public class DefaultCheckoutComCertificateServiceTest {

    private static final String CERIFICATE_WITH_HEADERS_NEWLINES_AND_SPACES = "-----BEGIN CERTIFICATE-----\n" +
            "MIIGNjCCBR6gAwIBAgIIDmu1SH+MxG8wDQYJKoZIhvc NAQELBQAwgZYxCzAJBgNV\n" +
            "AQEMGW1lcmNoYW50LmNvbS5jaGVja291dC5l MnkxPjA8BgNVBAMMNUFwcGxlIFBh\n" +
            "0CF1eSeDrGxa6Q==\n" +
            "-----END CERTIFICATE-----";

    private static final String CERIFICATE_WITH_HEADERS_AND_NEWLINES = "-----BEGIN CERTIFICATE-----\n" +
            "MIIGNjCCBR6gAwIBAgIIDmu1SH+MxG8wDQYJKoZIhvcNAQELBQAwgZYxCzAJBgNV\n" +
            "AQEMGW1lcmNoYW50LmNvbS5jaGVja291dC5lMnkxPjA8BgNVBAMMNUFwcGxlIFBh\n" +
            "0CF1eSeDrGxa6Q==\n" +
            "-----END CERTIFICATE-----";
    private static final String CERIFICATE_WITH_HEADERS_ONLY = "-----BEGIN CERTIFICATE-----MIIGNjCCBR6gAwIBAgIIDmu1SH+MxG8wDQYJKoZIhvcNAQELBQAwgZYxCzAJBgNVAQEMGW1lcmNoYW50LmNvbS5jaGVja291dC5lMnkxPjA8BgNVBAMMNUFwcGxlIFBh0CF1eSeDrGxa6Q==-----END CERTIFICATE-----";

    private static final String CLEANED_CERIFICATE = "MIIGNjCCBR6gAwIBAgIIDmu1SH+MxG8wDQYJKoZIhvcNAQELBQAwgZYxCzAJBgNVAQEMGW1lcmNoYW50LmNvbS5jaGVja291dC5lMnkxPjA8BgNVBAMMNUFwcGxlIFBh0CF1eSeDrGxa6Q==";

    private static final String PRIVATE_KEY_WITH_HEADERS_NEWLINES_AND_SPACES = "-----BEGIN PRIVATE KEY-----\n" +
            "MIIGNjCCBR6gAwIBAgIIDmu1SH+MxG8wDQYJKoZIhvc NAQELBQAwgZYxCzAJBgNV\n" +
            "AQEMGW1lcmNoYW50LmNvbS5jaGVja291dC5l MnkxPjA8BgNVBAMMNUFwcGxlIFBh\n" +
            "0CF1eSeDrGxa6Q==\n" +
            "-----END PRIVATE KEY-----";

    private static final String PRIVATE_KEY_WITH_HEADERS_AND_NEWLINES = "-----BEGIN PRIVATE KEY-----\n" +
            "MIIGNjCCBR6gAwIBAgIIDmu1SH+MxG8wDQYJKoZIhvcNAQELBQAwgZYxCzAJBgNV\n" +
            "AQEMGW1lcmNoYW50LmNvbS5jaGVja291dC5lMnkxPjA8BgNVBAMMNUFwcGxlIFBh\n" +
            "0CF1eSeDrGxa6Q==\n" +
            "-----END PRIVATE KEY-----";

    private static final String PRIVATE_KEY_WITH_HEADERS_ONLY = "-----BEGIN PRIVATE KEY-----MIIGNjCCBR6gAwIBAgIIDmu1SH+MxG8wDQYJKoZIhvcNAQELBQAwgZYxCzAJBgNVAQEMGW1lcmNoYW50LmNvbS5jaGVja291dC5lMnkxPjA8BgNVBAMMNUFwcGxlIFBh0CF1eSeDrGxa6Q==-----END PRIVATE KEY-----";

    private static final String CLEANED_PRIVATE_KEY = "MIIGNjCCBR6gAwIBAgIIDmu1SH+MxG8wDQYJKoZIhvcNAQELBQAwgZYxCzAJBgNVAQEMGW1lcmNoYW50LmNvbS5jaGVja291dC5lMnkxPjA8BgNVBAMMNUFwcGxlIFBh0CF1eSeDrGxa6Q==";

    private static final String MOCK_PUBLIC_KEY = "MIICiTCCAfICCQCAsBwPjMyJIzANBgkqhkiG9w0BAQsFADCBiDELMAkGA1UEBhMC" +
            "R0IxDjAMBgNVBAgMBXN0YXRlMQ0wCwYDVQQHDARjaXR5MRAwDgYDVQQKDAdjb21w" +
            "YW55MRAwDgYDVQQLDAdzZWN0aW9uMRkwFwYDVQQDDBB3d3cuZHVtbXkub3JnLm1l" +
            "MRswGQYJKoZIhvcNAQkBFgxkdW1teUBvcmcubWUwHhcNMjAwMzEyMTAxNDE2WhcN" +
            "MjEwMzEyMTAxNDE2WjCBiDELMAkGA1UEBhMCR0IxDjAMBgNVBAgMBXN0YXRlMQ0w" +
            "CwYDVQQHDARjaXR5MRAwDgYDVQQKDAdjb21wYW55MRAwDgYDVQQLDAdzZWN0aW9u" +
            "MRkwFwYDVQQDDBB3d3cuZHVtbXkub3JnLm1lMRswGQYJKoZIhvcNAQkBFgxkdW1t" +
            "eUBvcmcubWUwgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBANMCcUcSy9BYN2wS" +
            "8DH3hZhEymCiR9PT8LHumbBT6/6YcS3Lfl5U6r8lL5ep7GPG4TtfBj0D1HQA8sWy" +
            "qJ6f+toC5aajouhRFHNrDdOpsB5OfXfev4mrrY/cEFpYpPLa9IgEwOp0ymGRSJA2" +
            "/OzLhAz0fZULrx7C61it6VO6aWMFAgMBAAEwDQYJKoZIhvcNAQELBQADgYEAYDul" +
            "cTON30xnQuJjZ58kgm5q81zj7SiObJrJ3beKEPbIWYy8f0fljOM3QnRCBYtYr7lS" +
            "LOiiZ9bT5BNUDl5Ch9+DhBDhoG3TOF9L20hj3CVeCdfd/sBZFc4wbpjqKJRxA197" +
            "wlHD5BnVZbyFYKpLiLiDceR4v0kIw+KDjg2SdLc=";

    private static final String MOCK_PRIVATE_KEY = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBANMCcUcSy9BYN2wS" +
            "8DH3hZhEymCiR9PT8LHumbBT6/6YcS3Lfl5U6r8lL5ep7GPG4TtfBj0D1HQA8sWy" +
            "qJ6f+toC5aajouhRFHNrDdOpsB5OfXfev4mrrY/cEFpYpPLa9IgEwOp0ymGRSJA2" +
            "/OzLhAz0fZULrx7C61it6VO6aWMFAgMBAAECgYAO76pTDJqOud/ab5C/CLVVLPFi" +
            "W7pX3TW5cplAaQBYejPwuOnOBZbERv6Sbr3D5k0FJdnMMdw1BegpZFcZO1vHN+pF" +
            "C/OA7wIQi6aUWMmSLa392iQVwBGHn3xQ+jK3hoMiqGDUqt44W6V7KyiIa7Gpl/01" +
            "vtBNJhQCD0leoD9hlQJBAO0QFQft9wguXvooLBjhBfNeuHGpqcj91oBjKBI8yMpf" +
            "nbshrhN21bkZh7KWcy5gvj+emeYzPVam5Ua7YsU92fMCQQDj3ZKW4Bpea7SoITLU" +
            "opAz0cqfB90eKN7h8X0v2nddhv/WGCRkQd/z5SbVUSRXb4BIANLcTpOJc4PxK/Wd" +
            "QtUnAkEAk+tuBAWjF9K/Zr48TcXTDblzw1CHZael+XRlo4OUElq5M/PfB7wlvZrF" +
            "hlTESBMLguz2wKIGSxRWid0Q01w1qwJAamETAqT1KoeV+7gkBukt8UNMGRqOvHnQ" +
            "NCONkj2n5F4WKllzA+tNJowgqQ4MrVU8ymC4EGoOruji/EDNlfGRmQJBANBfvH9i" +
            "xaQau1ZVtuu8f5aq88dkFPFK5VOzkIeWSV9V0bTsu5D/SaU2iWAplGgQGXdN7ASB" +
            "Mbs4ZELmWe4E33k=";

    private static final String PRIVATE_KEY_EXPONENT = "10488551198223866875613894482211954141938553102241763638462283187669807229113013893987914374088732151301454738345957722886789000034786579846810232768565886251220378543953878559332142873357436193180207175257473574365357632426158320498058193479617830076186063660432622717053156476594867989744356733530459300245";
    private static final String PUBLIC_KEY_DN = "EMAILADDRESS=dummy@org.me, CN=www.dummy.org.me, OU=section, O=company, L=city, ST=state, C=GB";
    private static final String KEY_STORE_ALIAS = "alias";
    private static final String KEY_STORE_PASSWORD = "password";

    private DefaultCheckoutComCertificateService testObj = new DefaultCheckoutComCertificateService();

    @Test
    public void cleanupCertificate_WhenCertificateContainsHeadersSpacesAndNewlines_ShouldStripAllOut() {
        assertEquals(CLEANED_CERIFICATE, testObj.cleanupCertificate(CERIFICATE_WITH_HEADERS_NEWLINES_AND_SPACES));
    }

    @Test
    public void cleanupCertificate_WhenCertificateContainsHeadersAndSpaces_ShouldStripAllOut() {
        assertEquals(CLEANED_CERIFICATE, testObj.cleanupCertificate(CERIFICATE_WITH_HEADERS_AND_NEWLINES));
    }

    @Test
    public void cleanupCertificate_WhenCertificateContainsHeadersOnly_ShouldStripAllOut() {
        assertEquals(CLEANED_CERIFICATE, testObj.cleanupCertificate(CERIFICATE_WITH_HEADERS_ONLY));
    }

    @Test
    public void cleanupCertificate_WhenCertificateContainsNothingOtherThanCertificate_ShouldReturnSame() {
        assertEquals(CLEANED_CERIFICATE, testObj.cleanupCertificate(CLEANED_CERIFICATE));
    }

    @Test
    public void cleanupPrivateKey_WhenPrivateKeyContainsHeadersSpacesAndNewlines_ShouldStripAllOut() {
        assertEquals(CLEANED_PRIVATE_KEY, testObj.cleanupPrivateKey(PRIVATE_KEY_WITH_HEADERS_NEWLINES_AND_SPACES));
    }

    @Test
    public void cleanupPrivateKey_WhenPrivateKeyContainsHeadersAndSpaces_ShouldStripAllOut() {
        assertEquals(CLEANED_PRIVATE_KEY, testObj.cleanupPrivateKey(PRIVATE_KEY_WITH_HEADERS_AND_NEWLINES));
    }

    @Test
    public void cleanupPrivateKey_WhenPrivateKeyContainsHeadersOnly_ShouldStripAllOut() {
        assertEquals(CLEANED_PRIVATE_KEY, testObj.cleanupPrivateKey(PRIVATE_KEY_WITH_HEADERS_ONLY));
    }

    @Test
    public void cleanupPrivateKey_WhenPrivateKeyContainsNothingOtherThanPrivateKey_ShouldReturnSame() {
        assertEquals(CLEANED_PRIVATE_KEY, testObj.cleanupPrivateKey(CLEANED_PRIVATE_KEY));
    }

    @Test
    public void generateX509Certificate_WhenValidPublicKeyGiven_ShouldGenerateCertificate() throws Exception {
        final X509Certificate result = testObj.generateX509Certificate(MOCK_PUBLIC_KEY);

        assertEquals(PUBLIC_KEY_DN, result.getSubjectDN().getName());
    }

    @Test
    public void generatePrivateKey_WhenValidPrivateKeyGiven_ShouldGeneratePrivateKey() throws Exception {
        final RSAPrivateKey result = testObj.generatePrivateKey(MOCK_PRIVATE_KEY);

        assertEquals(PRIVATE_KEY_EXPONENT, result.getPrivateExponent().toString());
    }

    @Test
    public void generateKeyStore_WhenPrivateAndPublicKeyGiven_ShouldGenerateKeyStore() throws Exception {
        final RSAPrivateKey privateKey = testObj.generatePrivateKey(MOCK_PRIVATE_KEY);
        final X509Certificate publicKey = testObj.generateX509Certificate(MOCK_PUBLIC_KEY);

        final KeyStore result = testObj.generateKeyStore(KEY_STORE_ALIAS, KEY_STORE_PASSWORD, privateKey, publicKey);

        assertEquals(publicKey, result.getCertificate(KEY_STORE_ALIAS));
        assertEquals(privateKey, result.getKey(KEY_STORE_ALIAS, KEY_STORE_PASSWORD.toCharArray()));
    }
}