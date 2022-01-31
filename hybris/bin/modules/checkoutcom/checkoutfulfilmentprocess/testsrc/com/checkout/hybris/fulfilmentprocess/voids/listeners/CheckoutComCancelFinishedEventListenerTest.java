package com.checkout.hybris.fulfilmentprocess.voids.listeners;

import com.checkout.hybris.fulfilmentprocess.model.CheckoutComVoidProcessModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.ordercancel.events.CancelFinishedEvent;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.model.ModelService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComCancelFinishedEventListenerTest {

    private static final String CHECKOUT_COM_VOID_PROCESS_NAME = "void-process";
    private static final String ORDER_CODE = "orderCode";

    @Spy
    @InjectMocks
    private CheckoutComCancelFinishedEventListener testObj;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private CancelFinishedEvent cancelFinishedEventMock;
    @Mock
    private BusinessProcessService businessProcessServiceMock;
    @Mock
    private OrderModel orderModelMock;
    @Mock
    private CheckoutComVoidProcessModel checkoutComVoidProcessModelMock;
    @Mock
    private ModelService modelServiceMock;

    @Test
    public void onEvent_ShouldCreateNewVoidProcess() {
        when(orderModelMock.getCode()).thenReturn(ORDER_CODE);
        when(cancelFinishedEventMock.getCancelRequestRecordEntry().getModificationRecord().getOrder()).thenReturn(orderModelMock);
        when(businessProcessServiceMock.createProcess(anyString(), eq(CHECKOUT_COM_VOID_PROCESS_NAME))).thenReturn(checkoutComVoidProcessModelMock);

        testObj.onEvent(cancelFinishedEventMock);

        verify(businessProcessServiceMock).createProcess(anyString(), eq(CHECKOUT_COM_VOID_PROCESS_NAME));
        verify(checkoutComVoidProcessModelMock).setOrder(orderModelMock);
        verify(modelServiceMock).save(checkoutComVoidProcessModelMock);
        verify(businessProcessServiceMock).startProcess(checkoutComVoidProcessModelMock);
    }
}
