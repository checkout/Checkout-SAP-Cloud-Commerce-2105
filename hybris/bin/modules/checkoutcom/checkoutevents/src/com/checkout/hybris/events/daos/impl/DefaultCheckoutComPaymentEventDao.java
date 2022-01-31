package com.checkout.hybris.events.daos.impl;

import com.checkout.hybris.events.daos.CheckoutComPaymentEventDao;
import com.checkout.hybris.events.enums.CheckoutComPaymentEventStatus;
import com.checkout.hybris.events.enums.CheckoutComPaymentEventType;
import com.checkout.hybris.events.model.CheckoutComPaymentEventModel;
import com.google.common.base.Preconditions;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static de.hybris.platform.core.model.ItemModel.PK;
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

/**
 * Default implementation of {@link CheckoutComPaymentEventDao}
 */
public class DefaultCheckoutComPaymentEventDao implements CheckoutComPaymentEventDao {

    protected static final String QUERY_PARAM_PAYMENT_EVENT_TYPES = "checkoutComPaymentEventTypes";
    protected static final String QUERY_PARAM_PAYMENT_EVENT_STATUS = "checkoutComPaymentEventStatus";
    protected static final String QUERY_PARAM_CREATED_BEFORE_DATE = "createdBeforeDate";

    protected static final String FIND_PAYMENT_EVENT_TO_PROCESS_QUERY = "SELECT {pe." + CheckoutComPaymentEventModel.PK +
            "} FROM {" + CheckoutComPaymentEventModel._TYPECODE + " as pe JOIN " + CheckoutComPaymentEventStatus._TYPECODE +
            " as pes ON {pes.pk} = {pe. " + CheckoutComPaymentEventModel.STATUS + "}} WHERE {pe." + CheckoutComPaymentEventModel.EVENTTYPE +
            "} IN (?" + QUERY_PARAM_PAYMENT_EVENT_TYPES + ")" + " AND {pes.code} = '" + CheckoutComPaymentEventStatus.PENDING + "'";

    protected static final String FIND_PAYMENT_EVENT_BY_STATUS_AND_CREATION_DATE_QUERY =
            "SELECT {" + PK + "}\n" +
                    "FROM {" + CheckoutComPaymentEventModel._TYPECODE + "}\n" +
                    "WHERE {" + CheckoutComPaymentEventModel.STATUS + "} = ?" + QUERY_PARAM_PAYMENT_EVENT_STATUS + "\n" +
                    "AND {" + CheckoutComPaymentEventModel.CREATIONTIME + "} < ?" + QUERY_PARAM_CREATED_BEFORE_DATE;

    protected final FlexibleSearchService flexibleSearchService;

    public DefaultCheckoutComPaymentEventDao(final FlexibleSearchService flexibleSearchService) {
        this.flexibleSearchService = flexibleSearchService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CheckoutComPaymentEventModel> findPaymentEventToProcessForTypes(final Set<CheckoutComPaymentEventType> checkoutComPaymentEventTypes) {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(checkoutComPaymentEventTypes), "Payment event types are null or empty.");

        final Map<String, Object> queryParams = new HashMap<>();
        queryParams.put(QUERY_PARAM_PAYMENT_EVENT_TYPES, checkoutComPaymentEventTypes.stream().map(CheckoutComPaymentEventType::getCode).collect(Collectors.toList()));

        final FlexibleSearchQuery fQuery = new FlexibleSearchQuery(FIND_PAYMENT_EVENT_TO_PROCESS_QUERY);
        fQuery.addQueryParameters(queryParams);
        fQuery.setResultClassList(Collections.singletonList(CheckoutComPaymentEventModel.class));

        final SearchResult<CheckoutComPaymentEventModel> searchResult = flexibleSearchService.search(fQuery);
        return searchResult.getResult();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CheckoutComPaymentEventModel> findPaymentEventsByStatusCreatedBeforeDate(final CheckoutComPaymentEventStatus paymentEventStatus, final Date creationDate) {
        validateParameterNotNull(paymentEventStatus, "Payment Event Status cannot be null");
        validateParameterNotNull(creationDate, "Creation Date cannot be null");

        final FlexibleSearchQuery query = new FlexibleSearchQuery(FIND_PAYMENT_EVENT_BY_STATUS_AND_CREATION_DATE_QUERY);
        query.addQueryParameter(QUERY_PARAM_PAYMENT_EVENT_STATUS, paymentEventStatus);
        query.addQueryParameter(QUERY_PARAM_CREATED_BEFORE_DATE, creationDate);

        final SearchResult<CheckoutComPaymentEventModel> result = flexibleSearchService.search(query);
        return result.getResult();
    }

}
