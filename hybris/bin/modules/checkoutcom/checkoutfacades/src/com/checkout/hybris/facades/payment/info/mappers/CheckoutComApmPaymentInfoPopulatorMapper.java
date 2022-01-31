package com.checkout.hybris.facades.payment.info.mappers;

import com.checkout.hybris.core.model.CheckoutComAPMPaymentInfoModel;
import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.facades.beans.CheckoutComPaymentInfoData;
import de.hybris.platform.converters.Populator;
import org.apache.commons.collections4.MapUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Registers and finds the populator with the given key
 */
public class CheckoutComApmPaymentInfoPopulatorMapper {

    protected final Populator<CheckoutComAPMPaymentInfoModel, CheckoutComPaymentInfoData> checkoutComDefaultApmPaymentInfoPopulator;

    protected final Map<CheckoutComPaymentType, Populator<CheckoutComAPMPaymentInfoModel, CheckoutComPaymentInfoData>> populators = new HashMap<>();

    public CheckoutComApmPaymentInfoPopulatorMapper(final Populator<CheckoutComAPMPaymentInfoModel, CheckoutComPaymentInfoData> checkoutComDefaultApmPaymentInfoPopulator) {
        this.checkoutComDefaultApmPaymentInfoPopulator = checkoutComDefaultApmPaymentInfoPopulator;
    }

    /**
     * Adds the populator with related key
     *
     * @param key   the populator key
     * @param value the populator
     */
    public void addPopulator(final CheckoutComPaymentType key, final Populator<CheckoutComAPMPaymentInfoModel, CheckoutComPaymentInfoData> value) {
        populators.put(key, value);
    }

    /**
     * Gets the populator for the given key
     *
     * @param key the populator key
     * @return the populator
     */
    public Populator<CheckoutComAPMPaymentInfoModel, CheckoutComPaymentInfoData> findPopulator(final CheckoutComPaymentType key) {
        if (MapUtils.isNotEmpty(populators) && populators.containsKey(key)) {
            return populators.get(key);
        }
        return checkoutComDefaultApmPaymentInfoPopulator;
    }
}
