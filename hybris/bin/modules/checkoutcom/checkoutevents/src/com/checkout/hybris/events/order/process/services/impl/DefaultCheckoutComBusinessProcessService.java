package com.checkout.hybris.events.order.process.services.impl;

import com.checkout.hybris.events.model.CheckoutComPaymentEventModel;
import com.checkout.hybris.events.order.process.daos.CheckoutComProcessDefinitionDao;
import com.checkout.hybris.events.order.process.services.CheckoutComBusinessProcessService;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.processengine.impl.DefaultBusinessProcessService;
import de.hybris.platform.processengine.model.BusinessProcessModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Default checkout.com implementation of {@link CheckoutComBusinessProcessService}
 */
public class DefaultCheckoutComBusinessProcessService extends DefaultBusinessProcessService implements CheckoutComBusinessProcessService {

    protected static final Logger LOG = LogManager.getLogger(DefaultCheckoutComBusinessProcessService.class);

    protected final CheckoutComProcessDefinitionDao checkoutComProcessDefinitionDao;

    public DefaultCheckoutComBusinessProcessService(final CheckoutComProcessDefinitionDao checkoutComProcessDefinitionDao) {
        this.checkoutComProcessDefinitionDao = checkoutComProcessDefinitionDao;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<BusinessProcessModel> findBusinessProcess(PaymentTransactionType paymentTransactionType, OrderModel orderModel, CheckoutComPaymentEventModel event) {
        switch (paymentTransactionType) {
            case REFUND_FOLLOW_ON:
                LOG.debug("Finding return process for event with id [{}] and order [{}]", event.getEventId(), orderModel.getCode());
                return checkoutComProcessDefinitionDao.findWaitingReturnProcesses(event.getActionId());
            case CANCEL:
                LOG.debug("Finding void process for event with id [{}] and order [{}]", event.getEventId(), orderModel.getCode());
                return checkoutComProcessDefinitionDao.findWaitingVoidProcesses(orderModel.getCode());
            default:
                LOG.debug("Finding order process for event with id [{}] and order [{}]", event.getEventId(), orderModel.getCode());
                return checkoutComProcessDefinitionDao.findWaitingOrderProcesses(orderModel.getCode(), orderModel.getStore().getSubmitOrderProcessCode());
        }
    }
}