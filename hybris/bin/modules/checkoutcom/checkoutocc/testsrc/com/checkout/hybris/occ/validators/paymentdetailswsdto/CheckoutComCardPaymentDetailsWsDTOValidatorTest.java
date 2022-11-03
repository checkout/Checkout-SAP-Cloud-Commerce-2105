package com.checkout.hybris.occ.validators.paymentdetailswsdto;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commercewebservicescommons.dto.order.CardTypeWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.order.PaymentDetailsWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.user.AddressWsDTO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import static org.junit.Assert.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComCardPaymentDetailsWsDTOValidatorTest {

    private static final String BLANK_STRING = "   ";
    private static final String CARD_BIN_KEY = "cardBin";
    private static final String CARD_TYPE = "cardType.code";
    private static final String CARD_NUMBER_KEY = "cardNumber";
    private static final String EXPIRY_YEAR_KEY = "expiryYear";
    private static final String EXPIRY_MONTH_KEY = "expiryMonth";
    private static final String PAYMENT_TOKEN_KEY = "paymentToken";
    private static final String CARTES_BANCAIRES = "cartes_bancaires";

    @InjectMocks
    private CheckoutComCardPaymentDetailsWsDTOValidator testObj;

    private Errors errors;

    private PaymentDetailsWsDTO paymentDetailsWsDTO = new PaymentDetailsWsDTO();

    @Before
    public void setUp() {
        errors = new BeanPropertyBindingResult(paymentDetailsWsDTO, paymentDetailsWsDTO.getClass().getSimpleName());

        final CardTypeWsDTO cardTypeWsDTO = new CardTypeWsDTO();
        cardTypeWsDTO.setCode("American Express");
        cardTypeWsDTO.setName("American Express");
        paymentDetailsWsDTO.setCardType(cardTypeWsDTO);
        paymentDetailsWsDTO.setPaymentToken("token");
        paymentDetailsWsDTO.setCardNumber("123456789");
        paymentDetailsWsDTO.setCardBin("123456");
        paymentDetailsWsDTO.setExpiryMonth("12");
        paymentDetailsWsDTO.setExpiryYear("2020");
    }

    @Test
    public void supports_WhenPaymentDetailsWsDTOType_ShouldReturnTrue() {
        assertTrue(testObj.supports(PaymentDetailsWsDTO.class));
    }

    @Test
    public void supports_WhenNotCorrectType_ShouldReturnFalse() {
        assertFalse(testObj.supports(AddressWsDTO.class));
    }

    @Test
    public void validate_WhenAttributesAreValid_ShouldNotReturnAnyError() {
        testObj.validate(paymentDetailsWsDTO, errors);

        assertFalse(errors.hasErrors());
        assertEquals(0, errors.getErrorCount());
    }

    @Test
    public void validate_WhenPaymentTokenIsBlank_ShouldReturnError() {
        paymentDetailsWsDTO.setPaymentToken(BLANK_STRING);

        testObj.validate(paymentDetailsWsDTO, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(PAYMENT_TOKEN_KEY, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenNumberIsBlank_ShouldReturnError() {
        paymentDetailsWsDTO.setCardNumber(BLANK_STRING);

        testObj.validate(paymentDetailsWsDTO, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(CARD_NUMBER_KEY, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenCardBinIsBlank_ShouldReturnError() {
        paymentDetailsWsDTO.setCardBin(BLANK_STRING);

        testObj.validate(paymentDetailsWsDTO, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(CARD_BIN_KEY, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenValidToMonthIsBlank_ShouldReturnError() {
        paymentDetailsWsDTO.setExpiryMonth(BLANK_STRING);

        testObj.validate(paymentDetailsWsDTO, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(EXPIRY_MONTH_KEY, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenValidToYearIsBlank_ShouldReturnError() {
        paymentDetailsWsDTO.setExpiryYear(BLANK_STRING);

        testObj.validate(paymentDetailsWsDTO, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(EXPIRY_YEAR_KEY, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenCardTypeIsBlank_ShouldReturnError() {
        paymentDetailsWsDTO.setCardType(null);

        testObj.validate(paymentDetailsWsDTO, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(CARD_TYPE, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenCartesBancairesIsUsedAsCardType_ShouldReturnError() {
        final CardTypeWsDTO cardTypeWsDTO = new CardTypeWsDTO();
        cardTypeWsDTO.setCode(CARTES_BANCAIRES);
        cardTypeWsDTO.setName(CARTES_BANCAIRES);
        paymentDetailsWsDTO.setCardType(cardTypeWsDTO);

        testObj.validate(paymentDetailsWsDTO, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(CARD_TYPE, errors.getFieldError().getField());
    }
}
