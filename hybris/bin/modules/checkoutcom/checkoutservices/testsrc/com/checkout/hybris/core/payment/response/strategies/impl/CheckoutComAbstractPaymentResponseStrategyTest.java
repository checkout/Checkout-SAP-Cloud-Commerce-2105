package com.checkout.hybris.core.payment.response.strategies.impl;

import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.core.payment.response.mappers.CheckoutComPaymentResponseStrategyMapper;
import com.checkout.hybris.core.payment.response.strategies.CheckoutComPaymentResponseStrategy;
import de.hybris.bootstrap.annotations.UnitTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComAbstractPaymentResponseStrategyTest {

    @Spy
    private CheckoutComAbstractPaymentResponseStrategy testObj;

    @Mock
    private CheckoutComPaymentResponseStrategyMapper checkoutComPaymentResponseStrategyMapperMock;

    @Before
    public void setUp() {
        testObj = Mockito.mock(
                CheckoutComAbstractPaymentResponseStrategy.class,
                Mockito.CALLS_REAL_METHODS);
        Whitebox.setInternalState(testObj, "checkoutComPaymentResponseStrategyMapper",checkoutComPaymentResponseStrategyMapperMock);
    }

    @Test
    public void registerStrategy_ShouldRegisterTheStrategy() {
        testObj.registerStrategy();

        verify(checkoutComPaymentResponseStrategyMapperMock).addStrategy(any(CheckoutComPaymentType.class), any(CheckoutComPaymentResponseStrategy.class));
    }
}
