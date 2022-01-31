package com.checkout.hybris.core.payment.request.strategies.impl;

import de.hybris.bootstrap.annotations.UnitTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.ALIPAY;
import static org.junit.Assert.assertEquals;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComAlipayPaymentRequestStrategyTest {

    @InjectMocks
    private CheckoutComAlipayPaymentRequestStrategy testObj;

    @Test
    public void getStrategyKey_WhenBenefitPay_ShouldReturnAlipayType() {
        assertEquals(ALIPAY, testObj.getStrategyKey());
    }
}
