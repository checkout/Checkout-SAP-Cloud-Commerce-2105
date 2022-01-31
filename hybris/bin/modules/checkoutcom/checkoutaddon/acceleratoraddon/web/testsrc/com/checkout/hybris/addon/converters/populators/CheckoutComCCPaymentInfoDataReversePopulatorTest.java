package com.checkout.hybris.addon.converters.populators;

import com.checkout.hybris.addon.forms.PaymentDataForm;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commercefacades.order.data.CCPaymentInfoData;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.checkout.hybris.addon.converters.populators.CheckoutComCCPaymentInfoDataReversePopulator.SAVE_CARD_KEY;
import static org.junit.Assert.*;

@UnitTest
public class CheckoutComCCPaymentInfoDataReversePopulatorTest {

    private static final String NUMBER = "1221321321";
    private static final String TOKEN = "token";
    private static final String CARD_TYPE_WITH_SPACES = "American Express";
    private static final String VALID_TO_MONTH = "12";
    private static final String VALID_TO_YEAR = "2022";
    private static final String CARD_TYPE_STRIPPED_SPACES = "AmericanExpress";
    private static final String CARD_BIN = "123456";
    private static final String ACCOUNT_HOLDER_NAME = "John Doe";

    private static final String PAYMENT_TOKEN_KEY = "paymentToken";
    private static final String NUMBER_KEY = "number";
    private static final String CARD_BIN_KEY = "cardBin";
    private static final String VALID_TO_MONTH_KEY = "validToMonth";
    private static final String VALID_TO_YEAR_KEY = "validToYear";
    private static final String CARD_TYPE_KEY = "cardType";
    private static final String ACCOUNT_HOLDER_NAME_KEY = "accountHolderName";

    private CheckoutComCCPaymentInfoDataReversePopulator testObj = new CheckoutComCCPaymentInfoDataReversePopulator();

    private PaymentDataForm paymentDataForm;
    private CCPaymentInfoData ccPaymentInfoData;
    private Map<String, Object> attributesMap = new HashMap();

    @Before
    public void setUp() {
        ccPaymentInfoData = new CCPaymentInfoData();
        paymentDataForm = new PaymentDataForm();

        attributesMap.put(PAYMENT_TOKEN_KEY, TOKEN);
        attributesMap.put(NUMBER_KEY, NUMBER);
        attributesMap.put(CARD_BIN_KEY, CARD_BIN);
        attributesMap.put(VALID_TO_MONTH_KEY, VALID_TO_MONTH);
        attributesMap.put(VALID_TO_YEAR_KEY, VALID_TO_YEAR);
        attributesMap.put(CARD_TYPE_KEY, CARD_TYPE_WITH_SPACES);
        attributesMap.put(ACCOUNT_HOLDER_NAME_KEY, ACCOUNT_HOLDER_NAME);
        paymentDataForm.setFormAttributes(attributesMap);
    }

    @Test(expected = IllegalArgumentException.class)
    public void populate_WithNullSource_ShouldThrowException() {
        testObj.populate(null, ccPaymentInfoData);
    }

    @Test(expected = IllegalArgumentException.class)
    public void populate_WithNullTarget_ShouldThrowException() {
        testObj.populate(paymentDataForm, null);
    }

    @Test
    public void populate_WhenSaveCardNotChecked_ShouldPopulateEverythingCorrectly() {
        testObj.populate(paymentDataForm, ccPaymentInfoData);

        assertEquals(NUMBER, ccPaymentInfoData.getCardNumber());
        assertEquals(TOKEN, ccPaymentInfoData.getPaymentToken());
        assertEquals(VALID_TO_MONTH, ccPaymentInfoData.getExpiryMonth());
        assertEquals(VALID_TO_YEAR, ccPaymentInfoData.getExpiryYear());
        assertEquals(CARD_TYPE_STRIPPED_SPACES, ccPaymentInfoData.getCardType());
        assertEquals(ACCOUNT_HOLDER_NAME, ccPaymentInfoData.getAccountHolderName());
        assertFalse(ccPaymentInfoData.isSaved());
    }

    @Test
    public void populate_WhenSaveCardChecked_ShouldPopulateEverythingCorrectly() {
        attributesMap.put(SAVE_CARD_KEY, "true");

        testObj.populate(paymentDataForm, ccPaymentInfoData);

        assertEquals(NUMBER, ccPaymentInfoData.getCardNumber());
        assertEquals(TOKEN, ccPaymentInfoData.getPaymentToken());
        assertEquals(VALID_TO_MONTH, ccPaymentInfoData.getExpiryMonth());
        assertEquals(VALID_TO_YEAR, ccPaymentInfoData.getExpiryYear());
        assertEquals(CARD_TYPE_STRIPPED_SPACES, ccPaymentInfoData.getCardType());
        assertTrue(ccPaymentInfoData.isSaved());
        assertEquals(CARD_BIN, ccPaymentInfoData.getCardBin());
        assertEquals(ACCOUNT_HOLDER_NAME, ccPaymentInfoData.getAccountHolderName());
    }
}
