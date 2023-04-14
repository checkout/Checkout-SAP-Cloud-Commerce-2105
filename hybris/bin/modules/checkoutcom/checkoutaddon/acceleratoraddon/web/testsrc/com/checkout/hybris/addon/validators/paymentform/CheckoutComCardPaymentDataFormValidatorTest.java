package com.checkout.hybris.addon.validators.paymentform;

import com.checkout.hybris.addon.forms.PaymentDataForm;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.acceleratorstorefrontcommons.forms.ForgottenPwdForm;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComCardPaymentDataFormValidatorTest {

    private static final String NUMBER_KEY = "number";
    private static final String CARD_BIN_KEY = "cardBin";
    private static final String CARD_TYPE_KEY = "cardType";
    private static final String VALID_TO_YEAR_KEY = "validToYear";
    private static final String PAYMENT_TOKEN_KEY = "paymentToken";
    private static final String VALID_TO_MONTH_KEY = "validToMonth";
    private static final String CARTES_BANCAIRES = "cartes_bancaires";

    @InjectMocks
    private CheckoutComCardPaymentDataFormValidator testObj;

    private Errors errors;

    private PaymentDataForm paymentDataForm = new PaymentDataForm();
    private Map<String, Object> attributesMap = new HashMap();

    @Before
    public void setUp() {
        errors = new BeanPropertyBindingResult(paymentDataForm, paymentDataForm.getClass().getSimpleName());

        attributesMap.put(PAYMENT_TOKEN_KEY, "token");
        attributesMap.put(NUMBER_KEY, "123456789");
        attributesMap.put(CARD_BIN_KEY, "123456");
        attributesMap.put(VALID_TO_MONTH_KEY, "12");
        attributesMap.put(VALID_TO_YEAR_KEY, "2020");
        attributesMap.put(CARD_TYPE_KEY, "American Express");

        paymentDataForm.setFormAttributes(attributesMap);
    }

    @Test
    public void supports_WhenPaymentDataFormType_ShouldReturnTrue() {
        assertTrue(testObj.supports(PaymentDataForm.class));
    }

    @Test
    public void supports_WhenNotCorrectType_ShouldReturnFalse() {
        assertFalse(testObj.supports(ForgottenPwdForm.class));
    }

    @Test
    public void validate_WhenAttributesAreValid_ShouldNotReturnAnyError() {
        testObj.validate(paymentDataForm, errors);

        assertFalse(errors.hasErrors());
        assertEquals(0, errors.getErrorCount());
    }

    @Test
    public void validate_WhenPaymentTokenIsBlank_ShouldReturnError() {
        paymentDataForm.getFormAttributes().replace(PAYMENT_TOKEN_KEY, "   ");

        testObj.validate(paymentDataForm, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals("formAttributes[paymentToken]", errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenNumberIsBlank_ShouldReturnError() {
        paymentDataForm.getFormAttributes().replace(NUMBER_KEY, "   ");

        testObj.validate(paymentDataForm, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals("formAttributes[" + NUMBER_KEY + "]", errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenCardBinIsBlank_ShouldReturnError() {
        paymentDataForm.getFormAttributes().replace(CARD_BIN_KEY, "   ");

        testObj.validate(paymentDataForm, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals("formAttributes[" + CARD_BIN_KEY + "]", errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenValidToMonthIsBlank_ShouldReturnError() {
        paymentDataForm.getFormAttributes().replace(VALID_TO_MONTH_KEY, "   ");

        testObj.validate(paymentDataForm, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals("formAttributes[" + VALID_TO_MONTH_KEY + "]", errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenValidToYearIsBlank_ShouldReturnError() {
        paymentDataForm.getFormAttributes().replace(VALID_TO_YEAR_KEY, "   ");

        testObj.validate(paymentDataForm, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals("formAttributes[" + VALID_TO_YEAR_KEY + "]", errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenCardTypeIsBlank_ShouldReturnError() {
        paymentDataForm.getFormAttributes().replace(CARD_TYPE_KEY, null);

        testObj.validate(paymentDataForm, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals("formAttributes[" + CARD_TYPE_KEY + "]", errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenAttributeNotFoundOnMap_ShouldReturnError() {
        paymentDataForm.getFormAttributes().remove(VALID_TO_YEAR_KEY);

        testObj.validate(paymentDataForm, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals("formAttributes[" + VALID_TO_YEAR_KEY + "]", errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenCartesBancairesIsUsedAsCardType_ShouldReturnError() {
        paymentDataForm.getFormAttributes().replace(CARD_TYPE_KEY, CARTES_BANCAIRES);

        testObj.validate(paymentDataForm, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals("formAttributes[" + CARD_TYPE_KEY + "]", errors.getFieldError().getField());
    }
}
