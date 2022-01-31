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

import static com.checkout.hybris.addon.validators.paymentform.CheckoutComKlarnaPaymentDataFormValidator.AUTHORIZATION_TOKEN_KEY;
import static org.junit.Assert.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComKlarnaPaymentDataFormValidatorTest {

    private static final String AUTHORIZATION_TOKEN_MAP_FIELD = "formAttributes[authorizationToken]";

    @InjectMocks
    private CheckoutComKlarnaPaymentDataFormValidator testObj;

    private PaymentDataForm paymentDataForm = new PaymentDataForm();
    private Map<String, Object> attributesMap = new HashMap();

    private Errors errors;

    @Before
    public void setUp() {
        attributesMap.put(AUTHORIZATION_TOKEN_KEY, "12345678901_abdajkdjal");
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
    public void validate_WhenAuthorizationTokenIsPopulated_ShouldNotReturnErrors() {
        testObj.validate(paymentDataForm, errors);

        assertFalse(errors.hasErrors());
        assertEquals(0, errors.getErrorCount());
    }

    @Test
    public void validate_WhenAttributeNotFoundOnMap_ShouldReturnError() {
        paymentDataForm.getFormAttributes().remove(AUTHORIZATION_TOKEN_KEY);

        testObj.validate(paymentDataForm, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(AUTHORIZATION_TOKEN_MAP_FIELD, errors.getFieldError().getField());
    }

    @Test
    public void validate_WhenAttributeNotPopulated_ShouldReturnError() {
        attributesMap.put(AUTHORIZATION_TOKEN_KEY, "    ");

        testObj.validate(paymentDataForm, errors);

        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals(AUTHORIZATION_TOKEN_MAP_FIELD, errors.getFieldError().getField());
    }
}