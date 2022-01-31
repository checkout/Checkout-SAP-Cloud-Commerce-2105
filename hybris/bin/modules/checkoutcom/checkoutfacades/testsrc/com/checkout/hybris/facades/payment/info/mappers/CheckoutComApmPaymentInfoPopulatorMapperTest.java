package com.checkout.hybris.facades.payment.info.mappers;

import com.checkout.hybris.core.model.CheckoutComAPMPaymentInfoModel;
import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.facades.beans.CheckoutComPaymentInfoData;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.converters.Populator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComApmPaymentInfoPopulatorMapperTest {

    @InjectMocks
    private CheckoutComApmPaymentInfoPopulatorMapper testObj;

    @Mock
    private Populator<CheckoutComAPMPaymentInfoModel, CheckoutComPaymentInfoData> checkoutComApmPaymentInfoPopulatorMock;

    @Test
    public void findPopulator_WhenThereAreNotPopulatorsRegistered_ShouldReturnTheDefaultPopulator() {
        final Populator result = testObj.findPopulator(CheckoutComPaymentType.EPS);

        assertEquals(checkoutComApmPaymentInfoPopulatorMock, result);
    }

    @Test
    public void findPopulator_WhenPopulatorKeyNotFound_ShouldReturnTheDefaultPopulator() {
        testObj.addPopulator(CheckoutComPaymentType.CARD, checkoutComApmPaymentInfoPopulatorMock);

        final Populator result = testObj.findPopulator(CheckoutComPaymentType.EPS);

        assertEquals(checkoutComApmPaymentInfoPopulatorMock, result);
    }

    @Test
    public void findPopulator_WhenPopulatorKeyHasBeenFound_ShouldReturnThePopulator() {
        testObj.addPopulator(CheckoutComPaymentType.CARD, checkoutComApmPaymentInfoPopulatorMock);

        final Populator result = testObj.findPopulator(CheckoutComPaymentType.CARD);

        assertEquals(checkoutComApmPaymentInfoPopulatorMock, result);
    }
}
