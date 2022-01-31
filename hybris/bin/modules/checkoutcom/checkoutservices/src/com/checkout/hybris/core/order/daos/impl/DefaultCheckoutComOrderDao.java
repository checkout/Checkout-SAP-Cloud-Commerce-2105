package com.checkout.hybris.core.order.daos.impl;

import com.checkout.hybris.core.order.daos.CheckoutComOrderDao;
import com.google.common.base.Preconditions;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.order.daos.impl.DefaultOrderDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.SearchResult;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.empty;

/**
 * Default implementation of {@link CheckoutComOrderDao}
 */
public class DefaultCheckoutComOrderDao extends DefaultOrderDao implements CheckoutComOrderDao {

    private static final String FIND_ORDER_BY_PAYMENT_REFERENCE_QUERY = "SELECT DISTINCT absOrder.PK FROM ( {{ SELECT {" + OrderModel.PK + "} AS PK FROM {" +
            OrderModel._TYPECODE + "!} WHERE {" + OrderModel.CHECKOUTCOMPAYMENTREFERENCE + "} = ?paymentReference" +
            " AND {" + OrderModel.ORIGINALVERSION + "} IS NULL }} UNION ALL {{ SELECT {" + CartModel.PK + "} AS PK FROM {" + CartModel._TYPECODE + "!} WHERE {"
            + CartModel.CHECKOUTCOMPAYMENTREFERENCE + "} = ?paymentReference }} ) absOrder";

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<AbstractOrderModel> findAbstractOrderForPaymentReferenceNumber(final String paymentReference) {
        Preconditions.checkArgument(StringUtils.isNotBlank(paymentReference), "The checkout.com payment reference number cannot be null.");

        final Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("paymentReference", paymentReference);

        final FlexibleSearchQuery fQuery = new FlexibleSearchQuery(FIND_ORDER_BY_PAYMENT_REFERENCE_QUERY);
        fQuery.addQueryParameters(queryParams);

        final SearchResult<AbstractOrderModel> searchResult = getFlexibleSearchService().search(fQuery);
        return searchResult.getResult().isEmpty() ? empty() : Optional.of(searchResult.getResult().get(0));
    }
}
