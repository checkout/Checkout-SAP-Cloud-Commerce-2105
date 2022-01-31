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
public class CheckoutComIdealPaymentDataFormValidatorTest {

    private static final String BIC_KEY = "bic";
    private static final String BIC_KEY_MAP_FIELD = "formAttributes[bic]";

    @InjectMocks
    private CheckoutComIdealPaymentDataFormValidator testObj;

    private PaymentDataForm paymentDataForm = new PaymentDataForm();
    private Map<String, Object> attributesMap = new HashMap();

    private Errors errors;

    @Before
    public void setUp() {
        attributesMap.put(BIC_KEY, "ORD50234E89");
        paymentDataForm.setFormAttributes(attributesMap);

        errors = new BeanPropertyBindingResult(paymentDataForm, paymentDataForm.getClass().getSimpleName());
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
    public void validate_WhenBicFieldElevenCharValid_ShouldNotReturnAnyError() {
        testObj.validate(paymentDataForm, errors);

        assertFalse(errors.hasErrors());
        assertEquals(0, errors.getErrorCount());
    }

    @Test
    public void validate_WhenAttributeNotFoundOnMap_ShouldReturnError() {
        paymentDataForm.getFormAttributes().remove(BIC_KEY);

        testObj.validate(paymentDataForm, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(BIC_KEY_MAP_FIELD, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenBicContainsInvalidCharacters_ShouldReturnError() {
        paymentDataForm.getFormAttributes().replace(BIC_KEY, "ORD50/E89");

        testObj.validate(paymentDataForm, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(BIC_KEY_MAP_FIELD, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenBicIsDoesNotMatchLengthRequirement_ShouldReturnError() {
        paymentDataForm.getFormAttributes().replace(BIC_KEY, "123456789");

        testObj.validate(paymentDataForm, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(BIC_KEY_MAP_FIELD, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenBicIsBlank_ShouldReturnError() {
        paymentDataForm.getFormAttributes().replace(BIC_KEY, "   ");

        testObj.validate(paymentDataForm, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(BIC_KEY_MAP_FIELD, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenBicIsExactlyEightChar_ShouldNotReturnError() {
        paymentDataForm.getFormAttributes().replace(BIC_KEY, "tg4gasg5");

        testObj.validate(paymentDataForm, errors);

        assertFalse(errors.hasErrors());
        assertEquals(0, errors.getErrorCount());
    }
}
