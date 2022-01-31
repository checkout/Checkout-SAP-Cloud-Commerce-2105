package com.checkout.hybris.fulfilmentprocess.voids.listeners;

import com.checkout.hybris.fulfilmentprocess.model.CheckoutComVoidProcessModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.ordercancel.events.CancelFinishedEvent;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.event.impl.AbstractEventListener;
import de.hybris.platform.servicelayer.model.ModelService;

/**
 * Event listener to handle a CancelFinished event
 */
public class CheckoutComCancelFinishedEventListener extends AbstractEventListener<CancelFinishedEvent> {

    protected static final String CHECKOUT_COM_VOID_PROCESS_NAME = "void-process";
    protected static final String SEPARATOR = "-";

    protected final BusinessProcessService businessProcessService;
    protected final ModelService modelService;

    public CheckoutComCancelFinishedEventListener(final BusinessProcessService businessProcessService,
                                                  final ModelService modelService) {
        this.businessProcessService = businessProcessService;
        this.modelService = modelService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onEvent(final CancelFinishedEvent cancelFinishedEvent) {
        final OrderModel order = cancelFinishedEvent.getCancelRequestRecordEntry().getModificationRecord().getOrder();
        final String businessProcessId = CHECKOUT_COM_VOID_PROCESS_NAME + SEPARATOR + order.getCode() + SEPARATOR + System.currentTimeMillis();
        final CheckoutComVoidProcessModel voidOrderProcess = businessProcessService.createProcess(businessProcessId, CHECKOUT_COM_VOID_PROCESS_NAME);
        voidOrderProcess.setOrder(order);
        modelService.save(voidOrderProcess);
        businessProcessService.startProcess(voidOrderProcess);
    }
}
