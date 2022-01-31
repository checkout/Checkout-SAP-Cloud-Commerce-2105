package com.checkout.hybris.facades.order.converters.populators;

import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.ordermanagementfacades.order.converters.populator.OrderCancelPopulator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

import java.util.Map;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

/**
 * Checkout.com Order Cancel Populator that is responsible of populating Cancellable flag and flattening out MultiD lines.
 * The extension of the ootb populator has been needed because it was populating the data with partial cancellation enabled
 * by default. Checkout.com does not allow partial void.
 */
public class CheckoutComOrderCancelPopulator extends OrderCancelPopulator {

    /**
     * {@inheritDoc}
     */
    @Override
    public void populate(final OrderModel source, final OrderData target) throws ConversionException {
        validateParameterNotNull(source, "Parameter source cannot be null.");
        validateParameterNotNull(target, "Parameter target cannot be null.");
        boolean isFullCancellationAllowed = getOrderCancelService()
                .isCancelPossible(source, getUserService().getCurrentUser(), false, false).isAllowed();
        boolean isPartialCancellationAllowed = getOrderCancelService()
                .isCancelPossible(source, getUserService().getCurrentUser(), false, false).isAllowed();
        target.setCancellable(isFullCancellationAllowed || isPartialCancellationAllowed);

        final Map<AbstractOrderEntryModel, Long> cancellableEntryQuantityMap = getCancelableEntriesStrategy()
                .getAllCancelableEntries(source, getUserService().getCurrentUser());
        cancellableEntryQuantityMap.forEach((entry, qty) -> target.getEntries().forEach(orderEntryData ->
        {
            // Case of MultiD product
            if (isMultidimensionalEntry(orderEntryData)) {
                orderEntryData.getEntries().stream()
                        .filter(nestedOrderEntry -> nestedOrderEntry.getEntryNumber().equals(entry.getEntryNumber()))
                        .forEach(nestedOrderEntryData -> nestedOrderEntryData.setCancellableQty(qty));
            }
            // Case of non MultiD product
            else {
                if (orderEntryData.getEntryNumber().equals(entry.getEntryNumber())) {
                    orderEntryData.setCancellableQty(qty);
                }
            }
        }));
    }
}
