package com.checkout.hybris.facades.payment.info.populators.impl;

import com.checkout.hybris.core.model.CheckoutComAPMPaymentInfoModel;
import com.checkout.hybris.facades.beans.CheckoutComPaymentInfoData;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComAbstractApmPaymentInfoPopulatorTest {

    private static final String PAYMENT_TYPE = "type";

    @InjectMocks
    private CheckoutComAbstractApmPaymentInfoPopulator tesObj;

    @Mock
    private CheckoutComAPMPaymentInfoModel sourceMock;
    @Mock
    private Converter<AddressModel, AddressData> addressConverterMock;
    @Mock
    private AddressModel addressModelMock;
    @Mock
    private AddressData addressDataMock;

    private CheckoutComPaymentInfoData target = new CheckoutComPaymentInfoData();

    @Test(expected = IllegalArgumentException.class)
    public void populate_WhenSourceIsNull_ShouldThrowException() {
        tesObj.populate(null, target);
    }

    @Test(expected = IllegalArgumentException.class)
    public void populate_WhenTargetIsNull_ShouldThrowException() {
        tesObj.populate(sourceMock, null);
    }

    @Test
    public void populate_WhenTargetAndSourceAreCorrect_ShouldPopulateTypeAndAddress() {
        when(sourceMock.getType()).thenReturn(PAYMENT_TYPE);
        when(sourceMock.getBillingAddress()).thenReturn(addressModelMock);
        when(addressConverterMock.convert(addressModelMock)).thenReturn(addressDataMock);

        tesObj.populate(sourceMock, target);

        assertEquals(PAYMENT_TYPE, target.getType());
        assertEquals(addressDataMock, target.getBillingAddress());
    }
}