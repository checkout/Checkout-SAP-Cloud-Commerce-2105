package com.checkout.hybris.occ.converters.populators;

import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.facades.beans.KlarnaPaymentInfoData;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commercewebservicescommons.dto.order.PaymentDetailsWsDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComKlarnaPaymentDetailsDTOToKlarnaPaymentInfoDataPopulatorTest {

    private static final String KLARNA_AUTH_TOKEN_VALUE = "12345678901_abdajkdjal";

    @InjectMocks
    private CheckoutComKlarnaPaymentDetailsDTOToKlarnaPaymentInfoDataPopulator testObj;

    private PaymentDetailsWsDTO source = new PaymentDetailsWsDTO();
    private KlarnaPaymentInfoData target = new KlarnaPaymentInfoData();

    @Test
    public void populate_ShouldPopulateTargetCorrectly() {
        source.setAuthorizationToken(KLARNA_AUTH_TOKEN_VALUE);

        testObj.populate(source, target);

        assertEquals(CheckoutComPaymentType.KLARNA.name(), target.getType());
        assertEquals(KLARNA_AUTH_TOKEN_VALUE, target.getAuthorizationToken());
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
