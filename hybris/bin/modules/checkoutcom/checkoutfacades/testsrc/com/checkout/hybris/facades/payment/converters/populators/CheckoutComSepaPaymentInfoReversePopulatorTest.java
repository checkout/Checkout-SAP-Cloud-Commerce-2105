package com.checkout.hybris.facades.payment.converters.populators;

import com.checkout.hybris.core.enums.SepaPaymentType;
import com.checkout.hybris.core.model.CheckoutComSepaPaymentInfoModel;
import com.checkout.hybris.facades.beans.SepaPaymentInfoData;
import de.hybris.bootstrap.annotations.UnitTest;
import org.junit.Test;

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.SEPA;
import static org.junit.Assert.assertEquals;

@UnitTest
public class CheckoutComSepaPaymentInfoReversePopulatorTest {

    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Snow";
    private static final String PAYMENT_TYPE = "RECURRING";
    private static final String ACCOUNT_IBAN = "GB56789123456";
    private static final String ADDRESS_LINE1 = "1 Buckingham Palace Road";
    private static final String ADDRESS_LINE2 = "Royal Palace";
    private static final String CITY = "London";
    private static final String POSTAL_CODE = "SW12WS";
    private static final String COUNTRY = "UK";

    private CheckoutComSepaPaymentInfoReversePopulator testObj = new CheckoutComSepaPaymentInfoReversePopulator();

    private SepaPaymentInfoData source = new SepaPaymentInfoData();
    private CheckoutComSepaPaymentInfoModel target = new CheckoutComSepaPaymentInfoModel();

    @Test
    public void populate_ShouldPopulateTargetCorrectly() {
        source.setAccountIban(ACCOUNT_IBAN);
        source.setPaymentType(PAYMENT_TYPE);
        source.setFirstName(FIRST_NAME);
        source.setType(SEPA.name());
        source.setLastName(LAST_NAME);
        source.setPostalCode(POSTAL_CODE);
        source.setAddressLine1(ADDRESS_LINE1);
        source.setAddressLine2(ADDRESS_LINE2);
        source.setCity(CITY);
        source.setCountry(COUNTRY);

        testObj.populate(source, target);

        assertEquals(ACCOUNT_IBAN, target.getAccountIban());
        assertEquals(SepaPaymentType.RECURRING, target.getPaymentType());
        assertEquals(FIRST_NAME, target.getFirstName());
        assertEquals(LAST_NAME, target.getLastName());
        assertEquals(POSTAL_CODE, target.getPostalCode());
        assertEquals(ADDRESS_LINE1, target.getAddressLine1());
        assertEquals(ADDRESS_LINE2, target.getAddressLine2());
        assertEquals(CITY, target.getCity());
        assertEquals(COUNTRY, target.getCountry());
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