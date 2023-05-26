package com.checkout.hybris.fulfilmentprocess.actions.order;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.orderprocessing.events.OrderCompletedEvent;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.servicelayer.event.EventService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class SendOrderCompletedNotificationActionTest {

    @InjectMocks
    private SendOrderCompletedNotificationAction testObj;

    @Mock
    private EventService eventServiceMock;

    @Captor
    private ArgumentCaptor<OrderCompletedEvent> orderCompletedEventCaptor;

    private OrderModel orderModel;
    private OrderProcessModel orderProcessModel;


    @Test
    public void executeAction_shouldTriggerOrderCompletedEventWhenStatusOfOrderDifferentThanPaymentReturned() {
        orderModel = new OrderModel();
        orderModel.setStatus(OrderStatus.ORDER_SPLIT);
        orderProcessModel = new OrderProcessModel();
        orderProcessModel.setOrder(orderModel);

        testObj.executeAction(orderProcessModel);

        verify(eventServiceMock).publishEvent(orderCompletedEventCaptor.capture());
        final OrderCompletedEvent orderCompletedEvent = orderCompletedEventCaptor.getValue();
        assertThat(orderCompletedEvent.getProcess()).isEqualTo(orderProcessModel);
        assertThat(orderCompletedEvent.getProcess().getOrder()).isEqualTo(orderModel);
    }

    @Test
    public void executeAction_shouldNotTriggerOrderCompletedEventWhenStatusOfOrderEqualPaymentReturned() {
        orderModel = new OrderModel();
        orderModel.setStatus(OrderStatus.PAYMENT_RETURNED);
        orderProcessModel = new OrderProcessModel();
        orderProcessModel.setOrder(orderModel);

        testObj.executeAction(orderProcessModel);

        verifyZeroInteractions(eventServiceMock);
    }
}
