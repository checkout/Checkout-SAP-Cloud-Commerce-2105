package com.checkout.hybris.core.payment.response.mappers;

import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.core.payment.response.strategies.CheckoutComPaymentResponseStrategy;
import de.hybris.bootstrap.annotations.UnitTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.assertEquals;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComPaymentResponseStrategyMapperTest {

    @InjectMocks
    private CheckoutComPaymentResponseStrategyMapper testObj;

    @Mock
    private CheckoutComPaymentResponseStrategy checkoutComPaymentResponseStrategyMock;
    @Mock
    private CheckoutComPaymentResponseStrategy defaultCheckoutComPaymentResponseStrategyMock;

    @Before
    public void setUp() {
        ReflectionTestUtils.setField(testObj, "defaultCheckoutComPaymentResponseStrategy", defaultCheckoutComPaymentResponseStrategyMock);
    }

    @Test
    public void findStrategy_WhenThereAreNotStrategiesRegistered_ShouldTheDefaultStrategy() {
        final CheckoutComPaymentResponseStrategy result = testObj.findStrategy(CheckoutComPaymentType.EPS);

        assertEquals(defaultCheckoutComPaymentResponseStrategyMock, result);
    }

    @Test
    public void findStrategy_WhenStrategyKeyNotFound_ShouldTheDefaultStrategy() {
        testObj.addStrategy(CheckoutComPaymentType.CARD, checkoutComPaymentResponseStrategyMock);

        final CheckoutComPaymentResponseStrategy result = testObj.findStrategy(CheckoutComPaymentType.EPS);

        assertEquals(defaultCheckoutComPaymentResponseStrategyMock, result);
    }

    @Test
    public void findStrategy_WhenStrategyKeyHasBeenFound_ShouldReturnTheStrategy() {
        testObj.addStrategy(CheckoutComPaymentType.CARD, checkoutComPaymentResponseStrategyMock);

        final CheckoutComPaymentResponseStrategy result = testObj.findStrategy(CheckoutComPaymentType.CARD);

        assertEquals(checkoutComPaymentResponseStrategyMock, result);
    }
}
