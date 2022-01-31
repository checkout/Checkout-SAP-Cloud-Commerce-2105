package com.checkout.hybris.facades.payment.info.populators.impl;

import com.checkout.hybris.core.model.CheckoutComAPMPaymentInfoModel;
import com.checkout.hybris.core.model.CheckoutComBenefitPayPaymentInfoModel;
import com.checkout.hybris.core.model.CheckoutComKlarnaAPMPaymentInfoModel;
import com.checkout.hybris.facades.beans.CheckoutComPaymentInfoData;
import com.checkout.hybris.facades.payment.info.mappers.CheckoutComApmPaymentInfoPopulatorMapper;
import de.hybris.bootstrap.annotations.UnitTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.BENEFITPAY;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComBenefitPayPaymentInfoPopulatorTest {

    private static final String QR_CODE = "qrCode";

    @Spy
    @InjectMocks
    private CheckoutComBenefitPayPaymentInfoPopulator testObj;

    @Mock
    private CheckoutComApmPaymentInfoPopulatorMapper checkoutComApmPaymentInfoPopulatorMapperMock;
    @Mock
    private CheckoutComKlarnaAPMPaymentInfoModel klarnaPaymentInfoMock;
    @Mock
    private CheckoutComBenefitPayPaymentInfoModel sourceMock;

    private CheckoutComPaymentInfoData target = new CheckoutComPaymentInfoData();

    @Before
    public void setUp() {
        doNothing().when(testObj).callSuperPopulate(any(CheckoutComAPMPaymentInfoModel.class), any(CheckoutComPaymentInfoData.class));
        when(sourceMock.getQrCode()).thenReturn(QR_CODE);
    }

    @Test
    public void registerPopulator_ShouldAddThePopulator() {
        testObj.registerPopulator();

        verify(checkoutComApmPaymentInfoPopulatorMapperMock).addPopulator(BENEFITPAY, testObj);
    }

    @Test
    public void getPopulatorKey_ShouldReturnBenefitPayType() {
        assertEquals(BENEFITPAY, testObj.getPopulatorKey());
    }

    @Test
    public void populate_ShouldPopulateCorrectly() {
        testObj.populate(sourceMock, target);

        assertEquals(QR_CODE, target.getQrCodeData());
    }

    @Test(expected = IllegalArgumentException.class)
    public void populate_WhenNotBenefitPayment_ShouldThrowException() {
        testObj.populate(klarnaPaymentInfoMock, target);
    }
}