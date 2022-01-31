package com.checkout.hybris.occ.converters.populators;

import com.checkout.hybris.facades.beans.APMPaymentInfoData;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commercewebservicescommons.dto.order.PaymentDetailsWsDTO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComPaymentDetailsDTOToPaymentInfoDataPopulatorTest {

    private static final String FAWRY = "fawry";

    @InjectMocks
    private CheckoutComPaymentDetailsDTOToPaymentInfoDataPopulator testObj;

    private PaymentDetailsWsDTO paymentTokenForm;
    private APMPaymentInfoData apmPaymentInfoData;

    @Before
    public void setUp() {
        paymentTokenForm = new PaymentDetailsWsDTO();
        apmPaymentInfoData = new APMPaymentInfoData();

        paymentTokenForm.setType(FAWRY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void populate_WhenNullSource_ShouldThrowException() {
        testObj.populate(null, apmPaymentInfoData);
    }

    @Test(expected = IllegalArgumentException.class)
    public void populate_WhenNullTarget_ShouldThrowException() {
        testObj.populate(paymentTokenForm, null);
    }

    @Test
    public void populate_ShouldPopulateEverythingCorrectly() {
        testObj.populate(paymentTokenForm, apmPaymentInfoData);

        assertEquals(FAWRY, apmPaymentInfoData.getType());
    }
}
