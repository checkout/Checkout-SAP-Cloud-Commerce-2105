package com.checkout.hybris.facades.payment.converters.populators;

import com.checkout.hybris.core.merchant.services.CheckoutComMerchantConfigurationService;
import com.checkout.hybris.core.model.CheckoutComApplePayPaymentInfoModel;
import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.facades.beans.WalletPaymentInfoData;
import de.hybris.bootstrap.annotations.UnitTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComApplePayPaymentInfoReversePopulatorTest {

    private static final String PAYMENT_TOKEN_VALUE = "payment_token";

    @InjectMocks
    private CheckoutComApplePayPaymentInfoReversePopulator testObj;

    @Mock
    private CheckoutComMerchantConfigurationService checkoutComMerchantConfigurationServiceMock;

    private WalletPaymentInfoData source = new WalletPaymentInfoData();
    private CheckoutComApplePayPaymentInfoModel target = new CheckoutComApplePayPaymentInfoModel();

    @Test
    public void populate_ShouldPopulateTargetCorrectly() {
        when(checkoutComMerchantConfigurationServiceMock.isAutoCapture()).thenReturn(Boolean.TRUE);
        source.setType(CheckoutComPaymentType.APPLEPAY.name());
        source.setToken(PAYMENT_TOKEN_VALUE);

        testObj.populate(source, target);

        assertEquals(PAYMENT_TOKEN_VALUE, target.getToken());
        assertEquals(CheckoutComPaymentType.APPLEPAY.name(), target.getType());
        assertFalse(target.getUserDataRequired());
        assertTrue(target.getDeferred());
    }

    @Test(expected = IllegalArgumentException.class)
    public void populate_WhenSourceNull_ShouldThrowException() {
        testObj.populate(null, target);
    }

    @Test(expected = IllegalArgumentException.class)
    public void populate_WhenTargetNull_ShouldThrowException() {
        testObj.populate(source, null);
    }
}