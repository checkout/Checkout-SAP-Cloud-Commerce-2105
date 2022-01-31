package com.checkout.hybris.core.payment.details.mappers;

import com.checkout.hybris.core.payment.details.strategies.CheckoutComUpdatePaymentInfoStrategy;
import com.checkout.hybris.core.payment.details.strategies.impl.CheckoutComCardUpdatePaymentInfoStrategy;
import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
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
public class CheckoutComUpdatePaymentInfoStrategyMapperTest {

    @InjectMocks
    private CheckoutComUpdatePaymentInfoStrategyMapper testObj;

    @Mock
    private CheckoutComCardUpdatePaymentInfoStrategy checkoutComCardPaymentInfoStrategyMock;
    @Mock
    private CheckoutComUpdatePaymentInfoStrategy defaultCheckoutComPaymentInfoStrategyMock;

    @Before
    public void setUp() {
        ReflectionTestUtils.setField(testObj, "defaultCheckoutComPaymentInfoStrategy", defaultCheckoutComPaymentInfoStrategyMock);
    }

    @Test
    public void findStrategy_WhenThereAreNotStrategiesRegistered_ShouldReturnTheDefaultStrategy() {
        final CheckoutComUpdatePaymentInfoStrategy result = testObj.findStrategy(CheckoutComPaymentType.EPS);

        assertEquals(defaultCheckoutComPaymentInfoStrategyMock, result);
    }

    @Test
    public void findStrategy_WhenStrategyKeyNotFound_ShouldReturnTheDefaultStrategy() {
        testObj.addStrategy(CheckoutComPaymentType.CARD, checkoutComCardPaymentInfoStrategyMock);

        final CheckoutComUpdatePaymentInfoStrategy result = testObj.findStrategy(CheckoutComPaymentType.EPS);

        assertEquals(defaultCheckoutComPaymentInfoStrategyMock, result);
    }

    @Test
    public void findStrategy_WhenStrategyKeyHasBeenFound_ShouldReturnTheStrategy() {
        testObj.addStrategy(CheckoutComPaymentType.CARD, checkoutComCardPaymentInfoStrategyMock);

        final CheckoutComUpdatePaymentInfoStrategy result = testObj.findStrategy(CheckoutComPaymentType.CARD);

        assertEquals(checkoutComCardPaymentInfoStrategyMock, result);
    }
}
