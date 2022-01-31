package com.checkout.hybris.occ.converters.populators;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commercefacades.order.data.CCPaymentInfoData;
import de.hybris.platform.commercewebservicescommons.dto.order.CardTypeWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.order.PaymentDetailsWsDTO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComCCPaymentDetailsDTOToPaymentInfoDataPopulatorTest {

    private static final String NUMBER = "1221321321";
    private static final String TOKEN = "token";
    private static final String CARD_TYPE_WITH_SPACES = "American Express";
    private static final String VALID_TO_MONTH = "12";
    private static final String VALID_TO_YEAR = "2022";
    private static final String CARD_TYPE_STRIPPED_SPACES = "AmericanExpress";
    private static final String CARD_BIN = "123456";
    private static final String ACCOUNT_HOLDER_NAME = "John Doe";

    private CheckoutComCCPaymentDetailsDTOToPaymentInfoDataPopulator testObj = new CheckoutComCCPaymentDetailsDTOToPaymentInfoDataPopulator();

    private PaymentDetailsWsDTO paymentTokenForm;
    private CCPaymentInfoData ccPaymentInfoData;

    @Before
    public void setUp() {
        final CardTypeWsDTO cardTypeWsDTO = new CardTypeWsDTO();
        ccPaymentInfoData = new CCPaymentInfoData();
        paymentTokenForm = new PaymentDetailsWsDTO();

        cardTypeWsDTO.setCode(CARD_TYPE_WITH_SPACES);
        paymentTokenForm.setPaymentToken(TOKEN);
        paymentTokenForm.setCardNumber(NUMBER);
        paymentTokenForm.setCardBin(CARD_BIN);
        paymentTokenForm.setExpiryMonth(VALID_TO_MONTH);
        paymentTokenForm.setExpiryYear(VALID_TO_YEAR);
        paymentTokenForm.setCardType(cardTypeWsDTO);
        paymentTokenForm.setAccountHolderName(ACCOUNT_HOLDER_NAME);
    }

    @Test(expected = IllegalArgumentException.class)
    public void populate_WithNullSource_ShouldThrowException() {
        testObj.populate(null, ccPaymentInfoData);
    }

    @Test(expected = IllegalArgumentException.class)
    public void populate_WithNullTarget_ShouldThrowException() {
        testObj.populate(paymentTokenForm, null);
    }

    @Test
    public void populate_WhenSaveCardNotChecked_ShouldPopulateEverythingCorrectly() {
        testObj.populate(paymentTokenForm, ccPaymentInfoData);

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
        paymentTokenForm.setSaved(Boolean.TRUE);

        testObj.populate(paymentTokenForm, ccPaymentInfoData);

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
