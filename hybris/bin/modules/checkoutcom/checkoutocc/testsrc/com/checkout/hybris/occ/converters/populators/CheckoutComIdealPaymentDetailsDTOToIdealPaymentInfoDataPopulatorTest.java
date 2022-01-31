package com.checkout.hybris.occ.converters.populators;

import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.facades.beans.IdealPaymentInfoData;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commercewebservicescommons.dto.order.PaymentDetailsWsDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComIdealPaymentDetailsDTOToIdealPaymentInfoDataPopulatorTest {

    @InjectMocks
    private CheckoutComIdealPaymentDetailsDTOToIdealPaymentInfoDataPopulator testObj;

    private PaymentDetailsWsDTO source = new PaymentDetailsWsDTO();
    private IdealPaymentInfoData target = new IdealPaymentInfoData();

    @Test
    public void populate_ShouldPopulateTargetCorrectly() {
        source.setBic("68453120000");

        testObj.populate(source, target);

        assertEquals(CheckoutComPaymentType.IDEAL.name(), target.getType());
        assertEquals("68453120000", target.getBic());
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
