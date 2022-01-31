package com.checkout.hybris.core.order.interceptors;

import com.checkout.hybris.core.order.strategies.CheckoutComPaymentReferenceGenerationStrategy;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComPaymentReferencePrepareInterceptorTest {

    private static final String PAYMENT_REFERENCE = "paymentReference";

    @InjectMocks
    private CheckoutComPaymentReferencePrepareInterceptor testObj;

    @Mock
    private AbstractOrderModel abstractOrderMock;
    @Mock
    private InterceptorContext contextMock;
    @Mock
    private CheckoutComPaymentReferenceGenerationStrategy paymentReferenceGenerationStrategyMock;

    @Before
    public void setUp() {
        when(paymentReferenceGenerationStrategyMock.generatePaymentReference(abstractOrderMock)).thenReturn(PAYMENT_REFERENCE);
    }

    @Test
    public void onPrepare_WhenAbstractOrderNew_ShouldGenerateAndPopulatePaymentReference() throws InterceptorException {
        when(contextMock.isNew(abstractOrderMock)).thenReturn(true);

        testObj.onPrepare(abstractOrderMock, contextMock);

        verify(abstractOrderMock).setCheckoutComPaymentReference(PAYMENT_REFERENCE);
    }

    @Test
    public void onPrepare_WhenAbstractOrderModified_ShouldDoNothing() throws InterceptorException {
        when(contextMock.isNew(abstractOrderMock)).thenReturn(false);

        testObj.onPrepare(abstractOrderMock, contextMock);

        verifyNoMoreInteractions(abstractOrderMock);
    }
}