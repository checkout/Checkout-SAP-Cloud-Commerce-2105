package com.checkout.hybris.events.populators;

import com.checkout.hybris.core.currency.services.CheckoutComCurrencyService;
import com.checkout.hybris.events.enums.CheckoutComPaymentEventStatus;
import com.checkout.hybris.events.model.CheckoutComPaymentEventModel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.Map;

import static com.checkout.hybris.events.constants.CheckouteventsConstants.EVENT_APPROVED_RESPONSE_CODE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComPaymentEventReversePopulatorTest {

    private static final String PAYMENT_ID_VALUE = "pay_6qugd47beltevjzfi37ngm2apy";
    private static final String CURRENCY_CODE = "GBP";
    private static final Long CHECKOUTCOM_AMOUNT = 5695L;
    private static final BigDecimal AUTHORISATION_AMOUNT = BigDecimal.valueOf(56.95d);

    @InjectMocks
    private CheckoutComPaymentEventReversePopulator testObj;

    @Mock
    private CommonI18NService commonI18NServiceMock;
    @Mock
    private CheckoutComCurrencyService checkoutComCurrencyServiceMock;
    @Mock
    private CurrencyModel currencyModelMock;

    private CheckoutComPaymentEventModel target;

    @Before
    public void setUp() {
        when(commonI18NServiceMock.getCurrency(CURRENCY_CODE)).thenReturn(currencyModelMock);
        when(checkoutComCurrencyServiceMock.convertAmountFromPennies(CURRENCY_CODE, CHECKOUTCOM_AMOUNT)).thenReturn(AUTHORISATION_AMOUNT);
        target = new CheckoutComPaymentEventModel();
    }

    @Test(expected = IllegalArgumentException.class)
    public void populate_WhenSourceNull_ShouldThrowException() {
        testObj.populate(null, target);
    }

    @Test
    public void populate_WhenSourceValid_ShouldPopulateTargetCorrectly() {
        final Map mapWithActionIdAndResponseCode = createMapEventBody(true, true);
        testObj.populate(mapWithActionIdAndResponseCode, target);

        assertNotNull(target);
        assertEquals(currencyModelMock, target.getCurrency());
        assertEquals("evt_dh2zojguo4mebehzbqqltcfuna", target.getEventId());
        assertEquals("payment_approved", target.getEventType());
        assertEquals(PAYMENT_ID_VALUE, target.getPaymentId());
        assertEquals("00000003-1576254760720", target.getPaymentReference());
        assertEquals(EVENT_APPROVED_RESPONSE_CODE, target.getResponseCode());
        assertEquals("act_6qugd47beltevjzfi37ngm2apy", target.getActionId());
        assertEquals("Approved", target.getResponseSummary());
        assertEquals(false, target.getRiskFlag());
        assertEquals("electronics", target.getSiteId());
        assertEquals("card", target.getSourceType());
        assertEquals(CheckoutComPaymentEventStatus.PENDING, target.getStatus());
        Gson gson = new GsonBuilder().create();
        assertEquals(gson.toJson(mapWithActionIdAndResponseCode), target.getPayload());
    }

    @Test
    public void populate_WhenActionIdIsNull_ShouldPopulateActionIdWithPaymentId() {
        final Map mapWithoutActionId = createMapEventBody(false, true);

        testObj.populate(mapWithoutActionId, target);

        assertNotNull(target);

        assertEquals(PAYMENT_ID_VALUE, target.getPaymentId());
        assertEquals(PAYMENT_ID_VALUE, target.getActionId());

        Gson gson = new GsonBuilder().create();
        assertEquals(gson.toJson(mapWithoutActionId), target.getPayload());
    }

    @Test
    public void populate_WhenResponseCodeIsNull_ShouldPopulateActionIdWithPaymentId() {
        final Map mapWithoutResponseCode = createMapEventBody(true, false);

        testObj.populate(mapWithoutResponseCode, target);

        assertNotNull(target);
        assertEquals(EVENT_APPROVED_RESPONSE_CODE, target.getResponseCode());

        Gson gson = new GsonBuilder().create();
        assertEquals(gson.toJson(mapWithoutResponseCode), target.getPayload());
    }

    private Map createMapEventBody(final boolean actionIdPresent, final boolean responseCodePresent) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("    \"id\": \"evt_dh2zojguo4mebehzbqqltcfuna\",");
        sb.append("    \"type\": \"payment_approved\",");
        sb.append("    \"created_on\": \"2019-12-13T16:33:03Z\",");
        sb.append("    \"data\": {");
        if (actionIdPresent) {
            sb.append("        \"action_id\": \"act_6qugd47beltevjzfi37ngm2apy\",");
        }
        sb.append("        \"payment_type\": \"Regular\",");
        sb.append("        \"auth_code\": \"752422\",");
        if (responseCodePresent) {
            sb.append("        \"response_code\": \"10000\",");
        }
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
        sb.append("},");
        sb.append("        \"customer\": {");
        sb.append("            \"id\": \"cus_ghy7xcfznu6ujac7sfsbnpepqm\",");
        sb.append("            \"email\": \"automation.bot-46baxldf35qst518ycn89u@checkout.com\"");
        sb.append("},");
        sb.append("        \"processing\": {");
        sb.append("            \"acquirer_transaction_id\": \"2186916754\",");
        sb.append("            \"retrieval_reference_number\": \"529969706445\"");
        sb.append("},");
        sb.append("        \"amount\": 5695");
        sb.append("        ,");
        sb.append("        \"metadata\": {");
        sb.append("            \"site_id\": \"electronics\"");
        sb.append("},");
        sb.append("        \"risk\": {");
        sb.append("            \"flagged\": false");
        sb.append("},");
        sb.append("        \"id\": \"pay_6qugd47beltevjzfi37ngm2apy\",");
        sb.append("        \"currency\": \"GBP\",");
        sb.append("        \"processed_on\": \"2019-12-13T16:33:03Z\",");
        sb.append("        \"reference\": \"00000003-1576254760720\"");
        sb.append("    },");
        sb.append("    \"_links\": {");
        sb.append("        \"self\": {");
        sb.append("            \"href\": \"https://api.sandbox.checkout.com/events/evt_dh2zojguo4mebehzbqqltcfuna\"");
        sb.append("},");
        sb.append("        \"payment\": {");
        sb.append("            \"href\": \"https://api.sandbox.checkout.com/payments/pay_6qugd47beltevjzfi37ngm2apy\"");
        sb.append("        }");
        sb.append("    }");
        sb.append("}");
        return new Gson().fromJson(sb.toString(), Map.class);
    }
}
