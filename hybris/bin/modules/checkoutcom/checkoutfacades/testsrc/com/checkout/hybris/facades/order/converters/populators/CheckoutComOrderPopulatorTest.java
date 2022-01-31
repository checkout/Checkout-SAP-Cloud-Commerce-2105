package com.checkout.hybris.facades.order.converters.populators;

import com.checkout.hybris.core.model.CheckoutComAPMPaymentInfoModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commercefacades.order.data.CCPaymentInfoData;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComOrderPopulatorTest {

    @InjectMocks
    private CheckoutComOrderPopulator testObj;

    @Mock
    private OrderModel sourceMock;
    @Mock
    private Converter<AddressModel, AddressData> addressConverterMock;
    @Mock
    private AddressModel addressModelMock;
    @Mock
    private AddressData addressDataMock;
    @Mock
    private CheckoutComAPMPaymentInfoModel checkoutComRedirectApmPaymentInfoMock;
    @Mock
    private OrderData targetMock;
    @Mock
    private CCPaymentInfoData paymentInfoMock;
    @Captor
    private ArgumentCaptor<CCPaymentInfoData> ccPaymentInfoDataArgumentCaptor;

    @Before
    public void setUp() {
        when(addressConverterMock.convert(addressModelMock)).thenReturn(addressDataMock);
        when(targetMock.getPaymentInfo()).thenReturn(paymentInfoMock);
        when(sourceMock.getPaymentInfo()).thenReturn(checkoutComRedirectApmPaymentInfoMock);
        when(checkoutComRedirectApmPaymentInfoMock.getBillingAddress()).thenReturn(addressModelMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void populate_WithNullSource_ShouldThrowException() {
        testObj.populate(null, targetMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void populate_WithNullTarget_ShouldThrowException() {
        testObj.populate(sourceMock, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void populate_WithNullSourcePaymentInfo_ShouldThrowException() {
        when(sourceMock.getPaymentInfo()).thenReturn(null);

        testObj.populate(sourceMock, targetMock);
    }

    @Test
    public void populate_WhenTargetPaymentInfoAlreadyPopulated_ShouldDoNothing() {
        testObj.populate(sourceMock, targetMock);

        verifyZeroInteractions(addressConverterMock);
    }

    @Test
    public void populate_WhenTargetPaymentInfoIsNull_ShouldPopulateTheAddressData() {
        when(targetMock.getPaymentInfo()).thenReturn(null);

        testObj.populate(sourceMock, targetMock);

        verify(addressConverterMock).convert(addressModelMock);
        verify(targetMock).setPaymentInfo(ccPaymentInfoDataArgumentCaptor.capture());
        assertEquals(addressDataMock, ccPaymentInfoDataArgumentCaptor.getValue().getBillingAddress());
    }
}
