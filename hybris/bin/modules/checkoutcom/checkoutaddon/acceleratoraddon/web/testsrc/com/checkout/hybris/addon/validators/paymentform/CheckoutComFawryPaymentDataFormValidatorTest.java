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
public class CheckoutComFawryPaymentDataFormValidatorTest {

    private static final String MOBILE_NUMBER_KEY = "mobileNumber";
    private static final String MOBILE_NUMBER_MAP_FIELD = "formAttributes[mobileNumber]";

    @InjectMocks
    private CheckoutComFawryPaymentDataFormValidator testObj;

    private PaymentDataForm paymentDataForm = new PaymentDataForm();
    private Map<String, Object> attributesMap = new HashMap();

    private Errors errors;

    @Before
    public void setUp() {
        attributesMap.put(MOBILE_NUMBER_KEY, "12345678901");
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
    public void validate_WhenMobileNumberIsValid_ShouldNotReturnAnyError() {
        testObj.validate(paymentDataForm, errors);

        assertFalse(errors.hasErrors());
        assertEquals(0, errors.getErrorCount());
    }

    @Test
    public void validate_WhenAttributeNotFoundOnMap_ShouldReturnError() {
        paymentDataForm.getFormAttributes().remove(MOBILE_NUMBER_KEY);

        testObj.validate(paymentDataForm, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(MOBILE_NUMBER_MAP_FIELD, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenMobileNumberContainsInvalidCharacters_ShouldReturnError() {
        paymentDataForm.getFormAttributes().replace(MOBILE_NUMBER_KEY, "12dfr245678");

        testObj.validate(paymentDataForm, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(MOBILE_NUMBER_MAP_FIELD, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenMobileIsNotElevenCharacters_ShouldReturnError() {
        paymentDataForm.getFormAttributes().replace(MOBILE_NUMBER_KEY, "12345");

        testObj.validate(paymentDataForm, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(MOBILE_NUMBER_MAP_FIELD, errors.getFieldError().getField());
    }
}
