package com.checkout.hybris.events.populators;

import com.checkout.hybris.core.currency.services.CheckoutComCurrencyService;
import com.checkout.hybris.events.enums.CheckoutComPaymentEventStatus;
import com.checkout.hybris.events.model.CheckoutComPaymentEventModel;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import org.apache.commons.collections4.MapUtils;

import java.util.Map;

import static com.checkout.hybris.events.constants.CheckouteventsConstants.EVENT_APPROVED_RESPONSE_CODE;

/**
 * Populates the properties of the {@link CheckoutComPaymentEventModel}
 */
public class CheckoutComPaymentEventReversePopulator implements Populator<Map, CheckoutComPaymentEventModel> {

    protected static final String KEY_ID = "id";
    protected static final String KEY_SOURCE_TYPE = "type";
    protected static final String KEY_DATA = "data";
    protected static final String KEY_ACTION_ID = "action_id";
    protected static final String KEY_CURRENCY = "currency";
    protected static final String KEY_AMOUNT = "amount";
    protected static final String KEY_RESPONSE_SUMMARY = "response_summary";
    protected static final String KEY_RESPONSE_CODE = "response_code";
    protected static final String KEY_SITE_ID = "site_id";
    protected static final String KEY_REFERENCE = "reference";
    protected static final String KEY_SOURCE = "source";
    protected static final String KEY_RISK = "risk";
    protected static final String KEY_FLAGGED = "flagged";
    protected static final String KEY_METADATA = "metadata";
    protected static final String KEY_EVENT_TYPE = "type";

    protected final CommonI18NService commonI18NService;
    protected final CheckoutComCurrencyService checkoutComCurrencyService;

    public CheckoutComPaymentEventReversePopulator(final CommonI18NService commonI18NService, final CheckoutComCurrencyService checkoutComCurrencyService) {
        this.commonI18NService = commonI18NService;
        this.checkoutComCurrencyService = checkoutComCurrencyService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void populate(final Map source, final CheckoutComPaymentEventModel target) throws ConversionException {
        Preconditions.checkArgument(MapUtils.isNotEmpty(source), "CheckoutComPaymentEvent body cannot be null.");

        target.setEventId((String) source.get(KEY_ID));
        target.setEventType((String) source.get(KEY_EVENT_TYPE));
        target.setStatus(CheckoutComPaymentEventStatus.PENDING);
        final Gson gson = new GsonBuilder().create();
        target.setPayload(gson.toJson(source));

        populateDataAttributes(source, target);
    }

    protected void populateDataAttributes(final Map<String, Object> source, final CheckoutComPaymentEventModel target) {
        if (source.containsKey(KEY_DATA)) {
            final Map dataMap = (Map) source.get(KEY_DATA);
            target.setResponseSummary((String) dataMap.get(KEY_RESPONSE_SUMMARY));
            target.setResponseCode(dataMap.containsKey(KEY_RESPONSE_CODE) ? (String) dataMap.get(KEY_RESPONSE_CODE) : EVENT_APPROVED_RESPONSE_CODE);
            target.setPaymentReference((String) dataMap.get(KEY_REFERENCE));
            target.setPaymentId((String) dataMap.get(KEY_ID));
            target.setActionId(dataMap.containsKey(KEY_ACTION_ID) ? (String) dataMap.get(KEY_ACTION_ID) : (String) dataMap.get(KEY_ID));

            if (dataMap.containsKey(KEY_METADATA)) {
                final Map metadataMap = (Map) dataMap.get(KEY_METADATA);
                target.setSiteId((String) metadataMap.get(KEY_SITE_ID));
            }

            if (dataMap.containsKey(KEY_RISK)) {
                final Map riskMap = (Map) dataMap.get(KEY_RISK);
                target.setRiskFlag((Boolean) riskMap.get(KEY_FLAGGED));
            }

            if (dataMap.containsKey(KEY_SOURCE)) {
                final Map sourceMap = (Map) dataMap.get(KEY_SOURCE);
                target.setSourceType((String) sourceMap.get(KEY_SOURCE_TYPE));
            }

            if (dataMap.containsKey(KEY_CURRENCY) && dataMap.containsKey(KEY_AMOUNT)) {
                final String currencyCode = (String) dataMap.get(KEY_CURRENCY);
                target.setCurrency(commonI18NService.getCurrency(currencyCode));
                target.setAmount(checkoutComCurrencyService.convertAmountFromPennies(currencyCode, ((Double) dataMap.get(KEY_AMOUNT)).longValue()));
            }
        }
    }

}
