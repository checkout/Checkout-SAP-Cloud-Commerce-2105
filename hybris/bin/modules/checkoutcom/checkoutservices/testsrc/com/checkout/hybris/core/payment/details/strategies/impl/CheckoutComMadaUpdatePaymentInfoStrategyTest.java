package com.checkout.hybris.core.payment.details.strategies.impl;

import de.hybris.bootstrap.annotations.UnitTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.MADA;
import static org.junit.Assert.assertEquals;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComMadaUpdatePaymentInfoStrategyTest {

    @InjectMocks
    private CheckoutComMadaUpdatePaymentInfoStrategy testObj;

    @Test
    public void getStrategyKey_WhenStandardMada_ShouldReturnMadaType() {
        assertEquals(MADA, testObj.getStrategyKey());
    }
}