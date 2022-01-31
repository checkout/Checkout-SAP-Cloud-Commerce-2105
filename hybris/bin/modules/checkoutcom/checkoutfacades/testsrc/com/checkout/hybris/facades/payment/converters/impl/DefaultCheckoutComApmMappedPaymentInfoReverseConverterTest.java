package com.checkout.hybris.facades.payment.converters.impl;

import com.checkout.hybris.core.model.CheckoutComAPMPaymentInfoModel;
import com.checkout.hybris.core.model.CheckoutComBenefitPayPaymentInfoModel;
import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.facades.beans.APMPaymentInfoData;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultCheckoutComApmMappedPaymentInfoReverseConverterTest {

    @InjectMocks
    private DefaultCheckoutComApmMappedPaymentInfoReverseConverter testObj;

    @Mock
    private Converter<APMPaymentInfoData, CheckoutComAPMPaymentInfoModel> defaultConverterMock;
    @Mock
    private Map<CheckoutComPaymentType, Converter<APMPaymentInfoData, CheckoutComAPMPaymentInfoModel>> convertersMapMock;
    @Mock
    private Converter<APMPaymentInfoData, CheckoutComAPMPaymentInfoModel> benefitPayConverterMock;
    @Mock
    private APMPaymentInfoData apmPaymentInfoDataMock;
    @Mock
    private CheckoutComBenefitPayPaymentInfoModel benefitPayPaymentInfoMock;
    @Mock
    private CheckoutComAPMPaymentInfoModel apmPaymentInfoModelMock;

    @Before
    public void setUp() {
        when(convertersMapMock.containsKey(CheckoutComPaymentType.BENEFITPAY)).thenReturn(true);
        when(convertersMapMock.containsKey(CheckoutComPaymentType.GIROPAY)).thenReturn(false);
        when(convertersMapMock.get(CheckoutComPaymentType.BENEFITPAY)).thenReturn(benefitPayConverterMock);
        ReflectionTestUtils.setField(testObj, "defaultConverter", defaultConverterMock);
        when(benefitPayConverterMock.convert(apmPaymentInfoDataMock)).thenReturn(benefitPayPaymentInfoMock);
        when(defaultConverterMock.convert(apmPaymentInfoDataMock)).thenReturn(apmPaymentInfoModelMock);
    }

    @Test
    public void convertAPMPaymentInfoData_WhenSpecificConverterNotFound_ShouldInvokeTheDefaultConverter() {
        final CheckoutComAPMPaymentInfoModel result = testObj.convertAPMPaymentInfoData(apmPaymentInfoDataMock, CheckoutComPaymentType.BENEFITPAY);

        verify(benefitPayConverterMock).convert(apmPaymentInfoDataMock);
        assertEquals(benefitPayPaymentInfoMock, result);
    }

    @Test
    public void convertAPMPaymentInfoData_WhenSpecificConverterFound_ShouldInvokeTheSpecificConverter() {
        final CheckoutComAPMPaymentInfoModel result = testObj.convertAPMPaymentInfoData(apmPaymentInfoDataMock, CheckoutComPaymentType.GIROPAY);

        verify(defaultConverterMock).convert(apmPaymentInfoDataMock);
        assertEquals(apmPaymentInfoModelMock, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void convertAPMPaymentInfoData_WhenApmPaymentInfoDataNull_ShouldThrowException() {
        testObj.convertAPMPaymentInfoData(null, CheckoutComPaymentType.BENEFITPAY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void convertAPMPaymentInfoData_WhenPaymentTypeNull_ShouldThrowException() {
        testObj.convertAPMPaymentInfoData(apmPaymentInfoDataMock, null);
    }
}