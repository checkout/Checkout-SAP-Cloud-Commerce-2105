package com.checkout.hybris.events.order.process.daos.impl;

import com.checkout.hybris.events.order.process.daos.CheckoutComProcessDefinitionDao;
import com.checkout.hybris.fulfilmentprocess.model.CheckoutComVoidProcessModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.processengine.model.BusinessProcessModel;
import de.hybris.platform.returns.model.ReturnProcessModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;

import java.util.Collections;
import java.util.List;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

/**
 * The default implementation of {@link CheckoutComProcessDefinitionDao} interface.
 */
@SuppressWarnings("squid:S1192")
public class DefaultCheckoutComProcessDefinitionDao implements CheckoutComProcessDefinitionDao {

    protected static final String QUERY_PARAM_ORDER_CODE = "orderCode";
    protected static final String QUERY_PARAM_ORDER_PROCESS_NAME = "orderProcessDefinitionName";
    protected static final String QUERY_PARAM_REFUND_ACTION_ID = "refundActionId";

    private static final String FIND_WAITING_ORDER_PROCESSES_QUERY = "SELECT {op.PK} FROM {"
            + OrderProcessModel._TYPECODE + " AS op JOIN " + OrderModel._TYPECODE + " as o ON {op." + OrderProcessModel.ORDER + "} = {o.PK}} WHERE {o." + OrderModel.CODE + "} = ?" + QUERY_PARAM_ORDER_CODE +
            " AND {op." + OrderProcessModel.PROCESSDEFINITIONNAME + "} = ?" + QUERY_PARAM_ORDER_PROCESS_NAME + " AND {o." + OrderModel.ORIGINALVERSION + "} IS NULL";

    private static final String FIND_WAITING_RETURN_PROCESSES_QUERY = "SELECT {pk} FROM {"
            + ReturnProcessModel._TYPECODE + "} WHERE {" + ReturnProcessModel.REFUNDACTIONID + "} = ?" + QUERY_PARAM_REFUND_ACTION_ID;

    private static final String FIND_WAITING_VOID_PROCESSES_QUERY = "SELECT {vp.PK} FROM {"
            + CheckoutComVoidProcessModel._TYPECODE + " AS vp JOIN " + OrderModel._TYPECODE + " as o ON {vp." + CheckoutComVoidProcessModel.ORDER + "} = {o.PK}} WHERE {o." + OrderModel.CODE + "} = ?"
            + QUERY_PARAM_ORDER_CODE + " AND {o." + OrderModel.ORIGINALVERSION + "} IS NULL";

    protected final FlexibleSearchService flexibleSearchService;

    public DefaultCheckoutComProcessDefinitionDao(final FlexibleSearchService flexibleSearchService) {
        this.flexibleSearchService = flexibleSearchService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<BusinessProcessModel> findWaitingOrderProcesses(final String orderCode, final String processDefinitionName) {
        validateParameterNotNull(orderCode, "Order code must not be null");
        validateParameterNotNull(processDefinitionName, "Order process definition name must not be null");

        final FlexibleSearchQuery query = new FlexibleSearchQuery(FIND_WAITING_ORDER_PROCESSES_QUERY);
        query.setResultClassList(Collections.singletonList(BusinessProcessModel.class));
        query.addQueryParameter(QUERY_PARAM_ORDER_CODE, orderCode);
        query.addQueryParameter(QUERY_PARAM_ORDER_PROCESS_NAME, processDefinitionName);
        final SearchResult<BusinessProcessModel> searchResult = flexibleSearchService.search(query);

        return searchResult.getResult();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<BusinessProcessModel> findWaitingReturnProcesses(final String refundActionId) {
        validateParameterNotNull(refundActionId, "refundActionId must not be null");

        final FlexibleSearchQuery query = new FlexibleSearchQuery(FIND_WAITING_RETURN_PROCESSES_QUERY);
        query.setResultClassList(Collections.singletonList(BusinessProcessModel.class));
        query.addQueryParameter(QUERY_PARAM_REFUND_ACTION_ID, refundActionId);

        final SearchResult<BusinessProcessModel> searchResult = flexibleSearchService.search(query);

        return searchResult.getResult();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<BusinessProcessModel> findWaitingVoidProcesses(final String orderCode) {
        validateParameterNotNull(orderCode, "Order code must not be null");

        final FlexibleSearchQuery query = new FlexibleSearchQuery(FIND_WAITING_VOID_PROCESSES_QUERY);
        query.setResultClassList(Collections.singletonList(BusinessProcessModel.class));
        query.addQueryParameter(QUERY_PARAM_ORDER_CODE, orderCode);
        final SearchResult<BusinessProcessModel> searchResult = flexibleSearchService.search(query);

        return searchResult.getResult();
    }
}