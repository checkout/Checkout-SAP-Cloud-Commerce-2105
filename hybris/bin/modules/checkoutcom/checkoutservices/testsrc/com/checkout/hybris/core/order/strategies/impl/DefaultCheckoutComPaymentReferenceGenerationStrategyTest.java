package com.checkout.hybris.core.order.strategies.impl;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.servicelayer.time.TimeService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultCheckoutComPaymentReferenceGenerationStrategyTest {

    private static final String ORDER_CODE = "orderCode";

    @InjectMocks
    private DefaultCheckoutComPaymentReferenceGenerationStrategy testObj;

    @Mock
    private TimeService timeServiceMock;
    @Mock
    private AbstractOrderModel abstractOrderMock;

    @Test
    public void generatePaymentReference_ShouldReturnOrderCodeAndTimestamp() {
        Date currentTime = new Date();

        when(abstractOrderMock.getCode()).thenReturn(ORDER_CODE);
        when(timeServiceMock.getCurrentTime()).thenReturn(currentTime);

        final String result = testObj.generatePaymentReference(abstractOrderMock);

        assertEquals(ORDER_CODE + "-" + currentTime.getTime(), result);
    }
}
