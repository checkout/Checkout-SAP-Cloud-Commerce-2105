package com.checkout.hybris.core.order.interceptors;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Map;

import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComPaymentReferenceValidateInterceptorTest {

    @InjectMocks
    private CheckoutComPaymentReferenceValidateInterceptor testObj;

    @Mock
    private CartModel cartModelMock;
    @Mock
    private InterceptorContext contextMock;

    @Test(expected = InterceptorException.class)
    public void onValidate_PaymentReferenceEmptyAndAbstractOrderNew_ShouldThrowInterceptorException() throws InterceptorException {
        when(contextMock.isNew(cartModelMock)).thenReturn(true);
        when(contextMock.isModified(cartModelMock)).thenReturn(false);
        when(cartModelMock.getCheckoutComPaymentReference()).thenReturn(" ");

        testObj.onValidate(cartModelMock, contextMock);
    }

    @Test(expected = InterceptorException.class)
    public void onValidate_PaymentReferenceNullAndAbstractOrderNew_ShouldThrowInterceptorException() throws InterceptorException {
        when(contextMock.isNew(cartModelMock)).thenReturn(true);
        when(contextMock.isModified(cartModelMock)).thenReturn(false);
        when(cartModelMock.getCheckoutComPaymentReference()).thenReturn(null);

        testObj.onValidate(cartModelMock, contextMock);
    }

    @Test(expected = InterceptorException.class)
    public void onValidate_PaymentReferenceEmptyAndAbstractOrderModified_ShouldThrowInterceptorException() throws InterceptorException {
        when(contextMock.isNew(cartModelMock)).thenReturn(false);
        when(contextMock.isModified(cartModelMock)).thenReturn(true);
        when(contextMock.getDirtyAttributes(cartModelMock)).thenReturn(Map.of(AbstractOrderModel.CHECKOUTCOMPAYMENTREFERENCE, Collections.emptySet()));
        when(cartModelMock.getCheckoutComPaymentReference()).thenReturn(" ");

        testObj.onValidate(cartModelMock, contextMock);
    }

    @Test(expected = InterceptorException.class)
    public void onValidate_PaymentReferenceNullAndAbstractOrderModified_ShouldThrowInterceptorException() throws InterceptorException {
        when(contextMock.isNew(cartModelMock)).thenReturn(false);
        when(contextMock.isModified(cartModelMock)).thenReturn(true);
        when(contextMock.getDirtyAttributes(cartModelMock)).thenReturn(Map.of(AbstractOrderModel.CHECKOUTCOMPAYMENTREFERENCE, Collections.emptySet()));
        when(cartModelMock.getCheckoutComPaymentReference()).thenReturn(null);

        testObj.onValidate(cartModelMock, contextMock);
    }

    @Test
    public void onValidate_PaymentReferenceNotEmptyOrNullAndAbstractOrderNew_ShouldNotFail() throws InterceptorException {
        when(contextMock.isNew(cartModelMock)).thenReturn(true);
        when(contextMock.isModified(cartModelMock)).thenReturn(false);
        when(cartModelMock.getCheckoutComPaymentReference()).thenReturn("paymentReference");

        testObj.onValidate(cartModelMock, contextMock);

        verify(cartModelMock).getCheckoutComPaymentReference();
    }

    @Test
    public void onValidate_PaymentReferenceNotEmptyOrNullAndAbstractOrderModified_ShouldNotFail() throws InterceptorException {
        when(contextMock.isNew(cartModelMock)).thenReturn(false);
        when(contextMock.isModified(cartModelMock)).thenReturn(true);
        when(contextMock.getDirtyAttributes(cartModelMock)).thenReturn(Map.of(AbstractOrderModel.CHECKOUTCOMPAYMENTREFERENCE, Collections.emptySet()));
        when(cartModelMock.getCheckoutComPaymentReference()).thenReturn("paymentReference");

        testObj.onValidate(cartModelMock, contextMock);

        verify(cartModelMock).getCheckoutComPaymentReference();
    }

    @Test
    public void onValidate_PaymentReferenceNotSetAndAbstractOrderModified_ShouldDoNothing() throws InterceptorException {
        when(contextMock.isNew(cartModelMock)).thenReturn(false);
        when(contextMock.isModified(cartModelMock)).thenReturn(true);
        when(contextMock.getDirtyAttributes(cartModelMock)).thenReturn(Map.of("someOtherPropertyKey", Collections.emptySet()));

        testObj.onValidate(cartModelMock, contextMock);

        verifyZeroInteractions(cartModelMock);
    }
}