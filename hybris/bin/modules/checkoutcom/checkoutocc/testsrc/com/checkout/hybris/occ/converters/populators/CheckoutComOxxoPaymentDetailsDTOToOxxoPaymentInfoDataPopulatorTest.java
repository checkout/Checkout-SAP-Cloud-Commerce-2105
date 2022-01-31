package com.checkout.hybris.occ.converters.populators;

import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.facades.beans.OxxoPaymentInfoData;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commercewebservicescommons.dto.order.PaymentDetailsWsDTO;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

@UnitTest
public class CheckoutComOxxoPaymentDetailsDTOToOxxoPaymentInfoDataPopulatorTest {

    private static final String DOCUMENT = "111111111111111111";

    private CheckoutComOxxoPaymentDetailsDTOToOxxoPaymentInfoDataPopulator testObj = new CheckoutComOxxoPaymentDetailsDTOToOxxoPaymentInfoDataPopulator();

    private PaymentDetailsWsDTO source = new PaymentDetailsWsDTO();
    private OxxoPaymentInfoData target = new OxxoPaymentInfoData();

    @Test(expected = IllegalArgumentException.class)
    public void populate_WhenSourceNull_ShouldThrowException() {
        testObj.populate(null, target);
    }

    @Test(expected = IllegalArgumentException.class)
    public void populate_WhenTargetNull_ShouldThrowException() {
        testObj.populate(source, null);
    }

    @Test
    public void populate_ShouldPopulateTargetCorrectly() {
        source.setDocument(DOCUMENT);

        testObj.populate(source, target);

        assertEquals(CheckoutComPaymentType.OXXO.name(), target.getType());
        assertEquals(DOCUMENT, target.getDocument());
    }
}
