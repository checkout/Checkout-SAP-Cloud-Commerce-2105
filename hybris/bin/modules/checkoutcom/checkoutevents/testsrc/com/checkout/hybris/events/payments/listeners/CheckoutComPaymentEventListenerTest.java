package com.checkout.hybris.events.payments.listeners;

import com.checkout.hybris.core.payment.services.CheckoutComPaymentInfoService;
import com.checkout.hybris.events.enums.CheckoutComPaymentEventType;
import com.checkout.hybris.events.model.CheckoutComPaymentEventModel;
import com.checkout.hybris.events.payments.CheckoutComPaymentEvent;
import com.checkout.hybris.events.services.CheckoutComPaymentEventService;
import com.google.gson.Gson;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.model.ModelService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComPaymentEventListenerTest {

    private static final String ELECTRONICS = "electronics";
    private static final String PAYMENT_ID = "pay_6qugd47beltevjzfi37ngm2apy";
    private static final HashSet<CheckoutComPaymentEventType> EVENT_TYPES = new HashSet<>(Arrays.asList(CheckoutComPaymentEventType.PAYMENT_APPROVED, CheckoutComPaymentEventType.PAYMENT_REFUNDED));

    @InjectMocks
    private CheckoutComPaymentEventListener testObj;

    @Mock
    private CheckoutComPaymentEventService checkoutComPaymentEventServiceMock;
    @Mock
    private ModelService modelServiceMock;
    @Mock
    private Converter<Map, CheckoutComPaymentEventModel> checkoutComPaymentEventReverseConverterMock;
    @Mock
    private CheckoutComPaymentInfoService paymentInfoServiceMock;
    @Mock
    private CheckoutComPaymentEventModel checkoutComPaymentEventModelMock;
    private Map sourceMap;

    @Before
    public void setUp() {
        sourceMap = createMapEventBody();
        when(paymentInfoServiceMock.getSiteIdFromPaymentId(PAYMENT_ID)).thenReturn(ELECTRONICS);
        when(checkoutComPaymentEventServiceMock.getAllowedPaymentEventTypesForMerchant(ELECTRONICS)).thenReturn(EVENT_TYPES);
        when(checkoutComPaymentEventReverseConverterMock.convert(sourceMap)).thenReturn(checkoutComPaymentEventModelMock);
        when(checkoutComPaymentEventServiceMock.getSiteIdForTheEvent(sourceMap)).thenReturn(ELECTRONICS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void onEvent_WhenEventBodyNull_ShouldThrowException() {
        final CheckoutComPaymentEvent event = new CheckoutComPaymentEvent(null);

        testObj.onEvent(event);
    }

    @Test(expected = IllegalArgumentException.class)
    public void onEvent_WhenEventBodyEmpty_ShouldThrowException() {
        final CheckoutComPaymentEvent event = new CheckoutComPaymentEvent(new HashMap());

        testObj.onEvent(event);
    }

    @Test(expected = IllegalArgumentException.class)
    public void onEvent_WhenEventTypeNull_ShouldThrowException() {
        sourceMap.replace("type", null);
        final CheckoutComPaymentEvent event = new CheckoutComPaymentEvent(sourceMap);

        testObj.onEvent(event);
    }

    @Test
    public void onEvent_WhenEventTypeInvalid_ShouldDoNothing() {
        when(checkoutComPaymentEventServiceMock.getAllowedPaymentEventTypesForMerchant(ELECTRONICS)).thenReturn(Collections.emptySet());
        final CheckoutComPaymentEvent event = new CheckoutComPaymentEvent(sourceMap);

        testObj.onEvent(event);

        verifyZeroInteractions(checkoutComPaymentEventReverseConverterMock);
        verifyZeroInteractions(modelServiceMock);
    }

    @Test
    public void onEvent_WhenEventBodyIsValid_ShouldSaveTheEvent() {
        final CheckoutComPaymentEvent event = new CheckoutComPaymentEvent(sourceMap);

        testObj.onEvent(event);

        verify(checkoutComPaymentEventServiceMock).getSiteIdForTheEvent(sourceMap);
        verify(checkoutComPaymentEventReverseConverterMock).convert(sourceMap);
        verify(modelServiceMock).save(checkoutComPaymentEventModelMock);
    }

    private Map createMapEventBody() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("    \"id\": \"evt_dh2zojguo4mebehzbqqltcfuna\",");
        sb.append("    \"type\": \"payment_approved\",");
        sb.append("    \"created_on\": \"2019-12-13T16:33:03Z\",");
        sb.append("    \"data\": {");
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
        sb.append("},");
        sb.append("        \"customer\": {");
        sb.append("            \"id\": \"cus_ghy7xcfznu6ujac7sfsbnpepqm\",");
        sb.append("            \"email\": \"automation.bot-46baxldf35qst518ycn89u@checkout.com\"");
        sb.append("},");
        sb.append("        \"processing\": {");
        sb.append("            \"acquirer_transaction_id\": \"2186916754\",");
        sb.append("            \"retrieval_reference_number\": \"529969706445\"");
        sb.append("},");
        sb.append("        \"amount\": 5695,");
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
