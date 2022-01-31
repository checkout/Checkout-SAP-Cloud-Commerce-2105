package com.checkout.hybris.occ.converters.impl;

import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commercewebservicescommons.dto.order.PaymentDetailsWsDTO;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.APPLEPAY;
import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.FAWRY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultCheckoutComPaymentDetailsDTOReverseConverterTest {

    @InjectMocks
    private DefaultCheckoutComPaymentDetailsDTOReverseConverter testObj;

    @Mock
    private Converter<PaymentDetailsWsDTO, Object> defaultConverterMock;
    @Mock
    private Converter<PaymentDetailsWsDTO, Object> fawryConverterMock;

    @Mock
    private Object convertedObjectMock;
    @Mock
    private PaymentDetailsWsDTO paymentDetailsWsDTOMock;

    private Map<CheckoutComPaymentType, Converter<PaymentDetailsWsDTO, Object>> converters = new HashMap<>();

    @Before
    public void setUp() {
        converters.put(FAWRY, fawryConverterMock);
        testObj = new DefaultCheckoutComPaymentDetailsDTOReverseConverter(converters, defaultConverterMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void convertPaymentDetailsWsDTO_WhenNullSource_ShouldThrowException() {
        testObj.convertPaymentDetailsWsDTO(null, CheckoutComPaymentType.FAWRY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void convertPaymentDetailsWsDTO_WhenNullTarget_ShouldThrowException() {
        testObj.convertPaymentDetailsWsDTO(paymentDetailsWsDTOMock, null);
    }

    @Test
    public void convertPaymentDetailsWsDTO_WhenConverterForGivenPaymentType_ShouldSpecificConverter() {
        when(fawryConverterMock.convert(paymentDetailsWsDTOMock)).thenReturn(convertedObjectMock);

        final Object result = testObj.convertPaymentDetailsWsDTO(paymentDetailsWsDTOMock, CheckoutComPaymentType.FAWRY);

        assertThat(result).isEqualTo(convertedObjectMock);
    }

    @Test
    public void convertPaymentDetailsWsDTO_WhenThereIsNoConverterForGivenPaymentType_ShouldUseDefaultConverter() {
        when(defaultConverterMock.convert(paymentDetailsWsDTOMock)).thenReturn(convertedObjectMock);

        final Object result = testObj.convertPaymentDetailsWsDTO(paymentDetailsWsDTOMock, APPLEPAY);

        assertThat(result).isEqualTo(convertedObjectMock);
    }
}
