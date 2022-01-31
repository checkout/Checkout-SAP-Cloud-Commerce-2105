package com.checkout.hybris.core.payment.daos.impl;

import com.checkout.hybris.core.payment.daos.CheckoutComPaymentInfoDao;
import com.google.common.base.Preconditions;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import de.hybris.platform.servicelayer.internal.dao.AbstractItemDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.SearchResult;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of {@link CheckoutComPaymentInfoDao}
 */
public class DefaultCheckoutComPaymentInfoDao extends AbstractItemDao implements CheckoutComPaymentInfoDao {

    protected static final String FIND_PAYMENT_INFO_BY_PAYMENT_ID_QUERY = "SELECT {" + PaymentInfoModel.PK + "}" +
            " FROM {" + PaymentInfoModel._TYPECODE + "} WHERE {" + PaymentInfoModel.PAYMENTID + "} = ?paymentId";

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PaymentInfoModel> findPaymentInfosByPaymentId(final String paymentId) {
        Preconditions.checkArgument(StringUtils.isNotBlank(paymentId), "The checkout.com payment id cannot be null.");

        final Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("paymentId", paymentId);

        final FlexibleSearchQuery fQuery = new FlexibleSearchQuery(FIND_PAYMENT_INFO_BY_PAYMENT_ID_QUERY);
        fQuery.addQueryParameters(queryParams);

        final SearchResult<PaymentInfoModel> searchResult = getFlexibleSearchService().search(fQuery);
        return searchResult.getResult();
    }
}