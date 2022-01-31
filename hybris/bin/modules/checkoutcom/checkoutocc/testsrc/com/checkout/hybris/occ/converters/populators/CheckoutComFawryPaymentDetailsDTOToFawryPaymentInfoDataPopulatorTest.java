package com.checkout.hybris.occ.converters.populators;

import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.facades.beans.FawryPaymentInfoData;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commercewebservicescommons.dto.order.PaymentDetailsWsDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComFawryPaymentDetailsDTOToFawryPaymentInfoDataPopulatorTest {

    private static final String MOBILE_NUMBER_VALUE = "12345678912";

    @InjectMocks
    private CheckoutComFawryPaymentDetailsDTOToFawryPaymentInfoDataPopulator testObj;

    private PaymentDetailsWsDTO source = new PaymentDetailsWsDTO();
    private FawryPaymentInfoData target = new FawryPaymentInfoData();

    @Test
    public void populate_ShouldPopulateTargetCorrectly() {
        source.setMobileNumber(MOBILE_NUMBER_VALUE);

        testObj.populate(source, target);

        assertEquals(CheckoutComPaymentType.FAWRY.name(), target.getType());
        assertEquals(MOBILE_NUMBER_VALUE, target.getMobileNumber());
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
