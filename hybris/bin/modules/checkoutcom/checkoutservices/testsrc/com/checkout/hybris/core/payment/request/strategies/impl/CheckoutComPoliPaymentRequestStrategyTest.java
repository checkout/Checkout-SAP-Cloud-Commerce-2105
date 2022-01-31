package com.checkout.hybris.core.payment.request.strategies.impl;

import de.hybris.bootstrap.annotations.UnitTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.POLI;
import static org.junit.Assert.assertEquals;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComPoliPaymentRequestStrategyTest {

    @InjectMocks
    private CheckoutComPoliPaymentRequestStrategy testObj;

    @Test
    public void getStrategyKey_WhenBenefitPay_ShouldReturnPoliType() {
        assertEquals(POLI, testObj.getStrategyKey());
    }
}
