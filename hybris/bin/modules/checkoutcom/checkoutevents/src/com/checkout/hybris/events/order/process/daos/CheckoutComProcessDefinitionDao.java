package com.checkout.hybris.events.order.process.daos;

import de.hybris.platform.processengine.model.BusinessProcessModel;

import java.util.List;

/**
 * Process Definition DAO interface responsible for retrieving business processes.
 */
public interface CheckoutComProcessDefinitionDao {

    /**
     * Finds waiting order processes for the given order code and process definition name
     *
     * @param orderCode             the order code
     * @param processDefinitionName the defined process name
     * @return list of {@link BusinessProcessModel}
     */
    List<BusinessProcessModel> findWaitingOrderProcesses(String orderCode, String processDefinitionName);

    /**
     * Finds waiting refund processes for the refund action id
     *
     * @param refundActionId the order code
     * @return list of {@link BusinessProcessModel}
     */
    List<BusinessProcessModel> findWaitingReturnProcesses(String refundActionId);

    /**
     * Finds waiting void processes for the order code
     *
     * @param orderCode the order code
     * @return list of {@link BusinessProcessModel}
     */
    List<BusinessProcessModel> findWaitingVoidProcesses(String orderCode);
}
