package com.checkout.hybris.events.validators.impl;

import com.checkout.hybris.core.merchant.services.CheckoutComMerchantConfigurationService;
import com.checkout.hybris.events.services.CheckoutComPaymentEventService;
import com.google.gson.Gson;
import de.hybris.bootstrap.annotations.UnitTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultCheckoutComRequestEventValidatorTest {

    private static final String SECRET_KEY = "SECRET_KEY";
    private static final String SIGNATURE = "561668318D6FF228571FC7667C70E80BC727B7D3F44AB3130A5E368ABA94C2C0";
    private static final String SITE_ID = "electronics";

    @InjectMocks
    private DefaultCheckoutComRequestEventValidator testObj;

    @Mock
    private CheckoutComMerchantConfigurationService checkoutComMerchantConfigurationServiceMock;
    @Mock
    private CheckoutComPaymentEventService checkoutComPaymentEventServiceMock;

    private String eventBody;
    private String emptyEventBody;

    @Before
    public void setUp() {
        eventBody = createEventBody();
        emptyEventBody = createEventBodyEmpty();

        when(checkoutComPaymentEventServiceMock.getSiteIdForTheEvent(new Gson().fromJson(eventBody, Map.class))).thenReturn(SITE_ID);
        when(checkoutComPaymentEventServiceMock.getSiteIdForTheEvent(new Gson().fromJson(emptyEventBody, Map.class))).thenReturn(SITE_ID);
        when(checkoutComMerchantConfigurationServiceMock.getSecretKeyForSite(SITE_ID)).thenReturn(SECRET_KEY);
    }

    @Test
    public void isCkoSignatureValid_WhenSignatureNull_ShouldReturnFalse() throws NoSuchAlgorithmException, InvalidKeyException {
        assertFalse(testObj.isCkoSignatureValid(null, eventBody));
    }

    @Test
    public void isCkoSignatureValid_WhenBodyNull_ShouldReturnFalse() throws NoSuchAlgorithmException, InvalidKeyException {
        assertFalse(testObj.isCkoSignatureValid(SIGNATURE, null));
    }

    @Test
    public void isCkoSignatureValid_WhenBodyEmpty_ShouldThrowException() throws NoSuchAlgorithmException, InvalidKeyException {
        assertFalse(testObj.isCkoSignatureValid(SIGNATURE, emptyEventBody));
    }

    @Test
    public void isCkoSignatureValid_WhenEverythingMatch_ShouldReturnTrue() throws NoSuchAlgorithmException, InvalidKeyException {
        assertTrue(testObj.isCkoSignatureValid(SIGNATURE, eventBody));
    }

    private String createEventBody() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("    \"id\": \"evt_dh2zojguo4mebehzbqqltcfuna\",");
        sb.append("    \"type\": \"payment_approved\",");
        sb.append("    \"created_on\": \"2019-12-13T16:33:03Z\",");
        sb.append("    \"data\": {");
        sb.append("        \"id\": \"pay_6qugd47beltevjzfi37ngm2apy\",");
        sb.append("        \"action_id\": \"act_6qugd47beltevjzfi37ngm2apy\",");
        sb.append("        \"payment_type\": \"Regular\",");
        sb.append("        \"auth_code\": \"752422\",");
        sb.append("        \"response_code\": \"10000\",");
        sb.append("        \"response_summary\": \"Approved\",");
        sb.append("        \"scheme_id\": \"638263798022070\",");
        sb.append("        \"source\": {");
        sb.append("            \"id\": \"src_pmhhme2mxmaujobpnxrt5nzm7i\",");
        sb.append("            \"type\": \"card\",");
        sb.append("            \"billing_address\": {");
        sb.append("                \"address_line1\": \"123 Buckingham Palace Rd\",");
        sb.append("                \"address_line2\": \"\",");
        sb.append("                \"city\": \"London\",");
        sb.append("                \"state\": \"\",");
        sb.append("                \"country\": \"GB\"");
        sb.append("            },");
        sb.append("            \"expiry_month\": 1,");
        sb.append("            \"expiry_year\": 2029,");
        sb.append("            \"name\": \"Automation Bot\",");
        sb.append("            \"scheme\": \"AMEX\",");
        sb.append("            \"last_4\": \"4564\",");
        sb.append("            \"fingerprint\": \"e150213220169d17355e66a7c3cf8edb8f0f9e12050379c49db063a9e59d9431\",");
        sb.append("            \"bin\": \"345678\",");
        sb.append("            \"card_type\": \"Credit\",");
        sb.append("            \"issuer_country\": \"ES\",");
        sb.append("            \"product_type\": \"STANDARD\",");
        sb.append("            \"avs_check\": \"S\",");
        sb.append("            \"cvv_check\": \"Y\"");
        sb.append("        },");
        sb.append("        \"customer\": {");
        sb.append("            \"id\": \"cus_ghy7xcfznu6ujac7sfsbnpepqm\",");
        sb.append("            \"email\": \"automation.bot-46baxldf35qst518ycn89u@checkout.com\"");
        sb.append("        },");
        sb.append("        \"processing\": {");
        sb.append("            \"acquirer_transaction_id\": \"2186916754\",");
        sb.append("            \"retrieval_reference_number\": \"529969706445\"");
        sb.append("        },");
        sb.append("        \"amount\": 5695,");
        sb.append("        \"metadata\": {");
        sb.append("            \"site_id\": \"electronics\"");
        sb.append("        },");
        sb.append("        \"risk\": {");
        sb.append("            \"flagged\": false");
        sb.append("        },");
        sb.append("        \"currency\": \"GBP\",");
        sb.append("        \"processed_on\": \"2019-12-13T16:33:03Z\",");
        sb.append("        \"reference\": \"00000003-1576254760720\"");
        sb.append("    },");
        sb.append("    \"_links\": {");
        sb.append("        \"self\": {");
        sb.append("            \"href\": \"https://api.sandbox.checkout.com/events/evt_dh2zojguo4mebehzbqqltcfuna\"");
        sb.append("        },");
        sb.append("        \"payment\": {");
        sb.append("            \"href\": \"https://api.sandbox.checkout.com/payments/pay_6qugd47beltevjzfi37ngm2apy\"");
        sb.append("        }");
        sb.append("    }");
        sb.append("}");
        return sb.toString();
    }

    private String createEventBodyEmpty() {
        return "{}";
    }
}
