package com.checkout.hybris.events.validators.impl;

import com.checkout.hybris.core.merchant.services.CheckoutComMerchantConfigurationService;
import com.checkout.hybris.events.services.CheckoutComPaymentEventService;
import com.google.gson.Gson;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.site.BaseSiteService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultCheckoutComRequestEventValidatorTest {

    private static final String SITE_ID = "electronics";
    private static final String SECRET_KEY = "SECRET_KEY";
    private static final String SIGNATURE_KEY = "SIGNATURE_KEY";
    private static final String HEADER_CKO_SIGNATURE = "cko-signature";
    private static final String HEADER_AUTHORIZATION = "authorization";
    private static final String SECRET_KEY_MESSAGE_HASH = "561668318D6FF228571FC7667C70E80BC727B7D3F44AB3130A5E368ABA94C2C0";
    private static final String SIGNATURE_KEY_MESSAGE_HASH = "6E9AE0CC5858BB0A26859F028B11D59F7266640B4911D92BEBF880B60AE053CD";
    private static final String ANOTHER_KEY = "anotherKey";

    @Spy
    @InjectMocks
    private DefaultCheckoutComRequestEventValidator testObj;

    @Mock
    private BaseSiteService baseSiteServiceMock;
    @Mock
    private CheckoutComPaymentEventService checkoutComPaymentEventServiceMock;
    @Mock
    private CheckoutComMerchantConfigurationService checkoutComMerchantConfigurationServiceMock;

    @Mock
    private BaseSiteModel baseSiteModelMock;
    @Mock
    private HttpServletRequest httpServletRequestMock;

    private String eventBody;
    private String emptyEventBody;

    @Before
    public void setUp() {
        eventBody = createEventBody();
        emptyEventBody = createEventBodyEmpty();

        when(checkoutComPaymentEventServiceMock.getSiteIdForTheEvent(new Gson().fromJson(eventBody, Map.class))).thenReturn(SITE_ID);
        when(checkoutComPaymentEventServiceMock.getSiteIdForTheEvent(new Gson().fromJson(emptyEventBody, Map.class))).thenReturn(SITE_ID);
        when(checkoutComMerchantConfigurationServiceMock.getSecretKey()).thenReturn(SECRET_KEY);
        when(checkoutComMerchantConfigurationServiceMock.getSignatureKey()).thenReturn(SIGNATURE_KEY);
        when(httpServletRequestMock.getHeader(HEADER_CKO_SIGNATURE)).thenReturn(SECRET_KEY_MESSAGE_HASH);
        when(httpServletRequestMock.getHeader(HEADER_AUTHORIZATION)).thenReturn(SECRET_KEY);
        when(baseSiteServiceMock.getBaseSiteForUID(SITE_ID)).thenReturn(baseSiteModelMock);
    }

    @Test
    public void isRequestEventValid_whenABCIsActive_andAbcSignatureIsEnabled_andCKOSignatureIsValid_shouldReturnTrue() throws NoSuchAlgorithmException, InvalidKeyException {
        when(checkoutComMerchantConfigurationServiceMock.isNasUsed()).thenReturn(false);
        when(checkoutComMerchantConfigurationServiceMock.isAbcSignatureKeyUsedOnNotificationValidation()).thenReturn(true);
        doReturn(true).when(testObj).isCkoSignatureValid(SECRET_KEY_MESSAGE_HASH, eventBody);

        assertTrue(testObj.isRequestEventValid(httpServletRequestMock, eventBody));
        verify(baseSiteServiceMock).setCurrentBaseSite(baseSiteModelMock, false);
    }

    @Test
    public void isRequestEventValid_whenABCIsActive_andAbcSignatureIsEnabled_andCKOSignatureIsInvalid_shouldReturnTrue() throws NoSuchAlgorithmException, InvalidKeyException {
        when(checkoutComMerchantConfigurationServiceMock.isNasUsed()).thenReturn(false);
        when(checkoutComMerchantConfigurationServiceMock.isAbcSignatureKeyUsedOnNotificationValidation()).thenReturn(true);
        doReturn(false).when(testObj).isCkoSignatureValid(SECRET_KEY_MESSAGE_HASH, eventBody);

        assertFalse(testObj.isRequestEventValid(httpServletRequestMock, eventBody));
        verify(baseSiteServiceMock).setCurrentBaseSite(baseSiteModelMock, false);
    }

    @Test
    public void isRequestEventValid_whenABCIsActive_andAbcSignatureIsDisabled_shouldReturnTrue() throws NoSuchAlgorithmException, InvalidKeyException {
        when(checkoutComMerchantConfigurationServiceMock.isNasUsed()).thenReturn(false);
        when(checkoutComMerchantConfigurationServiceMock.isAbcSignatureKeyUsedOnNotificationValidation()).thenReturn(false);

        assertTrue(testObj.isRequestEventValid(httpServletRequestMock, eventBody));
        verify(baseSiteServiceMock).setCurrentBaseSite(baseSiteModelMock, false);
        verify(testObj, never()).isCkoSignatureValid(SECRET_KEY_MESSAGE_HASH, eventBody);
    }

    @Test
    public void isRequestEventValid_whenNASIsActive_andAuthorizationHeaderIsActive_andAuthorizationHeaderIsInvalid_shouldReturnFalse() throws NoSuchAlgorithmException, InvalidKeyException {
        when(checkoutComMerchantConfigurationServiceMock.isNasUsed()).thenReturn(true);
        when(checkoutComMerchantConfigurationServiceMock.isNasAuthorisationHeaderUsedOnNotificationValidation()).thenReturn(true);
        when(checkoutComMerchantConfigurationServiceMock.getAuthorizationKey()).thenReturn(ANOTHER_KEY);

        assertFalse(testObj.isRequestEventValid(httpServletRequestMock, eventBody));
        verify(baseSiteServiceMock).setCurrentBaseSite(baseSiteModelMock, false);
    }

    @Test
    public void isRequestEventValid_whenNASIsActive_andAuthorizationHeaderIsNotActive_andNasSignatureKeyIsInactive_shouldReturnTrue() throws NoSuchAlgorithmException, InvalidKeyException {
        when(checkoutComMerchantConfigurationServiceMock.isNasUsed()).thenReturn(true);
        when(checkoutComMerchantConfigurationServiceMock.isNasSignatureKeyUsedOnNotificationValidation()).thenReturn(false);
        when(checkoutComMerchantConfigurationServiceMock.getAuthorizationKey()).thenReturn(ANOTHER_KEY);

        assertTrue(testObj.isRequestEventValid(httpServletRequestMock, eventBody));
        verify(baseSiteServiceMock).setCurrentBaseSite(baseSiteModelMock, false);
    }

    @Test
    public void isRequestEventValid_whenNASIsActive_andAuthorizationHeaderIsNotActive_andNasSignatureKeyIsActive_andCKOSignatureIsValid_shouldReturnTrue() throws NoSuchAlgorithmException, InvalidKeyException {
        when(checkoutComMerchantConfigurationServiceMock.isNasUsed()).thenReturn(true);
        when(checkoutComMerchantConfigurationServiceMock.isNasSignatureKeyUsedOnNotificationValidation()).thenReturn(true);
        when(checkoutComMerchantConfigurationServiceMock.getAuthorizationKey()).thenReturn(SECRET_KEY_MESSAGE_HASH);
        doReturn(true).when(testObj).isCkoSignatureValid(SECRET_KEY_MESSAGE_HASH, eventBody);

        assertTrue(testObj.isRequestEventValid(httpServletRequestMock, eventBody));
        verify(baseSiteServiceMock).setCurrentBaseSite(baseSiteModelMock, false);
    }

    @Test
    public void isRequestEventValid_whenNASIsActive_andAuthorizationHeaderIsNotActive_andNasSignatureKeyIsActive_andCKOSignatureIsValid_shouldReturnFalse() throws NoSuchAlgorithmException, InvalidKeyException {
        when(checkoutComMerchantConfigurationServiceMock.isNasUsed()).thenReturn(true);
        when(checkoutComMerchantConfigurationServiceMock.isNasSignatureKeyUsedOnNotificationValidation()).thenReturn(true);
        when(checkoutComMerchantConfigurationServiceMock.getAuthorizationKey()).thenReturn(SECRET_KEY_MESSAGE_HASH);
        when(httpServletRequestMock.getHeader(HEADER_CKO_SIGNATURE)).thenReturn(ANOTHER_KEY);
        doReturn(false).when(testObj).isCkoSignatureValid(ANOTHER_KEY, eventBody);

        assertFalse(testObj.isRequestEventValid(httpServletRequestMock, eventBody));
        verify(baseSiteServiceMock).setCurrentBaseSite(baseSiteModelMock, false);
    }

    @Test
    public void isCkoSignatureValid_whenSignatureNull_ShouldReturnFalse() throws NoSuchAlgorithmException, InvalidKeyException {
        assertFalse(testObj.isCkoSignatureValid(null, eventBody));
    }

    @Test
    public void isCkoSignatureValid_whenBodyNull_ShouldReturnFalse() throws NoSuchAlgorithmException, InvalidKeyException {
        assertFalse(testObj.isCkoSignatureValid(SECRET_KEY_MESSAGE_HASH, null));
    }

    @Test
    public void isCkoSignatureValid_whenBodyEmpty_ShouldThrowException() throws NoSuchAlgorithmException, InvalidKeyException {
        assertFalse(testObj.isCkoSignatureValid(SECRET_KEY_MESSAGE_HASH, emptyEventBody));
    }

    @Test
    public void isCkoSignatureValid_whenABCIsUsed_andEverythingMatches_ShouldReturnTrue() throws NoSuchAlgorithmException, InvalidKeyException {
        assertTrue(testObj.isCkoSignatureValid(SECRET_KEY_MESSAGE_HASH, eventBody));
    }

    @Test
    public void isCkoSignatureValid_whenNASIsUsed_andEverythingMatches_ShouldReturnTrue() throws NoSuchAlgorithmException, InvalidKeyException {
        when(checkoutComMerchantConfigurationServiceMock.isNasUsed()).thenReturn(true);

        assertTrue(testObj.isCkoSignatureValid(SIGNATURE_KEY_MESSAGE_HASH, eventBody));
    }

    private String createEventBody() {
        return "{" +
                "    \"id\": \"evt_dh2zojguo4mebehzbqqltcfuna\"," +
                "    \"type\": \"payment_approved\"," +
                "    \"created_on\": \"2019-12-13T16:33:03Z\"," +
                "    \"data\": {" +
                "        \"id\": \"pay_6qugd47beltevjzfi37ngm2apy\"," +
                "        \"action_id\": \"act_6qugd47beltevjzfi37ngm2apy\"," +
                "        \"payment_type\": \"Regular\"," +
                "        \"auth_code\": \"752422\"," +
                "        \"response_code\": \"10000\"," +
                "        \"response_summary\": \"Approved\"," +
                "        \"scheme_id\": \"638263798022070\"," +
                "        \"source\": {" +
                "            \"id\": \"src_pmhhme2mxmaujobpnxrt5nzm7i\"," +
                "            \"type\": \"card\"," +
                "            \"billing_address\": {" +
                "                \"address_line1\": \"123 Buckingham Palace Rd\"," +
                "                \"address_line2\": \"\"," +
                "                \"city\": \"London\"," +
                "                \"state\": \"\"," +
                "                \"country\": \"GB\"" +
                "            }," +
                "            \"expiry_month\": 1," +
                "            \"expiry_year\": 2029," +
                "            \"name\": \"Automation Bot\"," +
                "            \"scheme\": \"AMEX\"," +
                "            \"last_4\": \"4564\"," +
                "            \"fingerprint\": \"e150213220169d17355e66a7c3cf8edb8f0f9e12050379c49db063a9e59d9431\"," +
                "            \"bin\": \"345678\"," +
                "            \"card_type\": \"Credit\"," +
                "            \"issuer_country\": \"ES\"," +
                "            \"product_type\": \"STANDARD\"," +
                "            \"avs_check\": \"S\"," +
                "            \"cvv_check\": \"Y\"" +
                "        }," +
                "        \"customer\": {" +
                "            \"id\": \"cus_ghy7xcfznu6ujac7sfsbnpepqm\"," +
                "            \"email\": \"automation.bot-46baxldf35qst518ycn89u@checkout.com\"" +
                "        }," +
                "        \"processing\": {" +
                "            \"acquirer_transaction_id\": \"2186916754\"," +
                "            \"retrieval_reference_number\": \"529969706445\"" +
                "        }," +
                "        \"amount\": 5695," +
                "        \"metadata\": {" +
                "            \"site_id\": \"electronics\"" +
                "        }," +
                "        \"risk\": {" +
                "            \"flagged\": false" +
                "        }," +
                "        \"currency\": \"GBP\"," +
                "        \"processed_on\": \"2019-12-13T16:33:03Z\"," +
                "        \"reference\": \"00000003-1576254760720\"" +
                "    }," +
                "    \"_links\": {" +
                "        \"self\": {" +
                "            \"href\": \"https://api.sandbox.checkout.com/events/evt_dh2zojguo4mebehzbqqltcfuna\"" +
                "        }," +
                "        \"payment\": {" +
                "            \"href\": \"https://api.sandbox.checkout.com/payments/pay_6qugd47beltevjzfi37ngm2apy\"" +
                "        }" +
                "    }" +
                "}";
    }

    private String createEventBodyEmpty() {
        return "{}";
    }
}
