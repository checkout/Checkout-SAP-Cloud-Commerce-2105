package com.checkout.hybris.events.services.impl;

import com.checkout.hybris.core.model.CheckoutComMerchantConfigurationModel;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentInfoService;
import com.checkout.hybris.events.enums.CheckoutComPaymentEventType;
import com.google.gson.Gson;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.cms2.servicelayer.services.CMSSiteService;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.checkout.hybris.events.enums.CheckoutComPaymentEventType.PAYMENT_APPROVED;
import static com.checkout.hybris.events.enums.CheckoutComPaymentEventType.PAYMENT_REFUNDED;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultCheckoutComPaymentEventServiceTest {

    private static final String SITE_ID = "electronics";
    private static final String PAYMENT_ID = "pay_6qugd47beltevjzfi37ngm2apy";

    private static final HashSet<CheckoutComPaymentEventType> EVENT_TYPES = new HashSet<>(asList(PAYMENT_APPROVED, PAYMENT_REFUNDED));

    @InjectMocks
    private DefaultCheckoutComPaymentEventService testObj;

    @Mock
    private CMSSiteService cmsSiteServiceMock;
    @Mock
    private CMSSiteModel cmsSiteMock;
    @Mock
    private CheckoutComMerchantConfigurationModel merchantConfigMock;
    @Mock
    private CheckoutComPaymentInfoService paymentInfoServiceMock;

    private String validEventBody;

    @Before
    public void setUp() {
        validEventBody = createEventBody();
        when(cmsSiteServiceMock.getSites()).thenReturn(singletonList(cmsSiteMock));
        when(cmsSiteMock.getUid()).thenReturn(SITE_ID);
        when(cmsSiteMock.getCheckoutComMerchantConfiguration()).thenReturn(merchantConfigMock);
        when(merchantConfigMock.getCheckoutComPaymentEventTypes()).thenReturn(EVENT_TYPES);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getAllowedPaymentEventTypesForMerchant_WhenSiteIdNull_ShouldThrowException() {
        testObj.getAllowedPaymentEventTypesForMerchant(null);
    }

    @Test
    public void getAllowedPaymentEventTypesForMerchant_WhenNoSites_ShouldReturnEmptySet() {
        when(cmsSiteServiceMock.getSites()).thenReturn(emptyList());

        final Set<CheckoutComPaymentEventType> result = testObj.getAllowedPaymentEventTypesForMerchant(SITE_ID);

        assertTrue(CollectionUtils.isEmpty(result));
    }

    @Test
    public void getAllowedPaymentEventTypesForMerchant_WhenSiteIsValid_ShouldReturnTheEventTypeList() {
        when(cmsSiteServiceMock.getSites()).thenReturn(singletonList(cmsSiteMock));
        when(merchantConfigMock.getCheckoutComPaymentEventTypes()).thenReturn(EVENT_TYPES);

        final Set<CheckoutComPaymentEventType> result = testObj.getAllowedPaymentEventTypesForMerchant(SITE_ID);

        assertEquals(EVENT_TYPES, result);
        assertTrue(result.contains(PAYMENT_APPROVED));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getSiteIdForTheEvent_WhenBodyNull_ShouldReturnFalse() {
        testObj.getSiteIdForTheEvent(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getSiteIdForTheEvent_WhenBodyEmpty_ShouldThrowException() {
        testObj.getSiteIdForTheEvent(new Gson().fromJson(createEventBodyEmpty(), Map.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getSiteIdForTheEvent_WhenDataBodyEmpty_ShouldThrowException() {
        testObj.getSiteIdForTheEvent(new Gson().fromJson(createDataBodyEmpty(), Map.class));
    }

    @Test
    public void getSiteIdForTheEvent_WhenMetadataBodyEmpty_ShouldGetSitIdFromRelatedAbstractOrder() {
        when(paymentInfoServiceMock.getSiteIdFromPaymentId(PAYMENT_ID)).thenReturn(SITE_ID);

        final String result = testObj.getSiteIdForTheEvent(new Gson().fromJson(createMetadataBodyEmpty(), Map.class));

        assertEquals(SITE_ID, result);
    }

    @Test
    public void getSiteIdForTheEvent_WhenSiteIdPresentOnEvent_ShouldReturnSiteId() {
        final String result = testObj.getSiteIdForTheEvent(new Gson().fromJson(validEventBody, Map.class));

        assertEquals(SITE_ID, result);
        verify(paymentInfoServiceMock, never()).getSiteIdFromPaymentId(anyString());
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

    private String createDataBodyEmpty() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("    \"data\": {");
        sb.append("        }");
        sb.append("}");
        return sb.toString();
    }

    private String createMetadataBodyEmpty() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("    \"data\": {");
        sb.append("        \"id\": \"pay_6qugd47beltevjzfi37ngm2apy\",");
        sb.append("        \"metadata\": {");
        sb.append("             }");
        sb.append("        }");
        sb.append("}");
        return sb.toString();
    }

    private String createEventBodyEmpty() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("}");
        return sb.toString();
    }
}
