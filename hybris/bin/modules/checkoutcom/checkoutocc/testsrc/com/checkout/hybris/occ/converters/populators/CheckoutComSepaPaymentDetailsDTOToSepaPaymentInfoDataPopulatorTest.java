package com.checkout.hybris.occ.converters.populators;

import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.facades.beans.SepaPaymentInfoData;
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
public class CheckoutComSepaPaymentDetailsDTOToSepaPaymentInfoDataPopulatorTest {

    @InjectMocks
    private CheckoutComSepaPaymentDetailsDTOToSepaPaymentInfoDataPopulator testObj;

    private PaymentDetailsWsDTO source = new PaymentDetailsWsDTO();
    private SepaPaymentInfoData target = new SepaPaymentInfoData();

    @Before
    public void setUp() {
        source.setFirstName("John");
        source.setLastName("Snow");
        source.setPaymentType("Recurring payment");
        source.setAccountIban("GB56789123456");
        source.setAddressLine1("1 Buckingham Palace Road");
        source.setAddressLine2("Royal Palace");
        source.setCity("London");
        source.setPostalCode("SW12WS");
        source.setCountry("UK");
    }

    @Test
    public void populate_ShouldPopulateTargetCorrectly() {
        testObj.populate(source, target);

        assertEquals(CheckoutComPaymentType.SEPA.name(), target.getType());
        assertEquals("Recurring payment", target.getPaymentType());
        assertEquals("GB56789123456", target.getAccountIban());
        assertEquals("John", target.getFirstName());
        assertEquals("Snow", target.getLastName());
        assertEquals("1 Buckingham Palace Road", target.getAddressLine1());
        assertEquals("Royal Palace", target.getAddressLine2());
        assertEquals("London", target.getCity());
        assertEquals("SW12WS", target.getPostalCode());
        assertEquals("UK", target.getCountry());
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
